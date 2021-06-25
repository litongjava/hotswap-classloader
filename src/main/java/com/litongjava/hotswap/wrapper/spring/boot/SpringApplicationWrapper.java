package com.litongjava.hotswap.wrapper.spring.boot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.litongjava.hotswap.classloader.HotSwapWatcher;
import com.litongjava.hotswap.kit.HotSwapUtils;
import com.litongjava.hotswap.kit.ReflectionUtils;
import com.litongjava.hotswap.server.RestartServer;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午8:06:48 
 * @version 1.0 
 * @desc
 */
//@Slf4j
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
    return run(primarySource, args, isDev, isDev);

  }

  /**
   * 
   * @param primarySource
   * @param args
   * @param isEnableClassLoader 开启自定义类加载器
   * @param isEnableWatcher 开启class检测服务
   */
  public static ConfigurableApplicationContext run(Class<?> primarySource, String[] args, boolean isEnableClassLoader, boolean isEnableWatcher) {
    if (isEnableClassLoader) {
      /**
       * Method runMethod = wrapperClazz.getMethod("runDev",Class.class,String[].class);
       * 出现下面的异常,原因不明,没办法,只能手动了 
       * java.lang.NoSuchMethodException:
       */
      return runDev(primarySource, args,isEnableClassLoader,isEnableWatcher);

    } else {
      return SpringApplication.run(primarySource, args);
    }
  }

  /**
   * 在开放模式下运行,使用HotSwapClassloader和HotSwapWatch
   * @param clazz
   * @param args
   * @param isDev
   * @return
   */
  private static ConfigurableApplicationContext runDev(Class<?> primarySource, String[] args,Boolean isEnableClassLoader,Boolean isEnableWatcher) {

    /*
     * 获取自定义的classLoalder
     */
    ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();

    /**
     * 设置线程上下文类加载器,因为Spring Boot
     * 在加载一些类的使用的就是这个线程上下文类加载器,而默认的线程上下文类加载器等于ClassLoader.getSystemClassLoader(),
     * 如果两个类加载器加载同一个类文件就会认为是两个类,会无法赋值,Spring Boot也起不来
     */
    Thread.currentThread().setContextClassLoader(hotSwapClassLoader);

    /**
     * 使用方向执行下面的方法
     * ConfigurableApplicationContext context = SpringApplication.run(clazz, args);
     */

    String className = SpringApplication.class.getName();
    Method runMethod = ReflectionUtils.getMethod(hotSwapClassLoader, className, "run", Class.class, String[].class);
    ConfigurableApplicationContext context = (ConfigurableApplicationContext) ReflectionUtils.invoke(runMethod, primarySource, args);
    /**
     * BootArgument.init(clazz, args, context,isDev); 
     */
    String bootArgClazzName = BootArgument.class.getName();
    Method initMethod = ReflectionUtils.getMethod(hotSwapClassLoader, bootArgClazzName, "init", Class.class, String[].class,
        ConfigurableApplicationContext.class, Boolean.class);
    ReflectionUtils.invoke(initMethod, primarySource, args, context, isEnableClassLoader);
    /**
     * 添加hotSwapWatcher,使用反射执行下面的方法
     * if ( hotSwapWatcher == null) {
          hotSwapWatcher = new HotSwapWatcher(new SpringBootRestartServer());
          hotSwapWatcher.start();
       }
     */
    if (isEnableWatcher && hotSwapWatcher == null ) {

      String restartServerClsssName = RestartServer.class.getName();
      Class<?> restartServerClszz = ReflectionUtils.loadClass(hotSwapClassLoader, restartServerClsssName);

      String hotSwapWatcherClazzName = HotSwapWatcher.class.getName();
      Class<?> hotSwapWatcherClszz = ReflectionUtils.loadClass(hotSwapClassLoader, hotSwapWatcherClazzName);
      Constructor<?> declaredConstructor = ReflectionUtils.getDeclaredConstructor(hotSwapWatcherClszz, restartServerClszz);

      String bootRestartServerClsssName = SpringBootRestartServer.class.getName();
      Class<?> bootRestartServerClszz = ReflectionUtils.loadClass(hotSwapClassLoader, bootRestartServerClsssName);
      Object bootRestartServerObj = ReflectionUtils.newInstance(bootRestartServerClszz);

      Object hotSwapWatcherObj = ReflectionUtils.newInstance(declaredConstructor, bootRestartServerObj);

      Method startMethod = ReflectionUtils.getMethod(hotSwapWatcherClszz, "start");

      ReflectionUtils.invokeForOjbect(startMethod, hotSwapWatcherObj);
    }
    return context;

  }

}
