package com.litongjava.hotswap.wrapper.tio.boot;

import com.litongjava.tio.boot.context.Context;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:48:16 
 * @version 1.0 
 * @desc 保存tio-boot启动时的启动参数
 */
public class TioBootArgument {

  private static String[] args;
  private static Class<?> BootClazz;
  private static Context context;
  private static Boolean isDev = false;

  public static void setArgs(String[] args) {
    TioBootArgument.args = args;
  }

  public static String[] getArgs() {
    return TioBootArgument.args;
  }

  public static void setBootClazz(Class<?> clazz) {
    TioBootArgument.BootClazz = clazz;
  }

  public static Class<?> getBootClazz() {
    return BootClazz;
  }

  public static void setContext(Context context) {
    TioBootArgument.context = context;
  }

  public static Context getContext() {
    return TioBootArgument.context;
  }

  public boolean getIsDev() {
    return TioBootArgument.isDev;
  }

  public static void init(Class<?> clazz, String[] args, Context context, Boolean isDev) {
    TioBootArgument.args = args;
    TioBootArgument.BootClazz = clazz;
    TioBootArgument.context = context;
    TioBootArgument.isDev = isDev;
  }

}
