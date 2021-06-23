package com.litongjava.hotswap.kit;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class UndertowKit {

  private static String[] classPathDirs = null;

  /**
   * 该方法起始用于 HotSwapResolver.isHotSwapClass() 下面的 findClassInClassPathDirs()
   * 用于判断当前被加载的类是不是处在 class path 之下，从而判断该类是不是需要被 HotSwapClassLoader
   * 进行热加载
   * 
   * 注意：返回值中的 path 全都以 '/' 或 '\\' 字符结尾，因为该方法一开始的用途需要这个结尾字符
   * 
   * 
   * 后来也被辅助用于 buildDeployMode()、buildWebRootPath()、buildRootClass() 这三个方法之中
   * 1：buildDeployMode() 通过判断是否存在以 "classes" 结尾的 class path 来确定是否处于部署模式
   * 2：buildWebRootPath() 判断同上，当处于部署模式时，指向 web 资源文件目录：APP_BASE/webapp
   * 3：buildRootClass() 判断同上，当处于部署模式时，指向用于存放配置文件的目录：APP_BASE/config
   */
  public static String[] getClassPathDirs() {
    if (classPathDirs == null) {
      classPathDirs = buildClassPathDirs();
    }
    return classPathDirs;
  }

  private static String[] buildClassPathDirs() {
    List<String> list = new ArrayList<>();
    String[] classPathArray = System.getProperty("java.class.path").split(File.pathSeparator);
    for (String classPath : classPathArray) {
      classPath = classPath.trim();

      if (classPath.startsWith("./")) {
        classPath = classPath.substring(2);
      }

      File file = new File(classPath);
      if (file.exists() && file.isDirectory()) {
        // if (!classPath.endsWith("/") && !classPath.endsWith("\\")) {
        if (!classPath.endsWith(File.separator)) {
          classPath = classPath + File.separator; // append postfix char "/"
        }

        list.add(classPath);
      }
    }
    return list.toArray(new String[list.size()]);
  }

  public static boolean notAvailablePort(int port) {
    return !isAvailablePort(port);
  }

  public static boolean isAvailablePort(int port) {
    if (port <= 0) {
      throw new IllegalArgumentException("Invalid start port: " + port);
    }

    ServerSocket ss = null;
    DatagramSocket ds = null;
    try {
      ss = new ServerSocket(port);
      ss.setReuseAddress(true);
      ds = new DatagramSocket(port);
      ds.setReuseAddress(true);
      return true;
    } catch (IOException e) {
      doNothing(e);
    } finally {
      closeQuietly(ds);
      closeQuietly(ss);
    }
    return false;
  }

  public static void closeQuietly(java.io.Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException e) {
      // should not be thrown, just detect port available.
      doNothing(e);
    }
  }

  public static void doNothing(Throwable e) {

  }

  // ----------------------------------------------------------------

  private static Boolean deployMode = null;

  public static boolean isDeployMode() {
    if (deployMode == null) {
      deployMode = buildDeployMode();
    }
    return deployMode;
  }

  public static boolean notDeployMode() {
    return !isDeployMode();
  }

  /**
   * 非部署模式下，可以获取到 target/classes
   * 
   * 有了这个方法, PathKitExt 中的有关 UndertowKit.getClassPathDirs()
   * 判断可以使用这个方法了，不用再重复写代码了
   */
  private static boolean buildDeployMode() {
    String[] classPathDirs = UndertowKit.getClassPathDirs();
    if (classPathDirs != null && classPathDirs.length > 0) {
      for (String path : classPathDirs) {
        if (path != null) {
          path = PathKitExt.removeSlashEnd(path);
          if (path.endsWith("classes")) {
            return false;
          }
        }
      }
    }

    return true;
  }

  /**
   * 先得到定位路径 PathKitExt.getLocationPath()，然后判断这个目录是否
   * 以 "lib" 结尾，如果是则返回上级目录，否则被认为是 fatjar 直接返回定位路径的值
   * 
   * 注意：此方法仅在 deployMode 下才有意义
   */
  public String getAppBasePath() {
    String path = PathKitExt.getLocationPath();
    if (path.endsWith(File.separatorChar + "lib")) {
      path = path.substring(0, path.lastIndexOf(File.separatorChar));
    }
    return path;
  }
}
