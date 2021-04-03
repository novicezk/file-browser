package com.github.novicezk.file.browser.web;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.github.novicezk.file.browser.FileBrowserProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {
	private final FileBrowserProperties properties;
	private final HttpServletRequest request;
	private final HttpServletResponse response;

	private static final int MAX_AGE = 7 * 24 * 3600;

	@GetMapping("/view/**")
	public void view() throws IOException {
		String src = StrUtil.replace(this.request.getServletPath(), "/view", "");
		File file = FileUtil.file(this.properties.getRoot() + src);
		if (!file.exists()) {
			this.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		var modifiedSince = this.request.getDateHeader("If-Modified-Since");
		long lastModified = file.lastModified();
		if (lastModified <= (modifiedSince + 1000L)) {
			this.response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		String mimeType;
		if (file.getName().endsWith(".ico")) {
			mimeType = "image/x-icon";
		} else {
			mimeType = FileUtil.getMimeType(file.getPath());
		}
		if (StrUtil.isBlank(mimeType)) {
			mimeType = "text/plain";
		}
		this.response.setContentType(mimeType);
		this.response.setCharacterEncoding("utf-8");
		this.response.setHeader("Cache-Control", "public, no-cache, max-age=" + MAX_AGE);
		this.response.setDateHeader("Last-Modified", lastModified);
		this.response.setContentLength((int) file.length());
		try (InputStream in = new FileInputStream(file); OutputStream out = this.response.getOutputStream()) {
			IoUtil.copy(in, out);
		}
	}

	@GetMapping("/download")
	public void download(@RequestParam String src) throws IOException {
		File file = FileUtil.file(this.properties.getRoot() + "/" + src);
		if (!file.exists()) {
			this.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		var modifiedSince = this.request.getDateHeader("If-Modified-Since");
		long lastModified = file.lastModified();
		if (lastModified <= (modifiedSince + 1000L)) {
			this.response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		this.response.setHeader("Cache-Control", "public, no-cache, max-age=" + MAX_AGE);
		this.response.setHeader("Content-Disposition", "attachment;filename*=utf-8'zh_cn'" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.name()));
		this.response.setContentLength((int) file.length());
		try (InputStream in = new FileInputStream(file); OutputStream out = this.response.getOutputStream()) {
			IoUtil.copy(in, out);
		}
	}

}
