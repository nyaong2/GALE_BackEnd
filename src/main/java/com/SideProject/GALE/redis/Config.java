package com.SideProject.GALE.redis;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class Config {
	public final static String BLACKLIST_CACHE_NAME = "jwtBlackList";
	
	@Value("${spring.redis.host}")
	private String redisHost;
	
	@Value("${spring.redis.port}")
	private int redisPort;

	@Value("${spring.datasource.hikari.password}")
	private String commonPw;
	
	
	
	@Bean // Connect
	public RedisConnectionFactory redisConnectionFactory() {
		//setPassword deprecated 대체제
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(redisHost);
		redisStandaloneConfiguration.setPort(redisPort);
		redisStandaloneConfiguration.setPassword(commonPw); //redis에 비밀번호가 설정 되어 있는 경우 설정해주면 됩니다.
		return new LettuceConnectionFactory(redisStandaloneConfiguration);	
	}

	@Bean // Template Key 변환하는 아이
	public RedisTemplate<String, Object> redisTemplate() {
		//RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		
		// 직렬화/역직렬화 시켜줌. 이게 있어야 redis-cli에서 조회시 이상하게 보이지 않음.
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	
}
