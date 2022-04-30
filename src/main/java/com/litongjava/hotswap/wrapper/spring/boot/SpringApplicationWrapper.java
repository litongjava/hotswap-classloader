package com.litongjava.hotswap.wrapper.spring.boot;

import java.io.IOException;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.PropertiesLoaderUtils;

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
   * 整合spring-boot 1.2.x
   * 读取配置文件 environment.properties
   * @return
   */
  public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
    String mode = null;
    try {
      Properties properties = PropertiesLoaderUtils.loadAllProperties("config.properties");
      mode = properties.getProperty("mode");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return run(primarySource, args, "dev".equals(mode));
  }

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
    // ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();
    // log.info("hotSwapClassLoader:{}", hotSwapClassLoader);

    
    // 第一次启动不需要使用自定义的类加载器,使用默认的类加载器即可
    // Thread.currentThread().setContextClassLoader(hotSwapClassLoader);
    ConfigurableApplicationContext context = SpringApplication.run(primarySource, args);
    SpringBootArgument.init(primarySource, args, context, true);
    
    //context启动成功之后再启动hotswapwathcer
    if (hotSwapWatcher == null) {
      // 使用反射执行下面的代码
      log.info("start hotSwapWatcher");
      hotSwapWatcher = new HotSwapWatcher(new SpringBootRestartServer());
      hotSwapWatcher.start();
    }
    return context;
  }
}
