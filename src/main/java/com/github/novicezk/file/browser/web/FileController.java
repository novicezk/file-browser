package com.github.novicezk.file.browser.web;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.github.novicezk.file.browser.FileBrowserProperties;
import com.github.novicezk.file.browser.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

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
		String src = StrUtil.removePrefix(this.request.getServletPath(), "/view/");
		File file = FileUtil.file(this.properties.getRoot() + File.separator + src);
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
		File file = FileUtil.file(this.properties.getRoot() + File.separator + src);
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

	@PostMapping("/mkdir")
	public Message<Void> mkdir(@RequestParam String name, @RequestParam String src) {
		File dir = FileUtil.file(this.properties.getRoot() + File.separator + src);
		if (!dir.exists()) {
			return Message.notFound();
		}
		FileUtil.mkdir(Path.of(this.properties.getRoot(), src, name));
		return Message.success();
	}

	@DeleteMapping("/delete")
	public Message<Void> delete(@RequestParam String src) {
		File file = FileUtil.file(this.properties.getRoot() + File.separator + src);
		if (file.exists() && !FileUtil.del(file)) {
			return Message.of(-1, "删除失败");
		}
		return Message.success();
	}

	@PostMapping("/upload")
	public Message<Void> upload(@RequestParam("file") MultipartFile partFile, @RequestParam String src) {
		if (partFile == null) {
			return Message.of(Message.VALIDATION_ERROR_CODE, "未选择上传文件");
		}
		File dir = FileUtil.file(this.properties.getRoot() + File.separator + src);
		if (!dir.exists()) {
			return Message.notFound();
		}
		var fileName = partFile.getOriginalFilename();
		if (StrUtil.isBlank(fileName)) {
			fileName = IdUtil.fastSimpleUUID();
		}
		File file = FileUtil.file(dir, fileName);
		try {
			partFile.transferTo(file);
			return Message.success();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return Message.failure();
		}
	}

}
