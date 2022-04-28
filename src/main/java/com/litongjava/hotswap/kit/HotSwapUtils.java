package com.litongjava.hotswap.kit;

import com.litongjava.hotswap.debug.Diagnostic;
import com.litongjava.hotswap.watcher.HotSwapResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HotSwapUtils {

  public static String[] args;

  protected static HotSwapResolver hotSwapResolver;
  protected static ClassLoaderKit classLoaderKit;
  protected static String[] classPathDirs; // 存放 .class 文件的目录
  protected static String hotSwapClassPrefix = null;

  public static ClassLoader replaceClassLoader() {
    return getClassLoaderKit().replaceClassLoader();
  }

  protected static ClassLoaderKit getClassLoaderKit() {
    if (classLoaderKit == null) {
      classLoaderKit = new ClassLoaderKit(HotSwapUtils.class.getClassLoader(), getHotSwapResolver());
      if (Diagnostic.isDebug()) {
        log.info("create new kit:{}", classLoaderKit);
      }
    }
    return classLoaderKit;
  }

  public static HotSwapResolver getHotSwapResolver() {
    if (hotSwapResolver == null) {
      hotSwapResolver = new HotSwapResolver(getClassPathDirs());
      if (hotSwapClassPrefix != null) {
        for (String prefix : hotSwapClassPrefix.split(",")) {
          if (isEmpty(prefix)) {
            hotSwapResolver.addHotSwapClassPrefix(prefix);
          }
        }
      }
    }
    return hotSwapResolver;
  }

  private static boolean isEmpty(String str) {
    return (str == null || "".equals(str));
  }

  /**
   * 获取存放 .class 文件的所有 classPath 目录，绝大部分场景下只有一个目录
   */
  public static String[] getClassPathDirs() {
    if (classPathDirs == null) {
      classPathDirs = UndertowKit.getClassPathDirs();
    }
    return classPathDirs;
  }

  public static ClassLoader getClassLoader() {
    //使用ClassLoaderKit()获取HotswapClassLoader
    return getClassLoaderKit().getClassLoader();
  }

  public static ClassLoader newClassLoader() {
    classLoaderKit = new ClassLoaderKit(HotSwapUtils.class.getClassLoader(), getHotSwapResolver());
    return classLoaderKit.getClassLoader();
  }

}
