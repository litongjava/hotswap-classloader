package com.litongjava.hotswap.wrapper.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.litongjava.hotswap.watcher.HotSwapWatcher;

import lombok.extern.slf4j.Slf4j;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:06:48 
 * @version 1.0 
 * @desc
 */
@Slf4j
public class SpringApplicationWrapper {
  protected static volatile HotSwapWatcher hotSwapWatcher;

  /**
   * 如果isDev=true
   * 使用自定义的HostSwapClassLoader启动SpringApplication
   * 如果isDev=false
   * 使用默认的加载
   * @param clazz
   * @param args
   * 
   */
  public static ConfigurableApplicationContext run(Class<?> primarySource, String[] args, Boolean isDev) {
    if (isDev) {
      return runDev(primarySource, args);
    } else {
      return SpringApplication.run(primarySource, args);
    }
  }

  /**
   * 支持可变参数
   * @param isDev
   * @param primarySource
   * @param args
   * @return
   */
  public static ConfigurableApplicationContext run(Boolean isDev, Class<?> primarySource, String... args) {
    return run(primarySource, args, isDev);
  }

  /**
   * 在开放模式下运行,使用HotSwapClassloader和HotSwapWatch
   * @param clazz
   * @param args
   * @param isDev
   * @return
   */
  private static ConfigurableApplicationContext runDev(Class<?> primarySource, String[] args) {
    // 获取自定义的classLoalder
//    ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();
//    log.info("hotSwapClassLoader:{}", hotSwapClassLoader);

    /**
     * 设置线程上下文类加载器,因为Spring Boot在加载一些类的使用的就是这个线程上下文类加载器,
     * 而默认的线程上下文类加载器等于ClassLoader.getSystemClassLoader(),
     * 如果两个类加载器加载同一个类文件就会认为是两个类,会无法赋值,Spring Boot也起不来
     */
    //Thread.currentThread().setContextClassLoader(hotSwapClassLoader);

    if (hotSwapWatcher == null) {
      // 使用反射执行下面的代码
      log.info("start hotSwapWatcher");
      hotSwapWatcher = new HotSwapWatcher(new SpringBootRestartServer());
      hotSwapWatcher.start();
    }

    ConfigurableApplicationContext context = SpringApplication.run(primarySource, args);
    BootArgument.init(primarySource, args, context, true);
    return context;
  }
}
