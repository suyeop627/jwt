package com.study.securitywithjwt.service;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.dto.LoginRequestDto;
import com.study.securitywithjwt.dto.LoginResponseDto;
import com.study.securitywithjwt.dto.LoginMemberInfo;
import com.study.securitywithjwt.exception.JwtAuthenticationException;
import com.study.securitywithjwt.exception.JwtExceptionType;
import com.study.securitywithjwt.jwt.JwtUtils;
import com.study.securitywithjwt.security.user.MemberDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

//사용자 인증(로그인 및 토큰) 관련 처리 담당 클래스.
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final RefreshTokenService refreshTokenService;
  private final String TYPE_ACCESS = "ACCESS";
  private final String TYPE_REFRESH = "REFRESH";

  //로그인 처리
  //로그인 성공 시, access token, refresh token 및 사용자 정보가 저장된 LoginResponseDto 반환
  public LoginResponseDto login(LoginRequestDto loginRequestDto) {

    LoginMemberInfo loginMemberInfo = getAuthenticatedLoginMemberInfo(loginRequestDto);

    deleteRefreshTokenIfExists(loginRequestDto);

    String accessToken = jwtUtils.issueToken(loginMemberInfo, TYPE_ACCESS);
    log.info("Access token for member(memberId: {}) generated. Access token : {}", loginMemberInfo.getMemberId(), accessToken);

    String refreshToken = jwtUtils.issueToken(loginMemberInfo, TYPE_REFRESH);
    log.info("Refresh token for member(memberId: {}) generated. Refresh token : {}", loginMemberInfo.getMemberId(), refreshToken);

    saveRefreshTokenOfLoginMember(loginMemberInfo.getMemberId(), refreshToken);

    return LoginResponseDto.builder()
        .name(loginMemberInfo.getName())
        .email(loginMemberInfo.getEmail())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  //loginRequestDto을 사용하여 인증 성공 시, LoginMemberInfo 을 반환함.
  private LoginMemberInfo getAuthenticatedLoginMemberInfo(LoginRequestDto loginRequestDto) {

    Authentication authentication = generateAuthenticationFromLoginRequestDto(loginRequestDto);

    //Member entity를 필드로 가지는 MemberDetails를 principal로 사용함.
    MemberDetails principal = (MemberDetails) authentication.getPrincipal();

    Member member = principal.getMember();

    log.info("Authentication for member(memberId: {}) generated. Authentication type: {}, principal: {}",
        member.getMemberId(), authentication.getClass(), authentication.getPrincipal());

    return LoginMemberInfo.builder()
        .memberId(member.getMemberId())
        .roles(member.getRoleNameSet())
        .email(member.getEmail())
        .name(member.getName())
        .build();
  }

  //email과 password로 인증 처리 후, Authentication 반환.
  private Authentication generateAuthenticationFromLoginRequestDto(LoginRequestDto loginRequestDto) {
    //첫 로그인 시, username과 password로 해당유저가 존재하는지 확인하므로, security가 기본으로 제공하는 UsernamePasswordAuthenticationToken 사용함
    return authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequestDto.getEmail(), loginRequestDto.getPassword())
    );
  }

  //로그인 시 기존에 저장돼있는 refresh token이 존재할 경우 삭제처리.
  private void deleteRefreshTokenIfExists(LoginRequestDto loginRequestDto) {
    refreshTokenService
        .selectRefreshTokenByMemberEmail(loginRequestDto.getEmail())
        .ifPresent(refreshToken -> refreshTokenService.deleteRefreshTokenById(refreshToken.getId()));
  }

  //로그인 시, 인증이 성공된 회원의 refresh token을 db에 저장.
  private void saveRefreshTokenOfLoginMember(Long memberId, String refreshToken) {
    RefreshToken refreshTokenOfLoginMember =
        RefreshToken.builder()
            .token(refreshToken)
            .memberId(memberId)
            .expiredAt(LocalDateTime.now())
            .build();
    refreshTokenService.insertRefreshToken(refreshTokenOfLoginMember);
  }

  //refresh token을 사용해서 access token 재발행 및 loginResponseDto 반환
  @Transactional
  public LoginResponseDto reAuthenticateWithRefreshToken(String refreshToken) {

    Claims claimsFromRefreshToken = getClaimsOfRefreshToken(refreshToken);

    LoginMemberInfo loginMemberInfo = generateLoginMemberInfoFromClaims(claimsFromRefreshToken);

    String accessToken = jwtUtils.issueToken(loginMemberInfo, TYPE_ACCESS);

    log.info("Access token for member(memberId: {}) re-generated from Refresh token. Access token : {}", loginMemberInfo.getMemberId(), accessToken);
    return LoginResponseDto.builder()
        .name(loginMemberInfo.getName())
        .email(loginMemberInfo.getEmail())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  //Claims에서 회원정보를 추출하여, LoginMemberInfo 반환
  private LoginMemberInfo generateLoginMemberInfoFromClaims(Claims claimsFromRefreshToken) {
    List<String> roleFromClaims = (List<String>) claimsFromRefreshToken.get("roles");

    return LoginMemberInfo.builder()
        .memberId(claimsFromRefreshToken.get("memberId", Long.class))
        .email(claimsFromRefreshToken.getSubject())
        .name(claimsFromRefreshToken.get("name", String.class))
        .roles(new HashSet<>(roleFromClaims))
        .build();
  }

  //refresh token에서 추출한 클레임을 반환
  private Claims getClaimsOfRefreshToken(String refreshToken) {
    Claims claimsOfRefreshToken = null;
    try {
      claimsOfRefreshToken = jwtUtils.extractClaimsFromRefreshToken(refreshToken);

    } catch (ExpiredJwtException e) {
      deleteRefreshTokenAndThrow(JwtExceptionType.EXPIRED_REFRESH_TOKEN, refreshToken);
    } catch (NullPointerException | IllegalArgumentException e) {
      deleteRefreshTokenAndThrow(JwtExceptionType.TOKEN_NOT_FOUND, refreshToken);
    } catch (MalformedJwtException e) {
      deleteRefreshTokenAndThrow(JwtExceptionType.INVALID_TOKEN, refreshToken);
    } catch (SignatureException e) {
      deleteRefreshTokenAndThrow(JwtExceptionType.INVALID_SIGNATURE, refreshToken);
    } catch (Exception e) {
      //전달받은 토큰을 parsing 할때 기타 예외가 발생한 경우 기존 토큰 삭제 및 예외 처리
      log.error("Unspecified exception occurred when parsing the token");
      deleteRefreshTokenAndThrow(JwtExceptionType.UNKNOWN_ERROR, refreshToken);
    }
    return claimsOfRefreshToken;
  }

  //전달받은 refresh token을 db에서 삭제하고, JwtException을 던짐
  private void deleteRefreshTokenAndThrow(JwtExceptionType jwtExceptionType, String refreshToken) {
    log.error(jwtExceptionType.getMessage());
    refreshTokenService.deleteRefreshTokenByToken(refreshToken);
    throw new JwtAuthenticationException(jwtExceptionType.getMessage(), jwtExceptionType);
  }
}
