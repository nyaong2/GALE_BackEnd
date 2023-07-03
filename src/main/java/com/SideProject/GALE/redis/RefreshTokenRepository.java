package com.SideProject.GALE.redis;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.SideProject.GALE.jwt.JwtProvider;
import com.SideProject.GALE.mapper.auth.AuthMapper;
import com.SideProject.GALE.model.auth.TokenDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Repository
public class RefreshTokenRepository {
	private final RedisTemplate redisTemplate;
	
	public RefreshTokenRepository(final RedisTemplate redisTemplate)
	{
		this.redisTemplate = redisTemplate;
	}
	
//    public void save(final RefreshTokenDto repo) {
//        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
//        valueOperations.set(repo.getToken(), repo.getEmail());
//        redisTemplate.expire(repo.getToken(), 60L, TimeUnit.SECONDS);
//    }
//
//    public Optional<RefreshTokenDto> findById(final String refreshToken) {
//        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
//        String email = valueOperations.get(refreshToken);
//
//        if (!StringUtils.hasText(email)) {
//            return Optional.empty();
//        }
//
//        return Optional.of(new RefreshTokenDto(refreshToken, email,LocalDateTime.now()));
//    }
}
	
	
