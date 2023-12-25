package com.study.securitywithjwt.service;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.LoginRequestDto;
import com.study.securitywithjwt.dto.LoginResponseDto;
import com.study.securitywithjwt.dto.LoginMemberInfo;
import com.study.securitywithjwt.exception.JwtAuthenticationException;
import com.study.securitywithjwt.exception.JwtExceptionType;
import com.study.securitywithjwt.jwt.JwtUtils;
import com.study.securitywithjwt.security.user.MemberDetails;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
  @Mock
  AuthenticationManager authenticationManager;
  @Mock
  JwtUtils jwtUtils;
  @Mock
  RefreshTokenService refreshTokenService;
  @InjectMocks //@Mock으로 지정한 클래스들 주입해서 테스트할 클래스의 인스턴스 생성해줌
  AuthenticationService authenticationService;

  @Nested
  class LoginSuccess {
    Member member;
    LoginRequestDto requestDto;
    Authentication authentication;

    @BeforeEach
    void setUp() {
      requestDto = new LoginRequestDto("test@test.com", "passwordForTest");

      member = Member.builder()
          .email("test@test.com")
          .password("encoded_password")
          .name("testName")
          .memberId(1L)
          .roles(Set.of(new Role(1L, UserRole.ROLE_USER), new Role(2L, UserRole.ROLE_ADMIN)))
          .gender(Gender.MALE)
          .regdate(LocalDateTime.now())
          .build();

      MemberDetails memberDetails = new MemberDetails(member);

      authentication
          = new UsernamePasswordAuthenticationToken(memberDetails, null);
    }

    @Test
    void login_tokenExistInDB_callDeleteRefreshTokenByIdAndReturnLoginResponseDto() {
      //given
      given(authenticationManager.authenticate(any())).willReturn(authentication);
      given(jwtUtils.issueToken(any(LoginMemberInfo.class), anyString())).willReturn("createdAccessTokenForTest", "createdRefreshTokenForTest");
      RefreshToken refreshToken = new RefreshToken();
      refreshToken.setId(1L);
      given(refreshTokenService.selectRefreshTokenByMemberEmail(anyString())).willReturn(Optional.of(refreshToken));

      //when
      LoginResponseDto loginResponseDto = authenticationService.login(requestDto);

      //then
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
    void login_nonexistentTokenInDB_returnLoginResponseDto() {
      //given
      given(authenticationManager.authenticate(any())).willReturn(authentication);
      given(jwtUtils.issueToken(any(LoginMemberInfo.class), anyString())).willReturn("createdAccessTokenForTest", "createdRefreshTokenForTest");
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

  }
  @Test
  void reIssueAccessToken_validState_returnLoginResponseDto() {
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

    given(jwtUtils.extractClaimsFromRefreshToken(anyString())).willReturn(claims);
    given(jwtUtils.issueToken(any(LoginMemberInfo.class), anyString())).willReturn("re_created_accessToken");

    //when
    LoginResponseDto loginResponseDto = authenticationService.reAuthenticateWithRefreshToken(refreshToken);

    //then
    assertThat(loginResponseDto).isNotNull()
        .hasFieldOrPropertyWithValue("name", claims.get("name"))
        .hasFieldOrPropertyWithValue("email", claims.getSubject())
        .hasFieldOrPropertyWithValue("accessToken", "re_created_accessToken")
        .hasFieldOrPropertyWithValue("refreshToken", refreshToken);
  }

  @Test
  void reIssueAccessToken_refreshTokenExpired_throwJwtExceptionWithHeader() {
    //given
    String expiredRefreshToken = "expired_refresh_token";
    given(jwtUtils.extractClaimsFromRefreshToken(anyString())).willThrow(ExpiredJwtException.class);

    //when, then
    assertThatThrownBy(() -> authenticationService.reAuthenticateWithRefreshToken(expiredRefreshToken))
        .isInstanceOf(JwtAuthenticationException.class).hasMessage(JwtExceptionType.EXPIRED_REFRESH_TOKEN.getMessage());

    then(refreshTokenService).should(times(1)).deleteRefreshTokenByToken(expiredRefreshToken);
  }
}