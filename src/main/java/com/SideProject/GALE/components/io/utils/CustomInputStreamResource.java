package com.SideProject.GALE.components.io.utils;

import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;

public class CustomInputStreamResource extends InputStreamResource {
	private String fileName;

	public CustomInputStreamResource(InputStream inputStream, String fileName) {
		super(inputStream);
		this.fileName= fileName;
	}
	
	@Override
	public String getFilename() {
		return this.fileName;
	}

}
