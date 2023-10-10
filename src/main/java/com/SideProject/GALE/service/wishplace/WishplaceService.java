package com.SideProject.GALE.service.wishplace;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.jwt.JwtProvider;
import com.SideProject.GALE.mapper.wishplace.WishplaceMapper;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class WishplaceService {
	private final JwtProvider jwtProvider;
	private final WishplaceMapper wishplaceMapper;
	
	public void Add(HttpServletRequest request, int board_Number)
	{
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("userid", userid);
			map.put("board_Number", board_Number);

			if(wishplaceMapper.Add(map) != 1)
				throw new Exception();
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
	public void Del(HttpServletRequest request, int board_Number)
	{
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("userid", userid);
			map.put("board_Number", board_Number);

			int result = wishplaceMapper.Del(map);
			
			if(result == 0)
				throw new CustomRuntimeException(ResCode.BAD_REQUEST_BOARD_ALREADYPROCESSED_WISHPLACE);
			else if (result != 1)
				throw new Exception();
			
		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
}
