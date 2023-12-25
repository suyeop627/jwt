package com.study.securitywithjwt.jwt;

import com.study.securitywithjwt.dto.LoginMemberInfo;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
// Authentication 이 JwtAuthenticationToken 인 경우의 인증 처리 담당.
// access token을 파싱하여 JwtAuthenticationToken 객체를 생성함.
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProvider implements AuthenticationProvider {
  private final JwtUtils jwtUtils;

  @Override
  public JwtAuthenticationToken authenticate(Authentication authentication) throws AuthenticationException {

    JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;

    String token = authenticationToken.getToken();

    Claims claimsFromAccessToken = jwtUtils.extractClaimsFromAccessToken(token);

    List<String> roles = (List<String>) claimsFromAccessToken.get("roles");

    Set<SimpleGrantedAuthority> authorities = roles.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());

    LoginMemberInfo loginMemberInfo = LoginMemberInfo.builder()
        .memberId(claimsFromAccessToken.get("memberId", Long.class))
        .email(claimsFromAccessToken.getSubject())
        .name(claimsFromAccessToken.get("name", String.class))
        .roles(new HashSet<>(roles))
        .build();

    log.info("Authentication created from access token. AccessToken: {}", token);

    return new JwtAuthenticationToken(loginMemberInfo, token, authorities);
  }


  @Override
  public boolean supports(Class<?> authentication) {
    //authentication parameter가 JwtAuthenticationToken클래스와 호환되는지.
    //해당 provider 에서, 주어진 authentication 을 처리할 수 있는지 확인
    return JwtAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
