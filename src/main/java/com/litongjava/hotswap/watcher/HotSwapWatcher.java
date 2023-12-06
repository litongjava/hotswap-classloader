package com.litongjava.hotswap.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.litongjava.hotswap.debug.Diagnostic;
import com.litongjava.hotswap.kit.UndertowKit;
import com.litongjava.hotswap.server.RestartServer;

import lombok.extern.slf4j.Slf4j;

/**
 * 监听 class path 下 .class 文件变动,并重启服务器
 */
@Slf4j
public class HotSwapWatcher extends Thread {

  protected RestartServer server;

  // protected int watchingInterval = 1000; // 1900 与 2000 相对灵敏
  protected int watchingInterval = 500;

  protected List<Path> watchingPaths;
  private WatchKey watchKey;
  protected volatile boolean running = true;

  public HotSwapWatcher(RestartServer server) {
    setName("HotSwapWatcher");
    // 避免在调用 deploymentManager.stop()、undertow.stop() 后退出 JVM
    setDaemon(false);
    setPriority(Thread.MAX_PRIORITY);

    this.server = server;
    this.watchingPaths = buildWatchingPaths();

  }

  protected List<Path> buildWatchingPaths() {
    Set<String> watchingDirSet = new HashSet<>();
    String[] classPathArray = System.getProperty("java.class.path").split(File.pathSeparator);
    for (String classPath : classPathArray) {
      buildDirs(new File(classPath.trim()), watchingDirSet);
    }

    List<String> dirList = new ArrayList<String>(watchingDirSet);
    Collections.sort(dirList);

    List<Path> pathList = new ArrayList<Path>(dirList.size());
    if (Diagnostic.isDebug()) {
      log.info("观察的目录有:");
    }
    for (String dir : dirList) {
      if (Diagnostic.isDebug()) {
        System.out.println(dir);
      }
      pathList.add(Paths.get(dir));
    }

    return pathList;
  }

  private void buildDirs(File file, Set<String> watchingDirSet) {
    if (file.isDirectory() && !file.getName().contains("META-INF")) {
      watchingDirSet.add(file.getPath());

      File[] fileList = file.listFiles();
      for (File f : fileList) {
        buildDirs(f, watchingDirSet);
      }
    }
  }

  public void run() {
    try {
      doRun();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  protected void doRun() throws IOException {
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    WatchService watcher = FileSystems.getDefault().newWatchService();

    if (Diagnostic.isDebug()) {
      log.info("文件观察器:{}", watcher);
    }
    addShutdownHook(watcher);

    for (Path path : watchingPaths) {
      path.register(watcher,
          // StandardWatchEventKinds.ENTRY_DELETE,
          StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
    }

    // 降低轮询频率：使用 WatchService 时，可以通过设置轮询频率来控制其对 CPU 的消耗，比如设置为每秒轮询一次，而不是每毫秒轮询一次。可以根据具体情况进行调整。
    executorService.scheduleAtFixedRate(() -> {
      watch(watcher);
    }, 0, 500, TimeUnit.MILLISECONDS);
//    while(running) {
//    	watch(watcher);
//    }

  }

  private void watch(WatchService watcher) {
    try {
      // watchKey = watcher.poll(watchingInterval, TimeUnit.MILLISECONDS); //
      // watcher.take(); 阻塞等待
      // 比较两种方式的灵敏性，或许 take() 方法更好，起码资源占用少，测试 windows 机器上的响应
//      watchKey = watcher.poll();
      watchKey = watcher.take();
    } catch (Throwable e) { // 控制台 ctrl + c 退出 JVM 时也将抛出异常
      running = false;
      if (e instanceof InterruptedException) {
        // 另一线程调用
        // hotSwapWatcher.interrupt() 抛此异常 Restore the interrupted status
        Thread.currentThread().interrupt();
      }
    }
    if (watchKey != null) {
      process(watcher);
    }
    resetWatchKey();
  }

  private void process(WatchService watcher) {
    List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
    if (Diagnostic.isDebug()) {
      log.info("Number of file modifications:{}", watchEvents.size());
    }
    for (WatchEvent<?> event : watchEvents) {
      Kind<?> kind = event.kind();
      String fileName = event.context().toString();

      if (Diagnostic.isDebug()) {
        log.info("{} modifications {},{}", watcher.toString(), kind.toString(), fileName);
      }

      if (kind == StandardWatchEventKinds.OVERFLOW) {
        continue;
      }
      if (fileName.endsWith(".class")) {
        boolean started = server.isStarted();
        if (started) {
          server.restart();
          resetWatchKey();

          while ((watchKey = watcher.poll()) != null) {
            List<WatchEvent<?>> pollEvents = watchKey.pollEvents();
            if (Diagnostic.isDebug()) {
              log.info("跳过的文件修改个数:{}", pollEvents.size());
            }

            resetWatchKey();
          }
          // 跳出for循环
          break;
        }
      }
    }
  }

  private void resetWatchKey() {
    if (watchKey != null) {
      watchKey.reset();
      watchKey = null;
    }
  }

  /**
   * 添加关闭钩子在 JVM 退出时关闭 WatchService
   * 
   * 注意：addShutdownHook 方式添加的回调在 kill -9 pid 强制退出 JVM 时不会被调用
   *      kill 不带参数 -9 时才回调
   */
  protected void addShutdownHook(WatchService watcher) {
    Thread hook = new Thread(() -> {
      try {
        log.info("stop hotswapWatcher");
        watcher.close();
      } catch (Throwable e) {
        UndertowKit.doNothing(e);
      }
    });
    Runtime.getRuntime().addShutdownHook(hook);
  }

  public void exit() {
    running = false;
    try {
      this.interrupt();
    } catch (Throwable e) {
      UndertowKit.doNothing(e);
    }
  }

  // public static void main(String[] args) throws InterruptedException {
  // HotSwapWatcher watcher = new HotSwapWatcher(null);
  // watcher.start();
  //
  // System.out.println("启动成功");
  // Thread.currentThread().join(99999999);
  // }
}