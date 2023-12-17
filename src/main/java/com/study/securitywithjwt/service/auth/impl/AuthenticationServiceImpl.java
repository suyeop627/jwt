package com.study.securitywithjwt.service.auth.impl;

import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.dto.LoginRequestDto;
import com.study.securitywithjwt.dto.LoginResponseDto;
import com.study.securitywithjwt.jwt.JwtUtils;
import com.study.securitywithjwt.service.auth.AuthenticationService;
import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.security.user.MemberUserDetails;
import com.study.securitywithjwt.service.refreshtoken.RefreshTokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final String TYPE_ACCESS = "ACCESS";
  private final String TYPE_REFRESH = "REFRESH";

  @Override
  public LoginResponseDto login(LoginRequestDto loginRequestDto) {
    //첫 로그인 시, username과 password로 해당유저가 존재하는지 확인하므로, security가 기본으로 제공하는 UsernamePasswordAuthenticationToken 사용함
    //토큰이 실려 오는경우엔 filter로만 처리. - principal -> Member(db에서 가져온 걸 그대로 저장함)
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequestDto.getEmail(), loginRequestDto.getPassword())
    );
    Optional<RefreshToken> refreshTokenSavedInDB = refreshTokenService.selectRefreshTokenByMemberEmail(loginRequestDto.getEmail());

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
  public Optional<RefreshToken> selectRefreshToken(String refreshToken) {
    return refreshTokenService.selectRefreshTokenByTokenValue(refreshToken);
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
