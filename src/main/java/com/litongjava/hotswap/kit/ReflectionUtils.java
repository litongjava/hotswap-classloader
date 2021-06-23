package com.litongjava.hotswap.kit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午11:28:05 
 * @version 1.0 
 * @desc
 */
public class ReflectionUtils {

  /**
   * 使用指定的加载器加载类
   * @param hotSwapClassLoader
   * @param clazzName
   * @return
   */
  public static Class<?> loadClass(ClassLoader hotSwapClassLoader, String clazzName) {
    try {
      return hotSwapClassLoader.loadClass(clazzName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 返回指定新形参的构造函数
   * @param clazz
   * @param parameterTypes
   * @return
   */
  public static Constructor<?> getDeclaredConstructor(Class<?> clazz, Class<?>... parameterTypes) {
    try {
      return clazz.getDeclaredConstructor(parameterTypes);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 创建对象
   * @param restartServerClszz
   * @return
   */
  public static Object newInstance(Class<?> clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 指定构造器,指定参数,创建对象
   * @param declaredConstructor
   * @param initargs
   * @return
   */
  public static Object newInstance(Constructor<?> declaredConstructor, Object... initargs) {
    try {
      return declaredConstructor.newInstance(initargs);
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 
   * @param clazz
   * @param args
   * @param hotSwapClassLoader
   * @param className
   * @return
   */
  public static Method getMethod(ClassLoader hotSwapClassLoader, String className, String methodName, Class<?>... parameterTypes) {
    try {
      Class<?> clazz = hotSwapClassLoader.loadClass(className);
      return clazz.getMethod(methodName, parameterTypes);

    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 获取方法
   * @param clazz
   * @param methodName
   * @return
   */
  public static Method getMethod(Class<?> clazz, String methodName) {
    try {
      return clazz.getMethod(methodName);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 执行方法
   * @param clazz
   * @param args
   * @param runMethod
   * @return
   */
  public static Object invoke(Method method, Object... args) {
    try {
      return method.invoke(null, args);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  /**
   * 执行方法
   * @param method
   * @param obj
   * @param args
   * @return
   */
  public static Object invokeForOjbect(Method method, Object obj) {
    try {
      return method.invoke(obj);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  

}
