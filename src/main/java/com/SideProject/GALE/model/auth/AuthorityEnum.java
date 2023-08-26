package com.SideProject.GALE.model.auth;

public class AuthorityEnum {
	public static String ROLE_ADMIN = "ROLE_ADMIN";
	public static String ROLE_USER = "ROLE_USER";
	
	
	public static String GetIntegerAuthorityToString(int integerAuthority)
	{
		switch(integerAuthority)
		{
			case 0:
				return ROLE_ADMIN;
			case 1:
				return ROLE_USER;
			default:
				return null;
		}
	}
}
