# hotswap-classloader
## 1.简介
hotswap-classloader简介  
基于jvm的动态类加载器
hotswap-classloader参考jfinal-undertow的hotswap设计开发  
https://gitee.com/jfinal/jfinal-undertow/tree/master/src/main/java/com/jfinal/server/undertow/hotswap

主要功能
实现应用快速速热加载,测试热加载在1s左右

简介
spring-boot开发者福音,spring-boot集成hotswap-classloader,在eclipse中启动spring-boot,对controller进行任意修改,按Ctrl+S保存,使用HotSwapWatcher加HotSwapClassloader技术,动态检测class文件修改,重启应用解析class并生效,加载过程在1秒内完成

替代产品
springloaded
spring-boot-devtools
JRebel
## 2.使用
### 1.2.和spring-boot整合

添加依赖

```xml
<dependency>
  <groupId>com.litongjava</groupId>
  <artifactId>hotswap-classloader</artifactId>
  <version>1.0.4</version>
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
##### 1.3.1.版本信息
idea版本信息如下
![](readme_files/1.jpg)

#### 1.3.2 为什么要进行热加载配置
HotsWapWatcher 是通过监听 target/classes 下面的 class 文件被修改时触发的热加载  
而 idea在开发的过程中是不会自动编译的，造成 target/classes 下面的文件没有变化，  
解决办法有两种  
第一种:通过快捷键 Ctrl + F9 触发编译,在IntelliJ IDEA 2019.3.3 (Ultimate Edition)测试失败  
第一种:可以让 IDEA 像 eclipse 一样开启自动编译,IntelliJ IDEA 2019.3.3 (Ultimate Edition)配置方法如下

#### 1.3.3.IDEA 热加载设置
##### build project automatically
settings里搜索compiler 勾选build project automatically
![](readme_files/2.jpg)
这个效果还是很慢的大约3秒才能检测到：
这里需要一个配置可以加快速度：

##### Allow auto-make to start even if developed application is currently running
settings搜索make勾选Allow auto-make to start even if developed application is currently running
![](readme_files/3.jpg)

##### compiler.automake.allow.when.app.running
在某些旧版的中的设置如下  
按组合键 Shift+Ctrl+Alt+/，选择Registry  
勾选上compiler.automake.allow.when.app.running  
新版中没有找到这个设置  
##### 修改delay时间
Ctrl+Shift+Alt+/,选择Registry...  
进去找个配置：  
1、compiler.automake.postpone.when.idle.less.than  
默认是3000 改为 100即可   
官方解释：  
英文：If at the moment the autobuild is about to start the IDE is idle for less than specified milliseconds, the automatic build will be postponed in order not to interfere with the user's activity.  

中文翻译：如果在自动构建即将启动IDE时，IDE的空闲时间小于指定的毫秒，则自动构建将被推迟，以避免干扰用户的活动。  

简单理解为，你修改了Java代码后 按了Ctrl+s保存代码后 多久触发了自动编译热部署  

2、compiler.automake.trigger.delay  
默认值是3000可以改为100  
官方解释：  
英文：Delay in milliseconds before triggering auto-make in response to file system events  

中文翻译：在触发自动生成(auto-make)以响应文件系统事件之前的延迟(毫秒)  

简单理解为：Idea检测到文件变更后间隔多久就立马触发热部署  

3、compiler.document.save.trigger.delay  
默认1500改成100  

官方解释：  
英文：Delay in milliseconds before triggering save in response to document changes  
中文翻译：触发保存以响应文档更改之前的延迟(毫秒)  
简单理解为：idea检测到文件变更后多久触发文件保存  主要针对js css html静态资源  

##### 取消代码自动保存
取消自动保存
File --> Settings--> Appearance & Behavior --> System Settings -->  
旧版设置  
Save files on frame deactivation 取消勾选  
Synchronize files on frame or editor tab activation 取消勾选  

新版设置  
Save files if tab IDE is idle for 10 seconds 取消勾选  
Save file when switching to a dufferent application or a built-in terminal  
然后点击 Apply生效  

显示modified标记  
修改文件会在代码编辑窗口的tab区域的文件名上显示"星号"标记  
File --> Settings --> Editor --> General --> Editor Tabs --> Mark modified(*) 勾选 --> Apply  

设置完成之后,在idea修改一个文件之后,你需要安装Ctrl+S保存文件,文件编译之后会自动编译,自动重启应用并热加载生效

注意:同一个包下有至少有2个.java文件，这个包下面的java文件正常热加载；不知道什么原因当目录只有一个.java文件的时候，idea总是把父文件夹删了再新建,导致无法检测到文件变动 测试详情 https://jfinal.com/share/2436


### 1.4 spring-boot-maven-plugin支持
如果再命令行使用mvn spring-boot:run 启动spring-boot项目,默认使用的类加载器是plexus-classworlds  
如果要使用本加载器,需要的配置如下
1)添加依赖
2)修改启动类
3)在pom.xml中添加下面的配置
fork 设置为 true，会在运行的时创建一个新虚拟机执行。hotswap-classloader这个新创建 JVM中会加载类  
它速度会稍慢一些，但是隔离性非常好
```
<!-- Spring Boot -->
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <configuration>
    <includeSystemScope>true</includeSystemScope>
    <!--使该插件支持热启动 -->
    <fork>true</fork>
    <mainClass>${start-class}</mainClass>
  </configuration>
</plugin>
```
4)启动项目
```
mvn spring-boot:run
```
## 2.使用效果截图
### 2.1.eclipse测试效果图

在spring-boot启动的情况下,向controller添加一个方法,按Ctrl+S保存HotSwapClassloader检测到文件,自动重新加载代码,并生效,加载过程在0.8秒内完成

![ABC](doc/images/hotswap-classloader-spring-boot-elipse-test.gif)

### 2.2.idea测试效果图
在spring-boot启动的情况下,向controller添加一个方法,按Ctrl+S保存HotSwapClassloader检测到文件,自动重新加载代码,并生效,加载过程在10.8秒内完成,idea编译大概有10s的延迟
![ABC](doc/images/hotswap-classloader-spring-boot-idea-test.gif)

### 2.2.命令行测试效果图
在命令行使用mvn spring-boot:run 启动项目,在eclipse或者idea中修改代码  
测试的是一个大型项目,正常启动需要9.5s,热加载需要3.4秒  
[https://www.ixigua.com/iframe/7091662497010156063?autoplay=0](https://www.ixigua.com/iframe/7091662497010156063?autoplay=0)
<iframe width="720" height="405" frameborder="0" src="https://www.ixigua.com/iframe/7091662497010156063?autoplay=0" referrerpolicy="unsafe-url" allowfullscreen></iframe>
