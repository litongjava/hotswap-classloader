## hotswap-classloader
### 1.1.hotswap-classloader简介

hotswap-classloader参考jfinal-undertow的hotswap设计开发
https://gitee.com/jfinal/jfinal-undertow/tree/master/src/main/java/com/jfinal/server/undertow/hotswap

主要功能
实现spring-boot快速速热加载,测试热加载在1s左右

简介
spring-boot开发者福音,spring-boot集成hotswap-classloader,在eclipse中启动spring-boot,对controller进行任意修改,按Ctrl+S保存,HotSwapClassloader会检测到文件修改,热记载代码并生效,加载过程在1秒内完成

替代产品
springloaded
spring-boot-devtools
JRebel

### 1.2.和spring-boot整合

添加依赖

```xml
<dependency>
  <groupId>com.litongjava</groupId>
  <artifactId>hotswap-classloader</artifactId>
  <version>1.0.1</version>
</dependency>
```



修改启动类
使用

```
SpringApplicationWrapper.run(Application.class, args, true);
```

修改后如下

```
package com.litongjava.spring.boot.v216;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.litongjava.hotswap.wrapper.spring.boot.SpringApplicationWrapper;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    //SpringApplication.run(Application.class, args);
    SpringApplicationWrapper.run(Application.class, args, true);
  }
}
```

完成
整合后的工程  
https://gitee.com/litongjava_admin/java-ee-spring-boot-study/tree/master/java-ee-spring-boot-2.1.6-study/java-ee-spring-boot-2.1.6-hello
### 1.3.idea支持

目前已经支持idea  
HotsWapWatcher 是通过监听 target/classes 下面的 class 文件被修改时触发的热加载  
而 idea在开发的过程中是不会自动编译的，造成 target/classes 下面的文件没有变化，  
解决办法有两种  
第一种:通过快捷键 Ctrl + F9 触发编译,在IntelliJ IDEA 2019.3.3 (Ultimate Edition)测试失败  
第一种:可以让 IDEA 像 eclipse 一样开启自动编译,IntelliJ IDEA 2019.3.3 (Ultimate Edition)配置方法如下  
第一步:  
File-->Settings-->Compiler-->勾选Build project automaitcally  
第二步:  
安装快捷键Ctrl+Shift+A-->输入Registry并进入-->勾线compiler.automake.allow.when.app.running  
第三步:
取消idea自动保存代码  
File --> Settings--> Appearance & Behavior --> System Settings -->  
 Save files on frame deactivation 取消勾选  
Synchronize files on frame or editor tab activation 取消勾选   
然后点击 Apply生效

显示modified标记,修改文件会在代码编辑窗口的tab区域的文件名上显示"幸好"标记  
File --> Settings --> Editor --> General --> Editor Tabs --> Mark modified(*) 勾选 --> Apply    

注意:同一个包下有至少有2个.java文件，这个包下面的java文件正常热加载；不知道什么原因当目录只有一个.java文件的时候，idea总是把父文件夹删了再新建,导致无法检测到文件变动
测试详情
https://jfinal.com/share/2436

### 1.4 eclipse测试效果图

在spring-boot启动的情况下,向controller添加一个方法,按Ctrl+S保存HotSwapClassloader检测到文件,自动重新加载代码,并生效,加载过程在0.8秒内完成

![ABC](doc/images/hotswap-classloader-spring-boot-elipse-test.gif)

### 1.5 idea测试效果图
在spring-boot启动的情况下,向controller添加一个方法,按Ctrl+S保存HotSwapClassloader检测到文件,自动重新加载代码,并生效,加载过程在10.8秒内完成,idea编译大概有10s的延迟
![ABC](doc/images/hotswap-classloader-spring-boot-idea-test.gif)
