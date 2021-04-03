package com.github.novicezk.file.browser.web;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.Tailer;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.novicezk.file.browser.FileBrowserProperties;
import com.github.novicezk.file.browser.util.FileReadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TailController {
	private final FileBrowserProperties properties;
	private final HttpServletRequest request;

	@GetMapping("/tail/**")
	public String tail(Model model, @RequestParam(required = false, defaultValue = "20") int n) throws IOException {
		String target = StrUtil.replace(this.request.getServletPath(), "/tail/", "");
		model.addAttribute("contextPath", this.request.getContextPath());
		model.addAttribute("target", target);
		File file = FileUtil.file(this.properties.getRoot() + "/" + target);
		String suffix = FileUtil.getSuffix(target);
		if (!file.exists()) {
			model.addAttribute("error", "文件不存在");
		} else if (file.isDirectory() || !ArrayUtil.contains(this.properties.getTailFileExts(), suffix)) {
			model.addAttribute("error", "无法监控");
		} else {
			model.addAttribute("initLines", FileReadUtils.readLastLines(file, n));
		}
		return "tail";
	}

	@ResponseBody
	@GetMapping("/tail-sse")
	public Object sse(@RequestParam String target) throws IOException {
		File file = FileUtil.file(this.properties.getRoot() + "/" + target);
		String suffix = FileUtil.getSuffix(target);
		if (!file.exists()) {
			return new ResponseEntity<>("文件不存在", HttpStatus.NOT_FOUND);
		} else if (file.isDirectory() || !ArrayUtil.contains(this.properties.getTailFileExts(), suffix)) {
			return new ResponseEntity<>("无法监控", HttpStatus.NOT_IMPLEMENTED);
		}
		SseEmitter sseEmitter = new SseEmitter(30000L);
		sseEmitter.send(SseEmitter.event().name("ready").data(""));
		final Tailer tailer = new Tailer(file, line -> {
			try {
				sseEmitter.send(SseEmitter.event().data(line));
			} catch (IOException e) {
				log.debug(e.getLocalizedMessage(), e);
				ThreadUtil.interrupt(Thread.currentThread(), false);
			}
		}, 0);
		tailer.start(true);
		return sseEmitter;
	}

}
