package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.exception.ResourceNotFoundException;
import com.study.securitywithjwt.service.AuthenticationService;
import com.study.securitywithjwt.service.RefreshTokenService;
import com.study.securitywithjwt.utils.ControllerUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
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
  @PostMapping
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
  @PutMapping
  public ResponseEntity<?> reIssueAccessToken(@RequestBody RefreshTokenDto refreshTokenDto) {

    log.info("Attempting to renew access token using the refresh token. Token: {}", refreshTokenDto.getRefreshToken());
    //received refresh token is nonexistent in db, throw exception
    findRefreshTokenOrThrow(refreshTokenDto.getRefreshToken());

    //longin response Dto with reissued access token
    LoginResponseDto response = authenticationService.reAuthenticateWithRefreshToken(refreshTokenDto.getRefreshToken());

    log.info("Member: {} , Access token changed", response.getEmail());
    log.info("New access token: {}", response.getAccessToken());
    return ResponseEntity.ok().body(response);
  }

  private void findRefreshTokenOrThrow(String refreshToken) {
    refreshTokenService.selectRefreshTokenByTokenValue(refreshToken)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format("The refresh token does not exist in the database.")));
  }

  //로그 아웃
  //db에 저장된 refresh token 삭제
  @DeleteMapping
//  public ResponseEntity<Void> logout(@TokenToMemberInfo LoginMemberInfo loggedInMember) {
  //access token 과 refresh token이 둘다 만료된 경우에서 로그아웃을 호출할 경우, 로그아웃을 위해 재인증을 받아야 함.->인층 없이 토큰을 전달받아 해당 토큰을 삭제하도록 변경
  public ResponseEntity<Void> logout(HttpServletRequest request) throws BadRequestException {
    String authorization =request.getHeader("Authorization");
    if(authorization!=null){
      String refreshToken = authorization.split(" ")[1];
      log.info("Logout called. Token: {}", refreshToken);
      refreshTokenService.deleteRefreshTokenByToken(refreshToken);
    }else{
      throw new BadRequestException("Refresh token required when calling logout");
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }
}