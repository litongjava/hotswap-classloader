package com.litongjava.hotswap.kit;

import java.lang.reflect.Method;

import com.litongjava.hotswap.classloader.HotSwapResolver;

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
    }
    return classLoaderKit;
  }

  public static HotSwapResolver getHotSwapResolver() {
    if (hotSwapResolver == null) {
      hotSwapResolver = new HotSwapResolver(getClassPathDirs());
      // 后续将此代码转移至 HotSwapResolver 中去，保持 UndertowConfig 的简洁
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
    // return isDevMode() ? getClassLoaderKit().getClassLoader() : Undertow.class.getClassLoader();
    
    /**
     * 不论是否为 devMode 都使用 HotSwapClassLoader
     * HotSwapClassLoader 添加了 isDevMode() 判断
     * 一直使用 HotSwapClassLoader 是因为为其添加了
     * 配置文件 config 目录到 class path，以便可以加载
     * 外部配置文件
     */
    return getClassLoaderKit().getClassLoader();
  }
  
}
