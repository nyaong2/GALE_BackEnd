package com.SideProject.GALE.components.response;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("serial")
public class ResponseHeaders {
	public static HttpHeaders Json()
	{
		return new HttpHeaders() {{
			add("Content-Type", "application/json; charset=UTF-8");
			}};
	}
	
	public static HttpHeaders DownloadImageHeader(File imageFile)
	{
		
		String extension = FilenameUtils.getExtension(imageFile.getName());

		HttpHeaders httpHeader = new HttpHeaders(); 
		httpHeader.setContentDispositionFormData("attachment", imageFile.getName());
		httpHeader.setContentLength(imageFile.length());
		

		switch(extension)
		{
		case "jpg":
			httpHeader.setContentType(MediaType.IMAGE_JPEG);
			break;
		case "jpeg":
			httpHeader.setContentType(MediaType.IMAGE_JPEG);
			break;
		case "png":
			httpHeader.setContentType(MediaType.IMAGE_PNG);
			break;
		case "bmp":
			httpHeader.setContentType(MediaType.parseMediaType("image/bmp"));
		default:
			httpHeader.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		}
		
		
		
		return httpHeader;
	}
}
