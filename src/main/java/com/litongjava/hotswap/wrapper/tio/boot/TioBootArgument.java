package com.litongjava.hotswap.wrapper.tio.boot;

import com.litongjava.tio.boot.context.Context;
import com.litongjava.tio.boot.context.TioBootConfiguration;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:48:16 
 * @version 1.0 
 * @desc 保存tio-boot启动时的启动参数
 */
public class TioBootArgument {

  public static Class<?>[] primarySources;
  public static TioBootConfiguration config;
  public static String[] args;
  public static Context context;
  public static boolean isDev = false;

  public static void init(Class<?>[] primarySources, TioBootConfiguration config, String[] args, Context context,
      boolean isDev) {
    TioBootArgument.primarySources = primarySources;
    TioBootArgument.config = config;
    TioBootArgument.args = args;
    TioBootArgument.context = context;
    TioBootArgument.isDev = isDev;
  }

}
