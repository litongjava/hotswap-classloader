package com.litongjava.hotswap.wrapper.app;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import com.litongjava.hotswap.kit.HotSwapUtils;
import com.litongjava.hotswap.server.RestartServer;
import com.litongjava.hotswap.watcher.HotSwapWatcher;

public class SimpleApp {
  protected static volatile HotSwapWatcher hotSwapWatcher;
  protected static DecimalFormat decimalFormat = new DecimalFormat("#.#");

  public static void run(String clazzName,String methodName) {
    // 获取自定义的classLoalder
    ClassLoader hotSwapClassLoader = HotSwapUtils.getClassLoader();
    // 第一次启动不需要使用自定义的类加载器,使用默认的类加载器即可
    Thread.currentThread().setContextClassLoader(hotSwapClassLoader);
    // 启动
    try {
      run(clazzName,methodName,hotSwapClassLoader);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
        | InvocationTargetException e) {
      e.printStackTrace();
    }

    RestartServer restartServer = new RestartServer() {

      @Override
      public boolean isStarted() {
        return true;
      }

      @Override
      public void restart() {
        System.err.println("loading");
        long start = System.currentTimeMillis();

        // 获取一个新的ClassLoader
        ClassLoader hotSwapClassLoader = HotSwapUtils.newClassLoader();
        // 绑定的线程上
        Thread.currentThread().setContextClassLoader(hotSwapClassLoader);
        try {
          run(clazzName, methodName, hotSwapClassLoader);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException e) {
          e.printStackTrace();
        }
        System.err.println("Loading complete in " + getTimeSpent(start) + " seconds (^_^)\n");
      }

    };

    if (hotSwapWatcher == null) {
      // 使用反射执行下面的代码
      hotSwapWatcher = new HotSwapWatcher(restartServer);
      hotSwapWatcher.start();
    }

  }

  private static void run(String className,String methodName, ClassLoader hotSwapClassLoader) throws ClassNotFoundException,
      InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      Class<?> clazz = hotSwapClassLoader.loadClass(className);
    Object callbaclObject = clazz.newInstance();
    Method method = clazz.getDeclaredMethod(methodName);
    method.setAccessible(true);
    method.invoke(callbaclObject);
  }

  protected static String getTimeSpent(long startTime) {
    float timeSpent = (System.currentTimeMillis() - startTime) / 1000F;
    return decimalFormat.format(timeSpent);
    //观看
  }
}
