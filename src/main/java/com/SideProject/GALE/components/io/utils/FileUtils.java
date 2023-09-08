package com.SideProject.GALE.components.io.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Comparator;

import org.springframework.stereotype.Component;

@Component
public class FileUtils {
	
	public String ExtractFileHashSHA256(String fullFilePath) {
		String SHA = ""; 
		int buff = 16384;
		try {
			RandomAccessFile file = new RandomAccessFile(fullFilePath, "r");

			MessageDigest hashSum = MessageDigest.getInstance("SHA-256");

			byte[] buffer = new byte[buff];
			byte[] partialHash = null;

			long read = 0;

			// calculate the hash of the hole file for the test
			long offset = file.length();
			int unitsize;
			while (read < offset) {
				unitsize = (int) (((offset - read) >= buff) ? buff : (offset - read));
				file.read(buffer, 0, unitsize);

				hashSum.update(buffer, 0, unitsize);

				read += unitsize;
			}

			file.close();
			partialHash = new byte[hashSum.getDigestLength()];
			partialHash = hashSum.digest();
			
			StringBuffer sb = new StringBuffer(); 
			for(int i = 0 ; i < partialHash.length ; i++){
				sb.append(Integer.toString((partialHash[i]&0xff) + 0x100, 16).substring(1));
			}
			SHA = sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return SHA;
	}
	
	public boolean ForceDeleteFolder(String fullPath)
	{
		try {
			Path folderPath = Paths.get(fullPath);
			File folder = new File(fullPath);
			
			if(folder.exists())
			{
				Files.walk(folderPath, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
					.forEach(File::delete);
				
				folder.delete();
			}
			
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
	
	public boolean DeleteFile(String fullFilePath)
	{
		try {
			File file = new File(fullFilePath);
			
			if(file.exists())
				file.delete();
		} catch (Exception ex) {
			return false;
		}
		
		return true;
	}
}
