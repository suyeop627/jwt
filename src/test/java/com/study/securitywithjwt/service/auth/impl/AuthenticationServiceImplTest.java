package com.study.securitywithjwt.service.auth.impl;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.LoginRequestDto;
import com.study.securitywithjwt.dto.LoginResponseDto;
import com.study.securitywithjwt.jwt.JwtUtils;
import com.study.securitywithjwt.repository.RefreshTokenRepository;
import com.study.securitywithjwt.security.user.MemberUserDetails;
import com.study.securitywithjwt.service.refreshtoken.RefreshTokenService;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

  @Mock
  AuthenticationManager authenticationManager;

  @Mock
  JwtUtils jwtUtils;

  @Mock
  RefreshTokenService refreshTokenService;

  @InjectMocks //@Mock으로 지정한 클래스들 주입해서 테스트할 클래스의 인스턴스 생성해줌
  AuthenticationServiceImpl authenticationService;


  @Test
  void test_login_success_with_deleteRefreshToken() {

    LoginRequestDto requestDto = new LoginRequestDto("test@test.com", "passwordForTest");

    Member member = Member.builder()
        .email("test@test.com")
        .password("encoded_password")
        .name("testName")
        .memberId(1L)
        .roles(Set.of(new Role(1L, UserRole.ROLE_USER), new Role(2L, UserRole.ROLE_ADMIN)))
        .gender(Gender.MALE)
        .regdate(LocalDateTime.now())
        .build();

    MemberUserDetails memberUserDetails = new MemberUserDetails(member);

    Authentication authentication
        = new UsernamePasswordAuthenticationToken(memberUserDetails, null);

    given(authenticationManager.authenticate(any())).willReturn(authentication);
    given(jwtUtils.issueToken(anyLong(), anyString(), anyString(), anySet(), anyString())).willReturn("createdAccessTokenForTest", "createdRefreshTokenForTest");
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setId(1L);
    given(refreshTokenService.selectRefreshTokenByMemberEmail(anyString())).willReturn(Optional.of(refreshToken));

    LoginResponseDto loginResponseDto = authenticationService.login(requestDto);

    assertThat(loginResponseDto).isNotNull()
        .hasFieldOrPropertyWithValue("email", requestDto.getEmail())
        .hasFieldOrPropertyWithValue("name", member.getName())
        .hasFieldOrPropertyWithValue("accessToken", "createdAccessTokenForTest")
        .hasFieldOrPropertyWithValue("refreshToken", "createdRefreshTokenForTest");

    then(refreshTokenService).should(times(1)).selectRefreshTokenByMemberEmail(requestDto.getEmail());
    then(refreshTokenService).should(times(1)).deleteRefreshTokenById(anyLong());
    then(refreshTokenService).should(times(1)).insertRefreshToken(any());
  }

  @Test
  void test_login_success_with_noDelete_refreshToken() {
    //given
    LoginRequestDto requestDto = new LoginRequestDto("test@test.com", "passwordForTest");

    Member member = Member.builder()
        .email("test@test.com")
        .password("encoded_password")
        .name("testName")
        .memberId(1L)
        .roles(Set.of(new Role(1L, UserRole.ROLE_USER), new Role(2L, UserRole.ROLE_ADMIN)))
        .gender(Gender.MALE)
        .regdate(LocalDateTime.now())
        .build();

    MemberUserDetails memberUserDetails = new MemberUserDetails(member);

    Authentication authentication
        = new UsernamePasswordAuthenticationToken(memberUserDetails, null);

    given(authenticationManager.authenticate(any())).willReturn(authentication);
    given(jwtUtils.issueToken(anyLong(), anyString(), anyString(), anySet(), anyString())).willReturn("createdAccessTokenForTest", "createdRefreshTokenForTest");

    given(refreshTokenService.selectRefreshTokenByMemberEmail(anyString())).willReturn(Optional.of(new RefreshToken()));
    //when
    LoginResponseDto loginResponseDto = authenticationService.login(requestDto);
    //then
    assertThat(loginResponseDto).isNotNull()
        .hasFieldOrPropertyWithValue("email", requestDto.getEmail())
        .hasFieldOrPropertyWithValue("name", member.getName())
        .hasFieldOrPropertyWithValue("accessToken", "createdAccessTokenForTest")
        .hasFieldOrPropertyWithValue("refreshToken", "createdRefreshTokenForTest");

    then(refreshTokenService).should(times(1)).selectRefreshTokenByMemberEmail(requestDto.getEmail());
    then(refreshTokenService).should(times(0)).deleteRefreshTokenById(anyLong());
    then(refreshTokenService).should(times(1)).insertRefreshToken(any());
  }


  @Test
  void test_selectRefreshToken() {
    //given
    String refreshTokenForSelect = "refreshToken_for_test";
    RefreshToken expectedRefreshToken = new RefreshToken();
    expectedRefreshToken.setToken("refreshToken_for_test");
    expectedRefreshToken.setMemberId(1L);
    expectedRefreshToken.setId(1L);
    given(refreshTokenService.selectRefreshTokenByTokenValue(anyString())).willReturn(Optional.of(expectedRefreshToken));

    //when
    Optional<RefreshToken> selectedRefreshToken = authenticationService.selectRefreshToken(refreshTokenForSelect);
    //then
    assertEquals(selectedRefreshToken, Optional.of(expectedRefreshToken));
    then(refreshTokenService).should(times(1)).selectRefreshTokenByTokenValue(refreshTokenForSelect);
  }

  @Test
  void reIssueAccessToken() {
    //given
    String refreshToken = "refreshToken";
    Claims claims = Jwts.claims()
        .subject("test@test.com")
        .issuedAt(new Date())
        .expiration(new Date(new Date().getTime() + 10000))
        .add(Map.of("name", "testName",
            "roles", List.of(UserRole.ROLE_USER.name()),
            "memberId", 1L))
        .build();

    System.out.println("claims = " + claims);
    given(jwtUtils.getClaimsFromRefreshToken(anyString())).willReturn(claims);
    given(jwtUtils.issueToken(anyLong(), anyString(), anyString(), anySet(), anyString())).willReturn("re_created_accessToken");
    //when
    LoginResponseDto loginResponseDto = authenticationService.reIssueAccessToken(refreshToken);

    //then
    assertThat(loginResponseDto).isNotNull()
        .hasFieldOrPropertyWithValue("name", claims.get("name"))
        .hasFieldOrPropertyWithValue("email", claims.getSubject())
        .hasFieldOrPropertyWithValue("accessToken", "re_created_accessToken")
        .hasFieldOrPropertyWithValue("refreshToken", refreshToken);
  }
}