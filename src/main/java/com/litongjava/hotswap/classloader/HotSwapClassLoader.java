package com.litongjava.hotswap.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * HotSwapClassLoader
 */
public class HotSwapClassLoader extends URLClassLoader {
  
  final ClassLoader parent;
  protected HotSwapResolver hotSwapResolver;
  
  static {
    registerAsParallelCapable();
  }
  
  public HotSwapClassLoader(URL[] urls, ClassLoader parent, HotSwapResolver hotSwapResolver) {
    super(urls, parent);
    this.parent = parent;
    this.hotSwapResolver = hotSwapResolver;
  }
  
  /**
   * 全程避免使用 super.loadClass(...)，以免被 parent 加载到不该加载的类
   */
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    /**
     * 生产环境所有类文件统一加载方式，不再进行额外判断
     * 生产环境避免使用 parent 加载，是为了能从额外添加的
     * config 目上录下面加载配置文件
     */
//    if (! UndertowConfig.isDevMode()) {
//      /* 下面的代码开启后抛异常：ClassNotFoundException: java.lang.Object
//       * 原因是系统类文件必须要由 parent 加载
//      Class<?> c = super.findClass(name);
//      if (c != null) {
//        if (resolve) {
//          resolveClass(c);
//        }
//        return c;
//      } */
//      /**
//       * 不能启动 return parent.loadClass(name); 因为 parent 没有添加
//       * config 到 class path 中去，无法实现外部配置文件的加载
//       * 
//       * 而 super.loadClass(...) 是 HotSwapClassLoader 自己在
//       * 使用继承过来的方法在加载，HotSwapClassLader 已添加过 config
//       * 到 class path
//       */
//      return super.loadClass(name, resolve);
//    }
    
    // ---------
    
    synchronized (getClassLoadingLock(name)) {
      // First, check if the class has already been loaded
      Class<?> c = findLoadedClass(name);
      if (c != null) {
        return c;
      }
      
      // ---------
      
      if (hotSwapResolver.isSystemClass(name)) {
        return parent.loadClass(name);
      }
      
      // ---------
      
      if (hotSwapResolver.isHotSwapClass(name)) {
        /**
         * 使用 "本 ClassLoader" 加载类文件
         * 注意：super.loadClass(...) 会触发 parent 加载，绝对不能使用
         */
        c = super.findClass(name);
        if (c != null) {
          if (resolve) {
            resolveClass(c);
          }
          return c;
        }
        
        // throw new ClassNotFoundException(name);  // TODO
      }
      
      // ---------
      
      return parent.loadClass(name);
    }
  }
}