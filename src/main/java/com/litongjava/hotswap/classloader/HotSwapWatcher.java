package com.litongjava.hotswap.classloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.litongjava.hotswap.kit.UndertowKit;
import com.litongjava.hotswap.server.RestartServer;

/**
 * 监听 class path 下 .class 文件变动，触发 UndertowServer.restart()
 */
public class HotSwapWatcher extends Thread {
  
  protected RestartServer server;
  
  // protected int watchingInterval = 1000;  // 1900 与 2000 相对灵敏
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
//    System.out.println("watchingPaths:");
//    for (Path path : watchingPaths) {
//      System.out.println(path.getFileName());
//    }
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
    for (String dir : dirList) {
      pathList.add(Paths.get(dir));
    }
    
    return pathList;
  }
  
  private void buildDirs(File file, Set<String> watchingDirSet) {
    if (file.isDirectory()) {
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
    WatchService watcher = FileSystems.getDefault().newWatchService();
    addShutdownHook(watcher);
    
    for (Path path : watchingPaths) {
      path.register(
          watcher,
          // StandardWatchEventKinds.ENTRY_DELETE,
          StandardWatchEventKinds.ENTRY_MODIFY,
          StandardWatchEventKinds.ENTRY_CREATE
      );
    }
    
    while (running) {
      try {
        // watchKey = watcher.poll(watchingInterval, TimeUnit.MILLISECONDS);  // watcher.take(); 阻塞等待
        // 比较两种方式的灵敏性，或许 take() 方法更好，起码资源占用少，测试 windows 机器上的响应
        watchKey = watcher.take();
        
        if (watchKey == null) {
          // System.out.println(System.currentTimeMillis() / 1000);
          continue ;
        }
      } catch (Throwable e) {            // 控制台 ctrl + c 退出 JVM 时也将抛出异常
        running = false;
        if (e instanceof InterruptedException) {  // 另一线程调用 hotSwapWatcher.interrupt() 抛此异常
          Thread.currentThread().interrupt();  // Restore the interrupted status
        }
        break ;
      }
      
      List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
       for(WatchEvent<?> event : watchEvents) {
        String fileName = event.context().toString();
        if (fileName.endsWith(".class")) {
          if (server.isStarted()) {
            server.restart();
            resetWatchKey();
            
            while((watchKey = watcher.poll()) != null) {
              // System.out.println("---> poll() ");
              watchKey.pollEvents();
              resetWatchKey();
            }
            
            break ;
          }
        }
      }
      
      resetWatchKey();
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
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        watcher.close();
      } catch (Throwable e) {
        UndertowKit.doNothing(e);
      }
    }));
  }
  
  public void exit() {
    running = false;
    try {
      this.interrupt();
    } catch (Throwable e) {
      UndertowKit.doNothing(e);
    }
  }
  
//  public static void main(String[] args) throws InterruptedException {
//    HotSwapWatcher watcher = new HotSwapWatcher(null);
//    watcher.start();
//    
//    System.out.println("启动成功");
//    Thread.currentThread().join(99999999);
//  }
}