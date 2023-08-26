package com.SideProject.GALE.redis;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.SideProject.GALE.util.TimeUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
	private final RedisTemplate<String, Object> redisTemplate;

	public void Save(final String key, final Object value, long expireMilliSeconds)
	{
		redisTemplate.opsForValue().set(key, value, Duration.ofMillis(expireMilliSeconds));
		System.out.println("Redis 대략 expire 시간 : " + TimeUtils.CurrentTimeStr(TimeUtils.GetCurrentMilliSeconds()+ expireMilliSeconds));
	}
	
	public String Get(final String key)
	{
		return String.valueOf(redisTemplate.opsForValue().get(key)); 
	}
	
	public boolean Del(final String key)
	{
		return redisTemplate.delete(key);
	}
}
