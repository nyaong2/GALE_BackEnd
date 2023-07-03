package com.SideProject.GALE.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
	/*
	 * LocalDate : 년, 월, 일
	 * LocatlTime : 시, 분, 초, 나노초
	 * LocalDateTime : 년, 월, 일, 시, 분, 초, 나노초
	 * Date, Calendar = 자바 8버전 이전. deprecated 주로 됨.
	 */
	
	public static void SystemCurrentTimeSync()
	{
		String[] argv = {
				"w32tm.exe /config /syncfromflags:manual /manualpeerlist:time.windows.com",
				"w32tm.exe /config /update",
				"sc config W32Time start= demand",
				"sc start W32Time",
				"c:\\windows\\system32\\w32tm.exe /resync"
		};
		
		for(String argvStr : argv)
		{
			try {
				Process p = Runtime.getRuntime().exec("cmd /c " + argvStr);
			}catch (Exception e) {
				System.out.println("윈도우 시간 동기화 에러 : " + e);
			};
		}
	}
	
	public static void RestartRedis()
	{
		String[] argv = {
				"sc stop \"Redis\"",
				"sc start \"Redis\""
		};
		
		for(String argvStr : argv)
		{
			try {
				Process p = Runtime.getRuntime().exec("cmd /c " + argvStr);
			}catch (Exception e) {
			};
		}
	}
	
	public static String CurrentTimeStr(long milliSeconds)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("[yyyy년MM월dd일 HH시mm분ss초] ");
		return sdf.format(milliSeconds);
	}
	
	public static long GetCurrentMilliSeconds()
	{
		return System.currentTimeMillis();
	}
	
	public static long GetCurrentSeconds()
	{
		Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
		timeStamp.setNanos(0);
		return timeStamp.getTime();
	}
	public static Timestamp GetTimeStampCurrentSeconds()
	{
		Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
		timeStamp.setNanos(0);
		return timeStamp;
	}

}
