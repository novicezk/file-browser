package com.github.novicezk.file.browser.web;

import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.novicezk.file.browser.FileBrowserProperties;
import com.github.novicezk.file.browser.pojo.FileVO;
import com.github.novicezk.file.browser.pojo.NavVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.comparator.BooleanComparator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BrowserController {
	private final FileBrowserProperties properties;
	private final MultipartProperties multipartProperties;
	private final HttpServletRequest request;

	@GetMapping({"/browser/**", "/browser**"})
	public String browser(Model model, @RequestParam(required = false, defaultValue = "name,asc") String sort) {
		String path = StrUtil.replace(this.request.getServletPath(), "/browser", "");
		if (StrUtil.startWith(path, "/")) {
			path = path.substring(1);
		}
		model.addAttribute("contextPath", this.request.getContextPath());
		model.addAttribute("path", path);
		model.addAttribute("modifiable", this.properties.isModifiable());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		model.addAttribute("username", username);
		model.addAttribute("authenticated", authentication.isAuthenticated() && !"anonymousUser".equals(username));
		model.addAttribute("naves", generateNaves(path));
		var uploadMaxsize = this.multipartProperties.getMaxFileSize().toBytes();
		model.addAttribute("uploadMaxsize", uploadMaxsize);
		model.addAttribute("uploadMaxsizeDisplay", FileUtil.readableFileSize(uploadMaxsize));
		String realPath = this.properties.getRoot() + File.separator + path;
		if (!FileUtil.exist(realPath)) {
			model.addAttribute("files", Collections.EMPTY_LIST);
			return "browser";
		}
		String property = StrUtil.subBefore(sort, ",", false);
		boolean asc = "asc".equals(StrUtil.subAfter(sort, ",", false));
		List<FileVO> fileVOList = Arrays.stream(FileUtil.ls(realPath)).map(this::convert)
				.sorted((o1, o2) -> comparator(o1, o2, property, asc))
				.collect(Collectors.toList());
		model.addAttribute("files", fileVOList);
		model.addAttribute("sizeDisplay", fileVOList.size() + " 项");
		long lastModified = FileUtil.file(realPath).lastModified();
		model.addAttribute("lastModifiedDisplay", convertTimestamp2DateDisplay(lastModified));
		return "browser";
	}

	private List<NavVO> generateNaves(String path) {
		List<NavVO> naves = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		String[] pathSplit = StrUtil.split(path, File.separator);
		for (int i = 0; i < pathSplit.length; i++) {
			if (i > 0) {
				sb.append(File.separator);
			}
			sb.append(pathSplit[i]);
			NavVO navVO = new NavVO();
			navVO.setName(pathSplit[i]);
			navVO.setPath(sb.toString());
			naves.add(navVO);
		}
		return naves;
	}

	private FileVO convert(File file) {
		FileVO fileVO = new FileVO();
		fileVO.setName(file.getName());
		long lastModified = file.lastModified();
		fileVO.setLastModified(lastModified);
		fileVO.setLastModifiedDisplay(convertTimestamp2DateDisplay(lastModified));
		if (file.isDirectory()) {
			fileVO.setDirectory(true);
			long directorySize = ArrayUtil.length(file.listFiles());
			fileVO.setSize(directorySize);
			fileVO.setSizeDisplay(directorySize + " 项");
		} else {
			fileVO.setDirectory(false);
			long fileSize = file.length();
			fileVO.setSize(fileSize);
			fileVO.setSizeDisplay(FileUtil.readableFileSize(fileSize));
			String suffix = FileUtil.getSuffix(file.getName());
			fileVO.setPreview(ArrayUtil.contains(this.properties.getPreviewFileExts(), suffix));
			fileVO.setTail(ArrayUtil.contains(this.properties.getTailFileExts(), suffix));
		}
		return fileVO;
	}

	private String convertTimestamp2DateDisplay(long lastModified) {
		return DateUtil.format(new Date(lastModified), "yyyy-MM-dd HH:mm:ss");
	}

	private int comparator(FileVO v1, FileVO v2, String property, boolean asc) {
		int i = BooleanComparator.TRUE_LOW.compare(v1.isDirectory(), v2.isDirectory());
		if (i != 0) {
			return i;
		}
		Object v1Val = ReflectUtil.getFieldValue(v1, property);
		Object v2Val = ReflectUtil.getFieldValue(v2, property);
		i = CompareUtil.compare(v1Val, v2Val, false);
		return asc ? i : i * -1;
	}

}
