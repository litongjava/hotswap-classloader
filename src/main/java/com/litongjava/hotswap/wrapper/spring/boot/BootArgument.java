package com.litongjava.hotswap.wrapper.spring.boot;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:48:16 
 * @version 1.0 
 * @desc 保存spring-boot启动时的启动参数
 */
public class BootArgument {

  private static String[] args;
  private static Class<?> BootClazz;
  private static ConfigurableApplicationContext context;
  private static Boolean isDev = false;

  public static void setArgs(String[] args) {
    BootArgument.args = args;
  }

  public static String[] getArgs() {
    return BootArgument.args;
  }

  public static void setBootClazz(Class<?> clazz) {
    BootArgument.BootClazz = clazz;
  }

  public static Class<?> getBootClazz() {
    return BootClazz;
  }

  public static void setContext(ConfigurableApplicationContext context) {
    BootArgument.context=context;
  }
  public static ConfigurableApplicationContext getContext() {
    return BootArgument.context;
  }

  public boolean getIsDev() {
    return BootArgument.isDev;
  }

  public static void init(Class<?> clazz, String[] args, ConfigurableApplicationContext context, Boolean isDev) {
    BootArgument.args = args;
    BootArgument.BootClazz = clazz;
    BootArgument.context = context;
    BootArgument.isDev = isDev;
  }

}
