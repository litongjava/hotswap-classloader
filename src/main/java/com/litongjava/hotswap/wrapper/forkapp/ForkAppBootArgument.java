package com.litongjava.hotswap.wrapper.forkapp;

import com.litongjava.context.Context;

/**
 * @desc 启动时的启动参数
 */
public class ForkAppBootArgument {

  private static String[] args;
  private static Class<?> BootClazz;
  private static Context context;
  private static Boolean isDev = false;

  public static void setArgs(String[] args) {
    ForkAppBootArgument.args = args;
  }

  public static String[] getArgs() {
    return ForkAppBootArgument.args;
  }

  public static void setBootClazz(Class<?> clazz) {
    ForkAppBootArgument.BootClazz = clazz;
  }

  public static Class<?> getBootClazz() {
    return BootClazz;
  }

  public static void setContext(Context context) {
    ForkAppBootArgument.context = context;
  }

  public static Context getContext() {
    return ForkAppBootArgument.context;
  }

  public boolean getIsDev() {
    return ForkAppBootArgument.isDev;
  }

  public static void init(Class<?> clazz, String[] args, Boolean isDev) {
    ForkAppBootArgument.args = args;
    ForkAppBootArgument.BootClazz = clazz;
    ForkAppBootArgument.isDev = isDev;
  }

  public static void init(Class<?> clazz, String[] args, Context context, Boolean isDev) {
    ForkAppBootArgument.args = args;
    ForkAppBootArgument.BootClazz = clazz;
    ForkAppBootArgument.context = context;
    ForkAppBootArgument.isDev = isDev;
  }

}
