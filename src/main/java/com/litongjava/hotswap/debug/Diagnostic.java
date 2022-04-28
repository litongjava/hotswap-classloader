package com.litongjava.hotswap.debug;

/**
 * @author Ping E Lee
 *
 */
public class Diagnostic {
  // 配置为true显示调试信息,反之同理之
  private static boolean debug = false;

  public static boolean isDebug() {
    return debug;
  }

  public static void setDebug(boolean debug) {
    Diagnostic.debug = debug;
  }
}
