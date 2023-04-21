package com.litongjava.hotswap.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * HotSwapResolver
 */
public class HotSwapResolver {

  protected String[] classPathDirs;

  protected static String[] systemClassPrefix = { "java.", "javax.", "sun.", // 支持 IDEA
      "com.sun.",
      // "jdk.",
      // "org.xml.",
      // "org.w3c.",

      // "io.undertow.",
      // "org.xnio.",

      "com.jfinal.server.undertow." // undertow server 项目自身

      // "org.apache.jasper.", // 支持 jsp，不影响 org.apache.shiro
      // "org.apache.taglibs.", // 支持 jsp，不影响 org.apache.shiro
      // "org.glassfish.jsp.", // 支持 jsp
      // "org.slf4j." // 支持slf4j
  };

  /**
   * 添加net.sf.ehcache.出现下面的错误
   * Caused by: java.lang.LinkageError: loader constraint violation: loader (instance of sun/misc/Launcher$AppClassLoader) previously initiated loading for a different type with name "net/sf/ehcache/CacheManager"
   * 
   * 添加org.quartz.出现下面的错误
   * Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'org.quartz.Scheduler' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}
   */
  protected static String[] hotSwapClassPrefix = {};

  public HotSwapResolver(String[] classPathDirs) {
    // 不必判断 length == 0，因为在打包后的生产环境获取到的 length 可以为 0
    // if (classPathDirs == null /* || classPathDirs.length == 0*/) {
    // throw new IllegalArgumentException("classPathDirs can not be null");
    // }

    if (classPathDirs != null) {
      this.classPathDirs = classPathDirs;
    } else {
      this.classPathDirs = new String[0];
    }
  }

  /**
   * 判断是否为系统类文件，系统类文件无条件使用 parent 类加载器加载
   */
  public boolean isSystemClass(String className) {
    for (String s : systemClassPrefix) {
      if (className.startsWith(s)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 判断是否为热加载类文件，热加载类文件无条件使用 HotSwapClassLoader 加载
   * 
   * 热加载类文件满足两个条件：
   * 1：通过 hotSwapClassPrefix 指定的类文件
   * 2：在 class path 目录下能找到的 .class 文件
   */
  public boolean isHotSwapClass(String className) {
    for (String s : hotSwapClassPrefix) {
      if (className.startsWith(s)) {
        return true;
      }
    }

    /**
     * 所有 classPath 目录下的所有 .class 文件需要热加载
     */
    if (findClassInClassPathDirs(className)) {
      return true;
    }

    return false;
  }

  protected boolean findClassInClassPathDirs(String className) {
    String fileName = className.replace('.', '/').concat(".class");

    if (classPathDirs.length == 1) {
      if (findFile(classPathDirs[0], fileName)) {
        return true;
      }
    } else {
      for (String dir : classPathDirs) {
        if (findFile(dir, fileName)) {
          return true;
        }
      }
    }

    return false;
  }

  protected boolean findFile(String filePath, String fileName) {
    File file = new File(filePath + fileName);
    return file.isFile();
  }

  /**
   * 添加系统类前缀，系统类由系统类加载器进行加载
   */
  public static synchronized void addSystemClassPrefix(String... prefixs) {
    List<String> list = new ArrayList<>();
    for (String s : systemClassPrefix) {
      list.add(s);
    }
    for (String prefix : prefixs) {
      list.add(prefix.trim());
    }
    
    systemClassPrefix = list.toArray(new String[list.size()]);
  }

  /**
   * 添加需要热加载的类前缀，由 HotSwapClassLoader 加载
   * 
   * 重要：在热加载过后，如果出现类型转换异常，找到无法转换的类
   *      调用本方法添加相关前缀即可解决
   */
  public static synchronized void addHotSwapClassPrefix(String... prefixs) {
    List<String> list = new ArrayList<>();
    for (String s : hotSwapClassPrefix) {
      list.add(s);
    }
    for (String prefix : prefixs) {
      list.add(prefix.trim());
    }
    hotSwapClassPrefix = list.toArray(new String[list.size()]);
  }
}