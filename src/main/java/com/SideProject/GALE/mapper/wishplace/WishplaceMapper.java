package com.SideProject.GALE.mapper.wishplace;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WishplaceMapper {
	int Add(Map<String,Object> map);
	int Del(Map<String,Object> map);
}
