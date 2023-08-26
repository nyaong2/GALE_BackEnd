package com.SideProject.GALE.model.auth;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor // 파라미터 없는 기본 생성자 생성
@AllArgsConstructor // 모든 필드값을 파라미터로 받는 생성자 생성
public class UserDto implements UserDetails {
	public String email;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public String password;
	public String confirmpassword; //Front 전용
	public String nickname;
	public int role = 1;
	
	public boolean NullChecking()
	{
		return (!StringUtils.hasText(email) || !StringUtils.hasText(password)
				|| !StringUtils.hasText(confirmpassword) || !StringUtils.hasText(nickname))
					? true : false;
	}
	
	
	// 이하 코드는 security 를 위한 용도
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isEnabled() {
        return true;
    }
}
