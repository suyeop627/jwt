package com.study.springsecurityboard.controller;

import com.study.springsecurityboard.domain.RefreshToken;
import com.study.springsecurityboard.dto.LoginRequestDto;
import com.study.springsecurityboard.dto.LoginResponseDto;
import com.study.springsecurityboard.dto.RefreshTokenDto;
import com.study.springsecurityboard.exception.ResourceNotFoundException;
import com.study.springsecurityboard.service.auth.AuthenticationService;
import com.study.springsecurityboard.service.refreshtoken.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    LoginResponseDto response = null;
    //is valid refresh token?
    //db저장 유무 확인
    RefreshToken refreshToken = authenticationService.searchRefreshToken(refreshTokenDto.getToken())
        .orElseThrow(() -> new ResourceNotFoundException("token doesn't exist in database"));

      //acees token 재발급
      response = authenticationService.reIssueAccessToken(refreshToken.getToken());

      log.info("===refresh token changed===");

      log.info("before : {}", refreshTokenDto.getToken());
      log.info("after : {}", response.getRefreshToken());

    return ResponseEntity.ok().body(response);
  }

  @DeleteMapping("/logout")
  public ResponseEntity logout(@RequestBody RefreshTokenDto refreshTokenDto) {
    log.info("logout called, token : {}", refreshTokenDto.getToken());
    refreshTokenService.deleteRefreshToken(refreshTokenDto.getToken());
    return ResponseEntity.ok().build();
  }

}