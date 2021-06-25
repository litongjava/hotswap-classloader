package com.litongjava.hotswap.wrapper.spring.boot;

import java.text.DecimalFormat;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.litongjava.hotswap.kit.HotSwapUtils;
import com.litongjava.hotswap.server.RestartServer;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午7:43:44 
 * @version 1.0 
 * @desc 不要被server的名字所迷惑,这仅仅是一个重启类
 */
public class SpringBootRestartServer implements RestartServer {

  protected DecimalFormat decimalFormat = new DecimalFormat("#.#");

  public boolean isStarted() {
    return BootArgument.getContext().isRunning();
  }

  public void restart() {
    long start = System.currentTimeMillis();
    BootArgument.getContext().close();
//    SpringApplicationWrapper.run(BootArgument.getBootClazz(), BootArgument.getArgs(), true);

    Class<?> clazz = BootArgument.getBootClazz();
    String[] args = BootArgument.getArgs();
    /**
     * 获取自定义的classLoalder
     * 测试失败
     */
//    ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();
    //获取一个新的classLoader
    /**
     * 通过反射执行,获取一个新的classLoader,否则修改文件保存之后不会生效
     * ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();
     * 测试失败
     */
//    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//    Class<?> hotSwapUtilsClzz=null;
//    try {
//      hotSwapUtilsClzz = contextClassLoader.loadClass(HotSwapUtils.class.getName());
//    } catch (ClassNotFoundException e) {
//      e.printStackTrace();
//    }
//    Method getClassLoaderMethod = ReflectionUtils.getMethod(hotSwapUtilsClzz, "getClassLoader");
//    ClassLoader hotSwapClassLoader =(ClassLoader) ReflectionUtils.invoke(getClassLoaderMethod);
    
    ClassLoader hotSwapClassLoader = HotSwapUtils.newClassLoader();
    System.out.println("new classLoader:"+hotSwapClassLoader);
    /**
     * 在启动新的spring-boot应用之前必须设置上下文加载器
     */
    Thread.currentThread().setContextClassLoader(hotSwapClassLoader);

    /**
     * 启动SpringApplication
     */
    ConfigurableApplicationContext context = SpringApplication.run(clazz, args);
    BootArgument.setContext(context);


    
    System.err.println("Loading complete in " + getTimeSpent(start) + " seconds (^_^)\n");
  }

  protected String getTimeSpent(long startTime) {
    float timeSpent = (System.currentTimeMillis() - startTime) / 1000F;
    return decimalFormat.format(timeSpent);
  }

}
