package com.study.securitywithjwt.jwt;

import com.study.securitywithjwt.dto.MemberInfoInToken;
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
  public JwtAuthenticationToken authenticate(Authentication authentication) throws AuthenticationException {

    JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;

    String token = authenticationToken.getToken();

    Claims claimsFromAccessToken = jwtUtils.getClaimsFromAccessToken(token);

    List<String> roles = (List<String>) claimsFromAccessToken.get("roles");

    Set<SimpleGrantedAuthority> authorities = roles.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());

    MemberInfoInToken memberInfoInToken = MemberInfoInToken.builder()
        .memberId(claimsFromAccessToken.get("memberId", Long.class))
        .email(claimsFromAccessToken.getSubject())
        .name(claimsFromAccessToken.get("name", String.class))
        .roles(new HashSet<>(roles))
        .build();

    log.info("user {} get authentication", memberInfoInToken);

    return new JwtAuthenticationToken(memberInfoInToken, token, authorities);
  }


  @Override
  public boolean supports(Class<?> authentication) {
    //authentication parameter가 JwtAuthenticationToken클래스와 호환되는지.
    //해당 provider 에서, 주어진 authentication 을 처리할 수 있는지 확인
    return JwtAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
