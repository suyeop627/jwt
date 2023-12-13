package com.study.springsecurityboard.jwt;

import com.study.springsecurityboard.dto.MemberInfoDto;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProvider implements AuthenticationProvider {
  private final JwtUtils jwtUtils;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;

    String token = authenticationToken.getToken();

    Claims claimsFromAccessToken = jwtUtils.getClaimsFromAccessToken(token);

    List<String> roles = (List<String>) claimsFromAccessToken.get("roles");

    Set<SimpleGrantedAuthority> authorities = roles.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());

    MemberInfoDto loginMemberInfo = MemberInfoDto.builder()
        .memberId(claimsFromAccessToken.get("memberId", Long.class))
        .email(claimsFromAccessToken.getSubject())
        .name(claimsFromAccessToken.get("name", String.class)).build();
    log.info("user {} get authentication", loginMemberInfo);

    return new JwtAuthenticationToken(loginMemberInfo, token, authorities);
  }


  @Override
  public boolean supports(Class<?> authentication) {
    return JwtAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
