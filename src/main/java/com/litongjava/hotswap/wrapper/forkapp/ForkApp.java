package com.litongjava.hotswap.wrapper.forkapp;

import java.text.DecimalFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.litongjava.hotswap.kit.HotSwapUtils;
import com.litongjava.hotswap.server.RestartServer;
import com.litongjava.hotswap.watcher.HotSwapWatcher;

public class ForkApp {

  protected static HotSwapWatcher hotSwapWatcher;
  protected static DecimalFormat decimalFormat = new DecimalFormat("#.#");

  public static <T> Future<T> run(Callable<T> task) {
    // 获取自定义的classLoalder
    ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();

    // 设置默认的类加载器即可
    Thread.currentThread().setContextClassLoader(hotSwapClassLoader);

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    final Future<T> future = executorService.submit(task);
    // 需要在spring启动之前启动hotswapWatcher,否则springboot重启之后,hotswapWatcher会也关闭 测试不需要
    // 在spring-boot启动之后再启动hotSwapWatcher
    if (hotSwapWatcher == null) {
      // 使用反射执行下面的代码
      hotSwapWatcher = new HotSwapWatcher(new RestartServer() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<T> newFuture=future;

        @Override
        public boolean isStarted() {
          boolean shutdown = executorService.isShutdown();
          System.out.println("shutdown:" + shutdown);
          return !shutdown;
        }

        @Override
        public void restart() {
          System.err.println("loading");
          long start = System.currentTimeMillis();
          newFuture.cancel(true);
          executorService.shutdownNow();

          ClassLoader hotSwapClassLoader = HotSwapUtils.newClassLoader();

          // 在启动新的应用之前必须设置上下文加载器
          Thread.currentThread().setContextClassLoader(hotSwapClassLoader);
          // 启动SpringApplication

          newFuture = executorService.submit(task);
          try {
            System.out.println(newFuture.get());
          } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
          }
          System.err.println("Loading complete in " + getTimeSpent(start) + " seconds (^_^)\n");
        }
      });
      hotSwapWatcher.start();
    }
    return future;
  }

  protected static String getTimeSpent(long startTime) {
    float timeSpent = (System.currentTimeMillis() - startTime) / 1000F;
    return decimalFormat.format(timeSpent);
  }

  public static void run(Runnable task) {
    
  }

}
