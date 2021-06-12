package net.imyeyu.exe4FX.bean;

import lombok.Data;

/**
 * 执行参数
 *
 * 夜雨 创建于 2021-06-02 23:27
 */
@Data
public class Params {

	private String pom;
	private String icon;
	private String splashScreen;
	private String out;
	private String jdk;
	private String runScript;
	private String customModule;
	private String fileName;
	private String fileVersion;
	private String productVersion;
	private String internalName;
	private String description;
	private String company;
	private String trademarks;
	private String copyright;
	private String comments;
	private boolean isX64;
	private boolean isInvisible;
	private boolean useUPX;
	private boolean needAdmin;

	// 缓存
	private String sdk;
	private String jmods;
	private String artifactId;
	private boolean needDownloadSDK;
}
