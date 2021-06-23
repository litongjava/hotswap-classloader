package com.litongjava.hotswap.server;
/**
 * @author create by ping-e-lee on 2021年6月23日 上午10:23:28 
 * @version 1.0 
 * @desc
 */
public interface RestartServer {

  public boolean isStarted();

  public void restart();
  
}
