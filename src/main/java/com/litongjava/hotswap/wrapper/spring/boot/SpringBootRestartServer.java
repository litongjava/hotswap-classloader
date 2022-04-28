package com.litongjava.hotswap.wrapper.spring.boot;

import java.text.DecimalFormat;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.litongjava.hotswap.debug.Diagnostic;
import com.litongjava.hotswap.kit.HotSwapUtils;
import com.litongjava.hotswap.server.RestartServer;

import lombok.extern.slf4j.Slf4j;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午7:43:44 
 * @version 1.0 
 * @desc 不要被server的名字所迷惑,这仅仅是一个重启类
 */
@Slf4j
public class SpringBootRestartServer implements RestartServer {

  protected DecimalFormat decimalFormat = new DecimalFormat("#.#");

  public boolean isStarted() {
    return BootArgument.getContext().isRunning();
  }

  public void restart() {
    System.err.println("loading");
    long start = System.currentTimeMillis();
    //关闭Spring容器,等于关闭spring,同时也等于关闭web中间件,因为web中间件在spring的容器中
    BootArgument.getContext().close();
    //获取启动类和启动参数
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
    
    //获取一个新的ClassLoader
    ClassLoader hotSwapClassLoader = HotSwapUtils.newClassLoader();
    if(Diagnostic.isDebug()) {
      log.info("new classLoader:{}",hotSwapClassLoader);
    }
    
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
