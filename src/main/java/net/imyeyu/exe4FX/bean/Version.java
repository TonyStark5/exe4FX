package net.imyeyu.exe4FX.bean;

import lombok.Data;

/**
 * 最新版本信息
 *
 * 夜雨 创建于 2021-06-10 17:41
 */
@Data
public class Version {

	private Long id;
	private String name;
	private String version;
	private String content;
	private String url;
	private Long createdAt;
	private Long updatedAt;
	private Long deletedAt;
}