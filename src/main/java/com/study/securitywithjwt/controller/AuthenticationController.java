package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.exception.ResourceNotFoundException;
import com.study.securitywithjwt.service.AuthenticationService;
import com.study.securitywithjwt.service.RefreshTokenService;
import com.study.securitywithjwt.utils.ControllerUtils;
import com.study.securitywithjwt.utils.annotation.LoggedInUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

//사용자 인증 클래스
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final RefreshTokenService refreshTokenService;


//loginRequestDto 로그인 할 사용자의 정보(email, password)
//bindingResult   데이터 바인딩 과정에서 발생한 에러
//request         http servlet request
//로그인 성공시 accessToken, refreshToken, email, name을 담은 LoginResponseDto 반환
//로그인 실패시 path, message, code, localDateTime을 담은 errorDto 반환
  @PostMapping("login")
  public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto loginRequestDto, BindingResult bindingResult, HttpServletRequest request) {
    ResponseEntity<Set<ErrorDto>> errorDtoSet = ControllerUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    log.info("Attempting authentication for the user with email. Email: {}", loginRequestDto.getEmail());
    LoginResponseDto response = authenticationService.login(loginRequestDto);
    log.info("Authentication success for the user with email. Email: {}", loginRequestDto.getEmail());
    return ResponseEntity.ok().body(response);
  }

  //access token 재 발급
  //refresh token을 기반으로, 회원 기본 정보 확인 후, access token 발급
  //refresh token이 만료된 경우, 응답 헤더에 JwtException: REFRESH_TOKEN_EXPIRED 를 추가해서 응답.
  @PostMapping("/refresh")
  public ResponseEntity<?> reIssueAccessToken(@RequestBody RefreshTokenDto refreshTokenDto) {

    log.info("Attempting to renew access token using the refresh token. Token: {}", refreshTokenDto.getToken());
    //received refresh token is nonexistent in db, throw exception
    RefreshToken refreshToken = refreshTokenService.selectRefreshTokenByTokenValue(refreshTokenDto.getToken())
        .orElseThrow(() -> new ResourceNotFoundException(
           String.format("Token does not exist in the database. Token: %s",refreshTokenDto.getToken())));

    //longin response Dto with reissued access token
    LoginResponseDto response = authenticationService.authenticateWithRefreshToken(refreshToken.getToken());

    log.info("Member: {} , Access token changed", response.getEmail());
    log.info("New access token: {}", response.getAccessToken());
    return ResponseEntity.ok().body(response);
  }

  //로그 아웃
  //db에 저장된 refresh token 삭제
  @DeleteMapping("/logout")
  public ResponseEntity<Void> logout(@LoggedInUserInfo MemberInfoInToken loggedInMember) {
    if (loggedInMember == null) {
      log.info("Logout called from a not logged-in user.");
      return ResponseEntity.badRequest().build();
    }
    log.info("Logout called. Member ID: {}", loggedInMember.getMemberId());
    refreshTokenService.deleteRefreshTokenByMemberId(loggedInMember.getMemberId());
    return new ResponseEntity<>(HttpStatus.OK);
  }
}