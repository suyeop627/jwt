package com.study.securitywithjwt.jwt;

import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
@Setter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
  private final Object principal;
  private String token;
//토큰만으로 인증 시도
  public JwtAuthenticationToken(String token) {
    super(null);
    this.principal = null;
    this.token = token;
    setAuthenticated(false);
  }
//토큰 + 사용자 정보 있음 ->
  public JwtAuthenticationToken(Object principal, String token, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.token = token;
    super.setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  public String getToken() {
    return token;
  }
}