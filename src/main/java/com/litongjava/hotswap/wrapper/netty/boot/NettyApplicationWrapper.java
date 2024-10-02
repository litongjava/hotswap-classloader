package com.litongjava.hotswap.wrapper.netty.boot;

import com.litongjava.context.BootConfiguration;
import com.litongjava.context.Context;
import com.litongjava.hotswap.kit.HotSwapUtils;
import com.litongjava.hotswap.watcher.HotSwapWatcher;
import com.litongjava.hotswap.wrapper.tio.boot.TioBootArgument;
import com.litongjava.hotswap.wrapper.tio.boot.TioBootRestartServer;
import com.litongjava.netty.boot.NettyApplication;
import com.litongjava.tio.constants.TioCoreConfigKeys;
import com.litongjava.tio.utils.environment.EnvUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:06:48
 * @version 1.0
 */
@Slf4j
public class NettyApplicationWrapper {
  protected static volatile HotSwapWatcher hotSwapWatcher;

  public static Context run(Class<?> primarySource, String[] args) {
    return run(new Class<?>[] { primarySource }, args);
  }

  public static Context run(Class<?> primarySource, BootConfiguration config, String[] args) {
    return run(new Class<?>[] { primarySource }, config, args);
  }

  private static Context run(Class<?>[] primarySources, String[] args) {
    return run(primarySources, null, args);
  }

  private static Context run(Class<?>[] primarySources, BootConfiguration config, String[] args) {
    String mode = null;
    // 检查命令行参数中是否包含 --mode=dev
    for (String arg : args) {
      String[] split = arg.split("=");
      if (split[0].equals("--mode")) {
        mode = split[1];
        break;
      }
    }

    // 如果未指定 mode，检查是否有调试器连接
    if (mode == null) {
      if (HotSwapUtils.isDevelopmentEnvironment()) {
        mode = "dev";
      } else {
        mode = "prod";
      }
    }
    return run(primarySources, args, config, "dev".equalsIgnoreCase(mode));
  }

  /**
   * 如果isDev=true 使用自定义的HostSwapClassLoader启动SpringApplication 如果isDev=false
   * 使用默认的加载
   * 
   */
  public static Context run(Class<?>[] primarySources, String[] args, BootConfiguration config, boolean isDev) {
    if (isDev) {
      return runDev(primarySources, config, args);
    } else {
      return NettyApplication.run(primarySources, config, args);
    }
  }

  /**
   * 在开发模式下运行,使用HotSwapClassloader和HotSwapWatch
   */

  public static Context runDev(Class<?>[] primarySources, BootConfiguration config, String[] args) {
    if (hotSwapWatcher == null) {
      hotSwapWatcher = new HotSwapWatcher(new TioBootRestartServer());
      log.info("start hotswap watcher:{}", hotSwapWatcher);
      hotSwapWatcher.start();
    }

    EnvUtils.set(TioCoreConfigKeys.TIO_CORE_HOTSWAP_RELOAD, "true");
    // 获取自定义的classLoalder
    ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();
    log.info("new hotswap class loader:{}", hotSwapClassLoader);
    Thread.currentThread().setContextClassLoader(hotSwapClassLoader);

    // run
    Context context = NettyApplication.run(primarySources, config, args);
    TioBootArgument.init(primarySources, config, args, context, true);

    return context;

  }

}
