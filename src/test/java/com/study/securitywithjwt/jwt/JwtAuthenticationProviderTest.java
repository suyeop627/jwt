package com.study.securitywithjwt.jwt;

import com.study.securitywithjwt.dto.MemberInfoInToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProviderTest {
  @Mock
  JwtUtils jwtUtils;
  @InjectMocks
  JwtAuthenticationProvider jwtAuthenticationProvider;
  @Test
  void authenticate_validState_returnJwtAuthenticationToken() {
    //given
    String name = "name";
    String email = "test@test.com";
    Date issuedAt = new Date();
    Date expiration =new Date(new Date().getTime() + 60 * 60 * 1000L);
    Long memberId = 1L;
    List<String> roles = List.of("ROLE_USER");
    String token = "token_for_test";
    JwtAuthenticationToken authentication = new JwtAuthenticationToken(token);
    Claims claims = Jwts.claims()
        .subject(email)
        .issuedAt(issuedAt)
        .expiration(expiration)
        .add("name", name)
        .add("roles", roles)
        .add("memberId", memberId)
        .build();

    given(jwtUtils.getClaimsFromAccessToken(anyString())).willReturn(claims);

    //when
    JwtAuthenticationToken jwtAuthenticationToken = jwtAuthenticationProvider.authenticate(authentication);
    MemberInfoInToken principal =(MemberInfoInToken) jwtAuthenticationToken.getPrincipal();

    //then
    assertThat(principal).isNotNull();
    assertThat(principal.getName()).isEqualTo(name);
    assertThat(principal.getEmail()).isEqualTo(email);
    assertThat(principal.getMemberId()).isEqualTo(memberId);
    assertThat(principal.getRoles()).isEqualTo(new HashSet<>(roles));
  }

  @Test
  void supports_JwtAuthenticationToken_returnTrue() {
    //given, when
    boolean isSupported = jwtAuthenticationProvider.supports(JwtAuthenticationToken.class);
    //then
    assertThat(isSupported).isTrue();
  }

  @Test
  void supports_UsernamePasswordAuthenticationToken_returnFalse() {
    //given, when
    boolean isSupported = jwtAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class);
    //then
    assertThat(isSupported).isFalse();
  }
}