package com.github.novicezk.file.browser.pojo;

import lombok.Data;

@Data
public class FileVO {
	private String name;
	private boolean directory;
	private boolean preview;
	private boolean tail;
	private long lastModified;
	private String lastModifiedDisplay;
	private long size;
	private String sizeDisplay;
}
