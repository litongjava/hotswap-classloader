package com.litongjava.hotswap.kit;

import java.io.File;

/**
 * PathKitExt 用于扩展 com.jfinal.kit.PathKit，同时支持开发环境与部署环境
 * 解决 PathKit.getWebRootPath()/getRootClassPath() 在部署环境下无法正常
 * 工作的问题 
 * 
 * 一、问题描述
 * 1：jfinal-undertow 打包部署启动时在 com.jfinal.core.Config.initEngine() 中
 *    调用 PathKit.getWebRootPath()/detectWebRootPath() 会抛出 NPE
 *    
 *    引起异常的原因应该是 JFinal.initPathKit() 中的 servletContext.getRealPath("/")
 *    得到的是一个 null 值，造成 PathKit.setWebRootPath(null) 注入的是 null 值，从而后续
 *    Config.initEngine() 间接调用 PathKit.detectWebRootPath() 引发 NPE
 *    
 *    在部署环境之下，由于 servletContext.getRealPath("/") 返回 null，而且 PathKit 中的
 *    ClassLoader.getResource("")、PathKit.class.getResource("/") 也不能正常工作
 *    合力引发 NPE
 *    
 * 
 * 2：jfinal-undertow 部署方式之下 PathKit.getWebRootPath()、getRootClassPath()
 *     都会报出异常，为 PathKit 的 rootClassPath、webRootPath 事先注入值，避免抛出异常
 *  
 * 3：由于 com.jfinal.kit.PathKit 历史久远，为兼容性起见不便进行改造
 * 
 * 
 * 二、解决方案
 * 1：一种手动处理的办法是用户在 JFinalConfig 继承类中的 configConstant(Constants me)
 *    中调用 PathKit.setWebRootPath()、PathKit.setRootClassPath() 注入值
 *    在该处手动注入的时机处于 JFinal.initPathKit() 之后与 Config.initEngine() 之前
 *     
 *    这两个值可以配置在外部配置文件中便于修改，但此法增加了配置工作量
 * 
 * 
 * 2：通过 PathKitExt 生成合理的 rootClassPath、webRootPath 并通过反射的方式
 *    注入到 com.jfinal.kit.PathKit 之中两个对应的变量之中
 *    
 *    关键在于生成的 rootClassPath、webRootPath 值需要同时满足开发环境与打包部署环境
 *    具体的生成逻辑放在 buildRootClassPath()、buildWebRootPath() 的注释中
 * 
 * 3：要兼顾三种情况：开发环境、fatjar 部署、"非fatjar" 部署
 * 
 */
public class PathKitExt {
	
	private static String locationPath = null;	// 定位路径
	
	private static String rootClassPath = null;
	private static String webRootPath = null;
	
