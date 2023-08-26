package com.SideProject.GALE.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.EnumSet;

import org.springframework.stereotype.Component;

@Component
public class FileUtils {
	/*
	 * @Compomont
	 *  - 사용시 : Bean으로 등록되어 자동주입 -> Spring 컨테이너가 객체의 생명주기를 관리하며 싱글톤으로 관리되기 때무넹 여러곳에서 동일한 인스턴스를 공유하여 메모리 효율적으로 사용가능
	 *  - 미사용으로 직접적으로 객체 생성를 생성시 : 직접적으로 객체 생성을 한 것을 개발자가 직접적으로 처리해줘야 함.
	 */
	
	public String ExtractFileHashSHA256(String fullFilePath) throws Exception {
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
	
	public boolean ForceFolderDelete(String fullPath)
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
}
