package com.litongjava.hotswap.wrapper.spring.boot;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:48:16 
 * @version 1.0 
 * @desc 保存spring-boot启动时的启动参数
 */
public class SpringBootArgument {

  private static String[] args;
  private static Class<?> BootClazz;
  private static ConfigurableApplicationContext context;
  private static Boolean isDev = false;

  public static void setArgs(String[] args) {
    SpringBootArgument.args = args;
  }

  public static String[] getArgs() {
    return SpringBootArgument.args;
  }

  public static void setBootClazz(Class<?> clazz) {
    SpringBootArgument.BootClazz = clazz;
  }

  public static Class<?> getBootClazz() {
    return BootClazz;
  }

  public static void setContext(ConfigurableApplicationContext context) {
    SpringBootArgument.context=context;
  }
  public static ConfigurableApplicationContext getContext() {
    return SpringBootArgument.context;
  }

  public boolean getIsDev() {
    return SpringBootArgument.isDev;
  }

  public static void init(Class<?> clazz, String[] args, ConfigurableApplicationContext context, Boolean isDev) {
    SpringBootArgument.args = args;
    SpringBootArgument.BootClazz = clazz;
    SpringBootArgument.context = context;
    SpringBootArgument.isDev = isDev;
  }

}
