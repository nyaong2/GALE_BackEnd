package com.SideProject.GALE.util;

import java.util.HashMap;
import java.util.Map.Entry;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

public class DebugMsg {
	
	public static void Msg(String msg)
	{
		System.out.println(msg);
		System.out.println();
	}
	
	public static void Msg(String classFunName, String msg)
	{
		System.out.println(TimeUtils.CurrentTimeStr(TimeUtils.GetCurrentMilliSeconds()) + " @" + classFunName + " " + msg);
		System.out.println();
	}
	
	public static void Msg(String classFunName, HashMap<String,Object> data)
	{
		System.out.println(TimeUtils.CurrentTimeStr(TimeUtils.GetCurrentMilliSeconds()) + " @" + classFunName);
		for (Entry<String,Object> obj : data.entrySet())
		{
			String valueStr = obj.getValue().toString();
			System.out.println(" - [" + obj.getKey() + ": "  + valueStr + "]");
		}
		System.out.println();

	}
	
	public static void Msg(String classFunName, String messageType, @Nullable HttpStatus httpStatus, @Nullable String code, String message, @Nullable String result)
	{
		System.out.println(TimeUtils.CurrentTimeStr(TimeUtils.GetCurrentMilliSeconds()) + " @" + classFunName + " [MessageType : #" + messageType + "]");
		if(StringUtils.hasText(result))
			System.out.println(" - [Status : " + httpStatus + "] [Code : " + code + "] [Message : " + message + "]");
		else
			System.out.println(" - [Status : " + httpStatus + "] [Code : " + code + "] [Message : " + message + "] [Result : " + result + "]");
		System.out.println();
	}
	
	
}
