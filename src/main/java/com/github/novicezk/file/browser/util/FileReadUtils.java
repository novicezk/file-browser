package com.github.novicezk.file.browser.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileMode;
import cn.hutool.core.text.CharPool;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class FileReadUtils {

	public List<String> readLastLines(File file, int n) throws IOException {
		List<String> lines = new ArrayList<>();
		if (n <= 0) {
			return lines;
		}
		try (RandomAccessFile accessFile = FileUtil.createRandomAccessFile(file, FileMode.r)) {
			final long len = accessFile.length();
			long start = accessFile.getFilePointer();
			long nextEnd = len - 1;
			accessFile.seek(nextEnd);
			int c;
			int currentLine = 0;
			while (nextEnd > start) {
				if (currentLine > n) {
					break;
				}
				c = accessFile.read();
				if (c == CharPool.LF || c == CharPool.CR) {
					String line = FileUtil.readLine(accessFile, StandardCharsets.UTF_8);
					if (null != line) {
						lines.add(line);
					}
					currentLine++;
					nextEnd--;
				}
				nextEnd--;
				accessFile.seek(nextEnd);
				if (nextEnd == 0) {
					final String line = FileUtil.readLine(accessFile, StandardCharsets.UTF_8);
					if (null != line) {
						lines.add(line);
					}
					break;
				}
			}
		}
		Collections.reverse(lines);
		return lines;
	}

}
