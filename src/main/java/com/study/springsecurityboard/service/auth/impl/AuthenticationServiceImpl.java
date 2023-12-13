package com.study.springsecurityboard.service.auth.impl;

import com.study.springsecurityboard.domain.RefreshToken;
import com.study.springsecurityboard.domain.Role;
import com.study.springsecurityboard.dto.LoginRequestDto;
import com.study.springsecurityboard.dto.LoginResponseDto;
import com.study.springsecurityboard.jwt.JwtUtils;
import com.study.springsecurityboard.service.auth.AuthenticationService;
import com.study.springsecurityboard.domain.Member;
import com.study.springsecurityboard.security.user.MemberUserDetails;
import com.study.springsecurityboard.service.refreshtoken.RefreshTokenService;
import com.study.springsecurityboard.utils.member.LoginStatus;
import com.study.springsecurityboard.utils.member.UserRole;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final RefreshTokenService refreshTokenService;
  @Value("${jwt.type.accessToken}")
  private String TYPE_ACCESS;
  @Value("${jwt.type.refreshToken}")
  private String TYPE_REFRESH;

  @Override
  public LoginResponseDto login(LoginRequestDto loginRequestDto) {
    //첫 로그인 시, username과 password로 해당유저가 존재하는지 확인하므로, security가 기본으로 제공하는 UsernamePasswordAuthenticationToken 사용함
    //토큰이 실려 오는경우엔 filter로만 처리.
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequestDto.getEmail(), loginRequestDto.getPassword())
    );
    Optional<RefreshToken> refreshTokenSavedInDB = refreshTokenService.searchRefreshTokenByMemberEmail(loginRequestDto.getEmail());

    refreshTokenSavedInDB.ifPresent(refreshToken -> refreshTokenService.deleteRefreshTokenById(refreshToken.getId()));

    Member member = ((MemberUserDetails) authentication.getPrincipal()).getMember();


    Set<String> roles = member.getRoles().stream().map(role->role.getName().name()).collect(Collectors.toSet());


    String accessToken = jwtUtils.issueToken(member.getMemberId(), member.getEmail(), member.getName(), roles, TYPE_ACCESS);
    String refreshToken = jwtUtils.issueToken(member.getMemberId(), member.getEmail(), member.getName(), roles, TYPE_REFRESH);

    log.info("Created access token : {} ", accessToken);
    log.info("Created refresh token : {}", refreshToken);

    RefreshToken refreshTokenInstance = new RefreshToken();
    refreshTokenInstance.setMemberId(member.getMemberId());
    refreshTokenInstance.setToken(refreshToken);

    refreshTokenService.insertRefreshToken(refreshTokenInstance);

    return LoginResponseDto.builder()
        .name(member.getName())
        .email(member.getEmail())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }
  @Override
  public Optional<RefreshToken> searchRefreshToken(String refreshToken) {
    return refreshTokenService.searchRefreshTokenByTokenValue(refreshToken);
  }

  @Override
  public LoginResponseDto reIssueAccessToken(String refreshToken) {
    Claims claimsFromRefreshToken = jwtUtils.getClaimsFromRefreshToken(refreshToken);
    String name = claimsFromRefreshToken.get("name", String.class);
    String subject = claimsFromRefreshToken.getSubject();
    Long memberId = claimsFromRefreshToken.get("memberId", Long.class);


    List<String> roleFromClaims = (List<String>) claimsFromRefreshToken.get("roles");

    Set<String> roles = new HashSet<>(roleFromClaims);

    String accessToken = jwtUtils.issueToken(memberId, subject, name, roles, TYPE_ACCESS);

    return LoginResponseDto.builder()
        .name(name)
        .email(subject)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }
}
