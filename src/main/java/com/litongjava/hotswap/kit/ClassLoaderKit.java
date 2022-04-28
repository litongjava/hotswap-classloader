package com.litongjava.hotswap.kit;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.litongjava.hotswap.classloader.HotSwapClassLoader;
import com.litongjava.hotswap.watcher.HotSwapResolver;

/**
 * ClassLoaderKit
 */
public class ClassLoaderKit {
  
  protected URL[] classPathUrls;
  protected ClassLoader parentClassLoader;
  protected HotSwapResolver hotSwapResolver;
  protected ClassLoader currentClassLoader;
  
  /**
   * 在 deployMode 下 java 命令行需要添加 ${APP_BASE_PATH}/config 到 class path
   * 才可以启动项目，而 System.getProperty("java.class.path") 也可以读到该目录
   * 从而造成 config 目录被添加两次，虽然对使用没有任何影响，但为了追求完美添加此控制
   * 变量，避免该目录被添加两次
   */
  protected String configClassPathDirectory = null;
  
  public ClassLoaderKit(ClassLoader parentClassLoader, HotSwapResolver hotSwapResolver) {
    this.classPathUrls = buildClassPathUrls();
    this.parentClassLoader = parentClassLoader;
    this.hotSwapResolver = hotSwapResolver;
    //创建HotSwapClassLoader
    this.currentClassLoader = new HotSwapClassLoader(classPathUrls, parentClassLoader, hotSwapResolver);
  }
  
  public ClassLoader getClassLoader() {
    return currentClassLoader;
  }
  
  public ClassLoader replaceClassLoader() {
    currentClassLoader = new HotSwapClassLoader(classPathUrls, parentClassLoader, hotSwapResolver);
    return currentClassLoader;
  }
  
  protected URL[] buildClassPathUrls() {
    List<URL> urlList = new ArrayList<>();
    
    addConfigClassPath(urlList);
    
    /**
     * unix/linux 与 widnows 下 pathSeparator 分别为 ':' 与 ';'
     * TODO 需测试被 split 后的元素中是否还包含了逗号 ','
     */
    String[] classPathArray = System.getProperty("java.class.path").split(File.pathSeparator);
    for(String classPath : classPathArray) {
      if (configClassPathDirectory != null && configClassPathDirectory.equals(classPath)) {
        continue ;
      }
      
      if (classPath.startsWith("./")) {
        classPath = classPath.substring(2);
      }
      
      File file = new File(classPath);
      if (file.exists()) {
        try {
          urlList.add(file.toURI().toURL());
        } catch (MalformedURLException e) {
          throw new RuntimeException(e);
        }
      }
    }
    
    return urlList.toArray(new URL[urlList.size()]);
  }
  
  /**
   * 在部署环境下添加配置文件目录为 class path，约定部署环境下的配置文件放在 APP_BASE/config 目录下
   * 
   * TODO 要测试 config 配置在最前方，是不是被优先加载了，而且要测试 dev_config.txt 在 jar 包内以及 pro_config.txt
   *      在 jar 包外的混合加载方式是否生效
   *      还要测试 jar 包内外同时存在  dev_config.txt 时优先加载的哪个
   *      希望是：优先加载 config 目录下面的，因为便于手工修改
   *      
   *      约定：配置文件目录为 config，并且在 jar 包之外，而且 fatjar 形式的配置文件也在 config 目录下
   */
  protected void addConfigClassPath(List<URL> urlList) {
    
    
    /**
     * 先假定是 fatjar 情况，寻找当前 jar 包目录下面的 config 目录，locationPath 为当前 jar 包所以目录
     */
    String locationPath = PathKitExt.getLocationPath();
    addConfigClassPathDirectory(locationPath, urlList);
    if (urlList.size() > 0) {
      return ;    // 只允许添加一个 config 目录
    }
    
    // ---------
    
    /**
     * 非 fatjar 情况下 locationPath 处在 APP_BASE/lib 目录下
     * 此时约定 config 目录与 lib 在同级目录，需要将判断 locationPath
     * 的上级目录下是否存在 config 目录再来添加
     * 
     * 注掉这里，允许 jar 包不放在 lib 下，兼容未来可能的情况
    if ( ! locationPath.endsWith(File.separator + "lib")) {
      return ;
    } */
    
    // String path = locationPath.substring(0, locationPath.lastIndexOf(File.separatorChar));
    int index = locationPath.lastIndexOf(File.separatorChar);  // 支持根目录下运行 jar 文件
    String path = index > 0 ? locationPath.substring(0, index) : locationPath;
    
    addConfigClassPathDirectory(path, urlList);
  }
  
  private void addConfigClassPathDirectory(String path, List<URL> urlList) {
    path = path + File.separator + "config";
    
    File file = new File(path);
    if (file.exists() && file.isDirectory()) {
      try {
        urlList.add(file.toURI().toURL());
        configClassPathDirectory = path;
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }
}