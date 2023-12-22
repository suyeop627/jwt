package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.exception.ResourceNotFoundException;
import com.study.securitywithjwt.service.AuthenticationService;
import com.study.securitywithjwt.service.RefreshTokenService;
import com.study.securitywithjwt.utils.RequestValidationUtils;
import com.study.securitywithjwt.utils.annotation.LoggedInUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    ResponseEntity<Set<ErrorDto>> errorDtoSet = RequestValidationUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    log.info("Attempting authentication for user {}, password {}", loginRequestDto.getEmail(), loginRequestDto.getPassword());

    LoginResponseDto response = authenticationService.login(loginRequestDto);

    return ResponseEntity.ok().body(response);
  }

  //access token 재 발급 end point
  //refresh token을 기반으로, 회원 기본 정보 확인 후, access token 발급
  //refresh token이 만료된 경우, 응답 헤더에 JwtException: REFRESH_TOKEN_EXPIRED 를 추가해서 응답.
  @PostMapping("/refresh")
  public ResponseEntity<?> reIssueAccessToken(@RequestBody RefreshTokenDto refreshTokenDto) {

    log.info("Attempting renew access token with refresh token {}", refreshTokenDto.getToken());

    //received refresh token is nonexistent in db, throw exception
    RefreshToken refreshToken = refreshTokenService.selectRefreshTokenByTokenValue(refreshTokenDto.getToken())
        .orElseThrow(() -> new ResourceNotFoundException("token doesn't exist in database"));

    //longin response Dto with reissued access token
    LoginResponseDto response = authenticationService.authenticateWithRefreshToken(refreshToken.getToken());

    log.info("member : {} , access token changed", response.getEmail());
    return ResponseEntity.ok().body(response);
  }

  //로그 아웃
  //db에 저장된 refresh token 삭제
  @DeleteMapping("/logout")
  @Transactional
  public ResponseEntity logout(@LoggedInUserInfo MemberInfoInToken loggedInMember) {
    if (loggedInMember == null) {
      log.info("logout called from not logged in user");
      return ResponseEntity.badRequest().build();
    }
    log.info("logout called, memberId : {}", loggedInMember.getMemberId());
    refreshTokenService.deleteRefreshTokenByMemberId(loggedInMember.getMemberId());
    return ResponseEntity.ok().build();
  }
}