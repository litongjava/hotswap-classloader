package com.litongjava.hotswap.wrapper.spring.boot;

import java.text.DecimalFormat;

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
    SpringApplicationWrapper.run(BootArgument.getBootClazz(), BootArgument.getArgs(), true);
    System.err.println("Loading complete in " + getTimeSpent(start) + " seconds (^_^)\n");
  }

  protected String getTimeSpent(long startTime) {
    float timeSpent = (System.currentTimeMillis() - startTime) / 1000F;
    return decimalFormat.format(timeSpent);
  }

}
