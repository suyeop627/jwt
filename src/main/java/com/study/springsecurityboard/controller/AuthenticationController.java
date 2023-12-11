package com.study.springsecurityboard.controller;

import com.study.springsecurityboard.domain.RefreshToken;
import com.study.springsecurityboard.dto.LoginRequestDto;
import com.study.springsecurityboard.dto.LoginResponseDto;
import com.study.springsecurityboard.dto.RefreshTokenDto;
import com.study.springsecurityboard.repository.RefreshTokenRepository;
import com.study.springsecurityboard.service.auth.AuthenticationService;
import com.study.springsecurityboard.service.refreshtoken.RefreshTokenService;
import com.study.springsecurityboard.service.refreshtoken.impl.RefreshTokenServiceImpl;
import com.study.springsecurityboard.utils.LoginStatus;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

  public final AuthenticationService authenticationService;
  public final RefreshTokenService refreshTokenService;

  @PostMapping("login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    log.info("Attempting authentication for user {}, password {}", loginRequestDto.getEmail(), loginRequestDto.getPassword());

    LoginResponseDto response = authenticationService.login(loginRequestDto);

    return ResponseEntity.ok().body(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponseDto> reIssueAccessToken(@RequestBody RefreshTokenDto refreshTokenDto) {

    log.info("Attempting renew access token with refresh token {}", refreshTokenDto.getToken());

    //is valid refresh token?
    //db저장 유무 확인
    RefreshToken refreshToken = authenticationService.searchRefreshToken(refreshTokenDto.getToken())
        .orElseThrow(() -> new IllegalArgumentException("token doesn't exist in database"));

    LoginResponseDto response = null;

    //만료여부 확인
    try {
      //acees token 재발급
      response = authenticationService.reIssueAccessToken(refreshToken.getToken());

      log.info("===refresh token changed===");

      log.info("before : {}", refreshTokenDto.getToken());
      log.info("after : {}", response.getRefreshToken());

    } catch (ExpiredJwtException e) {
      //todo : 로그인 재요청하기
      response = LoginResponseDto.builder().status(LoginStatus.REFRESH_TOKEN_EXPIRED).build();
    } catch (Exception e) {
      e.printStackTrace();
      //response = LoginResponseDto.builder().status(LoginStatus.REFRESH_TOKEN_EXPIRED).build();
      //todo : 처리 따로 해야함
    }
    return ResponseEntity.ok().body(response);
  }

  @DeleteMapping("/logout")
  public ResponseEntity logout(@RequestBody RefreshTokenDto refreshTokenDto) {
    refreshTokenService.deleteRefreshToken(refreshTokenDto.getToken());
    return new ResponseEntity(HttpStatus.OK);
  }

}