package com.litongjava.hotswap.wrapper.tio.boot;

import com.litongjava.hotswap.kit.HotSwapUtils;
import com.litongjava.hotswap.watcher.HotSwapWatcher;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.TioApplication;
import com.litongjava.tio.boot.context.Context;
import com.litongjava.tio.boot.context.TioApplicationContext;
import com.litongjava.tio.server.intf.ServerAioHandler;
import com.litongjava.tio.server.intf.ServerAioListener;
import com.litongjava.tio.utils.jfinal.P;

import lombok.extern.slf4j.Slf4j;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:06:48 
 * @version 1.0 
 * @desc
 */
@Slf4j
public class TioApplicationWrapper {
  protected static volatile HotSwapWatcher hotSwapWatcher;

  public static Context run(Class<?> primarySource, String... args) {
    return run(new Class<?>[] { primarySource }, null, null, args);
  }

  public static Context run(Class<?> primarySource, ServerAioHandler handler, String... args) {
    return run(new Class<?>[] { primarySource }, handler, null, args);
  }

  public static Context run(Class<?> primarySource, ServerAioHandler handler, ServerAioListener listener,
      String... args) {
    return run(new Class<?>[] { primarySource }, handler, listener, args);
  }

  private static Context run(Class<?>[] primarySources, ServerAioHandler handler, ServerAioListener listener,
      String[] args) {

    String mode = null;
    // 检查命令行参数中是否包含 --mode=dev
    for (String arg : args) {
      String[] split = arg.split("=");
      if (split[0].equals("--mode")) {
        mode = split[1];
        break;
      }
    }

    if (mode == null) {
      if (P.isLoad()) {
        mode = P.get("mode");
      }

    }
    return run(primarySources, handler, listener, args, "dev".equals(mode));
  }

  /**
   * 如果isDev=true
   * 使用自定义的HostSwapClassLoader启动SpringApplication
   * 如果isDev=false
   * 使用默认的加载
   * @param clazz
   * @param args
   * 
   */
  public static Context run(Class<?>[] primarySources, ServerAioHandler handler, ServerAioListener listener,
      String[] args, Boolean isDev) {
    if (isDev) {
      return runDev(primarySources, handler, listener, args);
    } else {
      return TioApplication.run(primarySources, handler, listener, args);
    }
  }

  /**
   * 支持可变参数
   * @param isDev
   * @param primarySource
   * @param args
   * @return
   */
  public static Context run(Boolean isDev, Class<?> primarySource, String... args) {
    return run(new Class<?>[] { primarySource }, null, null, args, isDev);
  }

  public static Context run(Boolean isDev, Class<?>[] primarySources, String[] args) {
    Context context = Aop.get(TioApplicationContext.class);
    return context.run(primarySources, args);

  }

  /**
   * 在开放模式下运行,使用HotSwapClassloader和HotSwapWatch
   * @param clazz
   * @param args
   * @param isDev
   * @return
   */
  private static Context runDev(Class<?>[] primarySources, ServerAioHandler handler, ServerAioListener listener,
      String[] args) {
    // 获取自定义的classLoalder
    ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();
    log.info("hotSwapClassLoader:{}", hotSwapClassLoader);

    Thread.currentThread().setContextClassLoader(hotSwapClassLoader);
    // run
    Context context = TioApplication.run(primarySources, handler, listener, args);
    TioBootArgument.init(primarySources,handler, listener,args, context, true);

    if (hotSwapWatcher == null) {
      // 使用反射执行下面的代码
      log.info("start hotSwapWatcher");
      hotSwapWatcher = new HotSwapWatcher(new TioBootRestartServer());
      hotSwapWatcher.start();
    }

    return context;
  }
}
