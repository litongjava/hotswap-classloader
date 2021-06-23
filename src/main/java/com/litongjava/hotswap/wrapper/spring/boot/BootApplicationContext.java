package com.litongjava.hotswap.wrapper.spring.boot;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:59:53 
 * @version 1.0 
 * @desc
 */
public class BootApplicationContext {

  public static ConfigurableApplicationContext context;

  /*
   * 通过get方法获取为null,实际原因不明 
   * @param context
   */
//  public static ConfigurableApplicationContext getContext() {
//    return BootApplicationContext.context;
//  }

  public static void setContext(ConfigurableApplicationContext context) {
    BootApplicationContext.context = context;
  }
}
