package com.litongjava.hotswap.classloader;

import java.net.URL;
import java.net.URLClassLoader;

import com.litongjava.hotswap.debug.Diagnostic;
import com.litongjava.hotswap.watcher.HotSwapResolver;

import lombok.extern.slf4j.Slf4j;

/**
 * HotSwapClassLoader
 */
@Slf4j
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
    synchronized (getClassLoadingLock(name)) {
      // First, check if the class has already been loaded
      Class<?> c = findLoadedClass(name);
      if (Diagnostic.isDebug()) {
        System.out.println(name + "," + c);
      }
      if (c != null) {
        return c;
      }

      // 如果是系统类,使用父类加载器
      if (hotSwapResolver.isSystemClass(name)) {
        return parent.loadClass(name);
      }

      // 如果是Hotswap类使用本类加载器
      if (hotSwapResolver.isHotSwapClass(name)) {
        if (Diagnostic.isDebug()) {
          log.info("isHotSwapClass:{}", name);
        }

        /**
         * 使用 "本 ClassLoader" 加载类文件
         * 注意：super.loadClass(...) 会触发 parent 加载，绝对不能使用
         */
        c = super.findClass(name);

        if (c != null) {
          if (resolve) {
            if (Diagnostic.isDebug()) {
              log.info("resolveClass:{}", name);
            }
            resolveClass(c);
          }
          return c;
        }

        // throw new ClassNotFoundException(name); // TODO
      }

      // 其他类使用父类加载器加载
      return parent.loadClass(name);
    }
  }
}