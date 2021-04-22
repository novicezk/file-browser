package com.github.novicezk.file.browser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(MultipartProperties.class)
public class FileBrowserApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileBrowserApplication.class, args);
	}

}
