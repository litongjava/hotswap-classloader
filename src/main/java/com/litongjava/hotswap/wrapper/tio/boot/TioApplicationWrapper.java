package com.litongjava.hotswap.wrapper.tio.boot;

import com.litongjava.hotswap.kit.HotSwapUtils;
import com.litongjava.hotswap.watcher.HotSwapWatcher;
import com.litongjava.tio.boot.TioApplication;
import com.litongjava.tio.boot.context.Context;
import com.litongjava.tio.boot.context.TioBootConfiguration;

import lombok.extern.slf4j.Slf4j;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:06:48 
 * @version 1.0 
 */
@Slf4j
public class TioApplicationWrapper {
  protected static volatile HotSwapWatcher hotSwapWatcher;

  public static Context run(Class<?> primarySource, String[] args) {
    return run(new Class<?>[] { primarySource }, args);
  }

  public static Context run(Class<?> primarySource, TioBootConfiguration config, String[] args) {
    return run(new Class<?>[] { primarySource }, config, args);
  }

  private static Context run(Class<?>[] primarySources, String[] args) {
    return run(primarySources, null, args);
  }

  private static Context run(Class<?>[] primarySources, TioBootConfiguration config, String[] args) {
    String mode = null;
    // 检查命令行参数中是否包含 --mode=dev
    for (String arg : args) {
      String[] split = arg.split("=");
      if (split[0].equals("--mode")) {
        mode = split[1];
        break;
      }
    }
    return run(primarySources, args, config, "dev".equals(mode));
  }

  /**
   * 如果isDev=true
   * 使用自定义的HostSwapClassLoader启动SpringApplication
   * 如果isDev=false
   * 使用默认的加载
   * 
   */
  public static Context run(Class<?>[] primarySources, String[] args, TioBootConfiguration config, boolean isDev) {
    if (isDev) {
      return runDev(primarySources, config, args);
    } else {
      return TioApplication.run(primarySources, config, args);
    }
  }

  /**
   * 在开发模式下运行,使用HotSwapClassloader和HotSwapWatch
   */

  public static Context runDev(Class<?>[] primarySources, TioBootConfiguration config, String[] args) {

    // 获取自定义的classLoalder
    ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();
    log.info("new hotSwapClassLoader:{}", hotSwapClassLoader);
    Thread.currentThread().setContextClassLoader(hotSwapClassLoader);

    // run
    Context context = TioApplication.run(primarySources, config, args);
    TioBootArgument.init(primarySources,config,args, context, true);

    if (hotSwapWatcher == null) {
      // 使用反射执行下面的代码
      log.info("start hotSwapWatcher");
      hotSwapWatcher = new HotSwapWatcher(new TioBootRestartServer());
      hotSwapWatcher.start();
    }

    return context;

  }

}
