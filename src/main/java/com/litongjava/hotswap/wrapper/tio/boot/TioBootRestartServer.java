package com.litongjava.hotswap.wrapper.tio.boot;

import com.litongjava.hotswap.debug.Diagnostic;
import com.litongjava.hotswap.kit.HotSwapUtils;
import com.litongjava.hotswap.server.RestartServer;
import com.litongjava.tio.boot.TioApplication;
import com.litongjava.tio.boot.context.Context;

import lombok.extern.slf4j.Slf4j;

/**
 * @author create by ping-e-lee on 2021年6月23日 上午7:43:44 
 * @version 1.0 
 * @desc 不要被server的名字所迷惑,这仅仅是一个重启类
 */
@Slf4j
public class TioBootRestartServer implements RestartServer {

  public boolean isStarted() {
    return TioBootArgument.getContext().isRunning();
  }

  public void restart() {
    System.err.println("loading");
    long start = System.currentTimeMillis();
    // 关闭Spring容器,等于关闭spring,同时也等于关闭web中间件,因为web中间件在spring的容器中
    TioBootArgument.getContext().close();
    // 获取启动类和启动参数
    Class<?> clazz = TioBootArgument.getBootClazz();
    String[] args = TioBootArgument.getArgs();

    // 获取一个新的ClassLoader
    ClassLoader hotSwapClassLoader = HotSwapUtils.newClassLoader();
    if (Diagnostic.isDebug()) {
      log.info("new classLoader:{}", hotSwapClassLoader);
    }

    // 在启动新的spring-boot应用之前必须设置上下文加载器
    Thread.currentThread().setContextClassLoader(hotSwapClassLoader);

    // 启动Application
    Context context = TioApplication.run(clazz, args);
    TioBootArgument.setContext(context);
    long end = System.currentTimeMillis();
    System.err.println("Loading complete in " + (end - start) + " ms (^_^)\n");
  }

  @Override
  public void start(Class<?> primarySource, String[] args) {

  }

  @Override
  public void stop() {

  }

}
