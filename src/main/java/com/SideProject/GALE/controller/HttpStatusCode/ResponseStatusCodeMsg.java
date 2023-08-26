package com.SideProject.GALE.controller.HttpStatusCode;

import lombok.Getter;

public  class ResponseStatusCodeMsg {
	public static final String SUCCESS = "Sucess";
	public static final String FAIL_SERVICE_UNAVAILABLE = "FAIL_ServerError";
	
	public static class Auth
	{
		public static final String FAIL = "FAIL";
		public static final String FAIL_UNAUTHORIZED = "FAIL_UNAUTHORIZED";
		public static final String FAIL_NULLDATA = "FAIL_NULLDATA";
		public static final String FAIL_OVERFLOWDATA = "FAIL_OVERFLOWDATA";
		public static final String FAIL_DENIALAUTHSATISFY = "FAIL_DENIALAUTHSATISFY";
		
		
		public static final String FAIL_PASSWORD = "FAIL_PASSWORD";
		public static final String FAIL_DUPLICATION ="FAIL_DUPLICATION";
		
		public static final String FAIL_NOTFOUND = "FAIL_NOTFOUND";
		
		public static final String FAIL_REGTOKEN = "FAIL_REGTOKEN";
		public static final String FAIL_INVALIDTOKEN = "FAIL_INVALIDTOKEN";
		public static final String FAIL_DIFFERENTTOKEN = "FAIL_DENIALAUTHSATISFY";
	}
	
	
	public static class Board
	{
		public static final String FAIL	= "FAIL";
		public static final String FAIL_UNAUTHORIZED = "FAIL_UNAUTHORIZED";
		public static final String FAIL_FORBIDDEN = "FAIL_FORBIDDEN";
		public static final String FAIL_NOTFOUND = "FAIL_NOTFOUND";
		public static final String FAIL_BAD_REQUEST = "FAIL_BAD_REQUEST";
	}
	
	
	public static class File
	{
		public static final  String UNSUPPORTED_Extension = "UNSUPPORTED_Extension";
		public final static String FAIL = "FAIL";
		public final static String FAIL_DENYFILEEXTENSION = "FAIL_DENYFILEEXTENSION";
		public final static String FAIL_NOTFOUND= "FAIL_NOTFOUND";
		public final static String FAIL_DUPLICATEFILE= "FAIL_DUPLICATEFILE";
	}
	
	public static class Planner
	{
		public static final String FAIL_BAD_REQUEST = "BAD_REQUEST";
	}
}
