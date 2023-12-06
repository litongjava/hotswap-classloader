package com.litongjava.hotswap.wrapper.forkapp;

import java.text.DecimalFormat;

import com.litongjava.hotswap.kit.HotSwapUtils;
import com.litongjava.hotswap.server.RestartServer;
import com.litongjava.hotswap.watcher.HotSwapWatcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForkApp {

  protected static HotSwapWatcher hotSwapWatcher;
  protected static DecimalFormat decimalFormat = new DecimalFormat("#.#");

  protected static String getTimeSpent(long startTime) {
    float timeSpent = (System.currentTimeMillis() - startTime) / 1000F;
    return decimalFormat.format(timeSpent);
  }

  public static void run(Class<?> primarySource,String[] args,boolean dev, RestartServer restarServer) {
    if(dev) {
      // 获取自定义的classLoalder
      ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();

      // 设置默认的类加载器即可
      Thread.currentThread().setContextClassLoader(hotSwapClassLoader);
      restarServer.start(primarySource,args);
      ForkAppBootArgument.init(primarySource, args,dev);
      
      if (hotSwapWatcher == null) {
        // 使用反射执行下面的代码
        log.info("start hotSwapWatcher");
        hotSwapWatcher = new HotSwapWatcher(restarServer);
        hotSwapWatcher.start();
      }
    }else {
      restarServer.start(primarySource,args);
      ForkAppBootArgument.init(primarySource, args,dev);
    }
  }

}