	/**
	 * 1：获取 PathKitExt 类文件所处 jar 包文件所在的目录，注意在 "非部署" 环境中获取到的
	 *    通常是 maven 本地库中的某个目录，因为在开发时项目所依赖的 jar 包在 maven 本地库中
	 *    这种情况不能使用 
	 * 
	 * 2：PathKitExt 自身在开发时，也就是未打成 jar 包时，获取到的是 APP_BASE/target/classes
	 *    这种情况多数不必关心，因为 PathKitExt 在使用时必定处于 jar 包之中
	 * 
	 * 3：获取到的 locationPath 目录用于生成部署时的 config 目录，该值只会在 "部署" 环境下被获取
	 *    也用于生成 webRootPath、rootClassPath，这两个值也只会在 "部署" 时被获取
	 *    这样就兼容了部署与非部署两种场景 
	 * 
	 * 注意：该路径尾部的 "/" 或 "\\" 已被去除
	 */
	public static String getLocationPath() {
		if (locationPath != null) {
			return locationPath;
		}
		
		try {
			// Class<?> clazz = io.undertow.Undertow.class;		// 仅测试用
			Class<?> clazz = PathKitExt.class;
			String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
			path = java.net.URLDecoder.decode(path, "UTF-8");
			path = path.trim();
			File file = new File(path);
			if (file.isFile()) {
				path = file.getParent();
			}
			
			path = removeSlashEnd(path);		// 去除尾部 '/' 或 '\' 字符
			locationPath = path;
			
			return locationPath;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getRootClassPath() {
		if (rootClassPath == null) {
			rootClassPath = buildRootClassPath();
		}
		return rootClassPath;
	}
	
	/**
	 * 1：先通过 UndertowKit.getClassPathDirs() 从 System.getProperty("java.class.path")
	 *    中获取 class path 值，开发阶段将获取到正确的 class path 值，如: .../target/classes
	 * 
	 * 
	 * 2：如果项目打成 jar 包并部署将无法正确获取 class path 值，则使用 Class.getProtectionDomain()
	 *    的方式来获取
	 *   
	 *    
	 * 3：如果 targetClass 处在 jar 包之中，则 targetClass.getProtectionDomain() 方式获取到的是
	 *   targetClass 所在的 jar 文件的全路径名，例如：
	 *   			.../maven_repo/com/.../xxx.jar
	 *   			注意该值带有文件名
	 *   
	 *   如果 targetClass 不处在 jar 包之中，将获取 target 所在的目录的全路径，例如：
	 *   			.../target/classes
	 *   			注意该值仅仅是路径，不带文件名
	 *   
	 *   
	 *  4：不能跳过第一步直接使用 Class.getProtectionDomain() 方案，因为对于 maven 项目来说
	 *     该方案得到的路径通常是指向本地 maven 库中的一个路径，因为 maven 项目的多数 jar 依赖
	 *     都指向 maven 库
	 *     
	 *     而打包部署后的项目，其中的依赖要么在 fatjar 之中，要么在项目的 lib 子目录下，对于传统
	 *     java web 项目在 WEB-INF/lib 之下。这三种情况才有了间接定位 root class path 的可能
	 */
	private static String buildRootClassPath() {
		String classPathDirEndsWith_classes = getClassPathDirEndsWith_classes();
		if (classPathDirEndsWith_classes != null) {
			return classPathDirEndsWith_classes;
		}
		
		String path = getLocationPath();
		return processRootClassPath(path);
	}
	
	/**
	 * 获取以 "classes" 结尾的 class path
	 */
	private static String getClassPathDirEndsWith_classes() {
		String[] classPathDirs = UndertowKit.getClassPathDirs();
		if (classPathDirs == null || classPathDirs.length == 0) {
			return null;
		}
		
		for (String dir : classPathDirs) {
			if (dir != null) {
				dir = removeSlashEnd(dir.trim());
				if (dir.endsWith("classes")) {
					return dir;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 1：开发环境 path 会以 classes 结尾
	 * 
	 * 2：打包以后的部署环境不会以 classes 结尾，约定一个合理的项目打包结构
	 *    暂时约定 APP_BASE/config 为 rootClassPath，因为要读取外部配置文件
	 */
	private static String processRootClassPath(String path) {
		if (path.endsWith("classes")) {
			return path;
		}
		
		if (path.endsWith(File.separatorChar + "lib")) {
			path = path.substring(0, path.lastIndexOf(File.separatorChar));
		}
		
		return new File(path + File.separator + "config").getAbsolutePath();
	}
	
	public static String removeSlashEnd(String path) {
		if (path != null && path.endsWith(File.separator)) {
			return path.substring(0, path.length() - 1);
		} else {
			return path;
		}
	}
	
	// --------------------------------------------------------------------------------------
	
	public static String getWebRootPath() {
		if (webRootPath == null) {
			webRootPath = buildWebRootPath();
		}
		return webRootPath;
	}
	
	private static String buildWebRootPath() {
		String classPathDirEndsWith_classes = getClassPathDirEndsWith_classes();
		if (classPathDirEndsWith_classes != null) {
			return classPathDirEndsWith_classes;
		}
		
		String path = getLocationPath();
		return processWebRootPath(path);
	}
	
	private static String processWebRootPath(String path) {
		if (path.endsWith("classes")) {
			return path;
		}
		
		if (path.endsWith(File.separatorChar + "lib")) {
			path = path.substring(0, path.lastIndexOf(File.separatorChar));
		}
		
		return new File(path + File.separator + "webapp").getAbsolutePath();
	}
	
	// ---------
	
	/**
	 * 如果 jfinal-undertow 生成的 path 仍然不能满足需要，可以在 UndertowServer 启动之前
	 * 先手动注入值，例如：
	 * PathKitExt.setWebRootPath(...);
	 * UndertowServer.start(...);
	 * 
	 * 注意：建议优先通过配置 undertow.resourcePath 的方式来解决，例如：
	 *     undertow.resourcePath = IDEA开发时的module名称/src/main/webapp, src/main/webapp, webapp
	 *     上述配置适合于 IDEA 下在 maven module 中启动项目时使用
	 * 
	 * TODO: 未来在 IDEA 的 maven module 中启动项目的场景下解决一下自动探测该目录的适配问题
	 */
	public static void setWebRootPath(String webRootPath) {
		PathKitExt.webRootPath = webRootPath;
	}
	
	/**
	 * 用途与使用方式与 setWebRootPath(...) 类似，一般不用
	 */
	public static void setRootClassPath(String rootClassPath) {
		PathKitExt.rootClassPath = rootClassPath;
	}
}








