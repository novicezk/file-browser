package com.github.novicezk.file.browser;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fb")
public class FileBrowserProperties {
	/**
	 * 文件访问的根目录.
	 */
	private String root = "/tmp";
	/**
	 * 文件访问是否需要登录.
	 */
	private boolean accessAuthenticated = false;
	/**
	 * 登陆后是否允许更改文件，包括上传文件、删除文件、创建目录等.
	 */
	private boolean modifiable = false;
	/**
	 * 用户名.
	 */
	private String username = "admin";
	/**
	 * 密码.
	 */
	private String password = "novice@2021";
	/**
	 * 可预览的文件类型.
	 */
	private String[] previewFileExts = new String[]{"", "gif", "jpg", "jpeg", "png", "pdf", "svg", "json", "txt", "log",
			"xml", "css", "js", "ico", "html", "ts", "scss", "less", "properties", "java", "conf", "md"};
	/**
	 * 可监控的文件类型.
	 */
	private String[] tailFileExts = new String[]{"txt", "log"};
}
