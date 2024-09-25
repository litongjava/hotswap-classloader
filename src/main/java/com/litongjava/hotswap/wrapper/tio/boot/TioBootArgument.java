package com.litongjava.hotswap.wrapper.tio.boot;

import com.litongjava.context.BootConfiguration;
import com.litongjava.context.Context;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:48:16 
 * @version 1.0 
 * @desc 保存tio-boot启动时的启动参数
 */
public class TioBootArgument {

  public static Class<?>[] primarySources;
  public static BootConfiguration config;
  public static String[] args;
  public static Context context;
  public static boolean isDev = false;

  public static void init(Class<?>[] primarySources, BootConfiguration config, String[] args, Context context,
      boolean isDev) {
    TioBootArgument.primarySources = primarySources;
    TioBootArgument.config = config;
    TioBootArgument.args = args;
    TioBootArgument.context = context;
    TioBootArgument.isDev = isDev;
  }

}
