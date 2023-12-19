package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.dto.LoginRequestDto;
import com.study.securitywithjwt.dto.LoginResponseDto;
import com.study.securitywithjwt.dto.MemberInfo;
import com.study.securitywithjwt.dto.RefreshTokenDto;
import com.study.securitywithjwt.exception.ErrorDto;
import com.study.securitywithjwt.exception.ResourceNotFoundException;
import com.study.securitywithjwt.service.auth.AuthenticationService;
import com.study.securitywithjwt.service.refreshtoken.RefreshTokenService;
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


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final RefreshTokenService refreshTokenService;

  @PostMapping("login")
  public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto loginRequestDto, BindingResult bindingResult, HttpServletRequest request) {
    ResponseEntity<Set<ErrorDto>> errorDtoSet = RequestValidationUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

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
    RefreshToken refreshToken = authenticationService.selectRefreshToken(refreshTokenDto.getToken())
        .orElseThrow(() -> new ResourceNotFoundException("token doesn't exist in database"));

      //acees token 재발급
      response = authenticationService.reIssueAccessToken(refreshToken.getToken());

      log.info("===refresh token changed===");

      log.info("before : {}", refreshTokenDto.getToken());
      log.info("after : {}", response.getRefreshToken());

    return ResponseEntity.ok().body(response);
  }

  @DeleteMapping("/logout")
  @Transactional
  public ResponseEntity logout(@LoggedInUserInfo MemberInfo loggedInMember) {
    if(loggedInMember==null){
      log.info("logout called from not logged in user");
      return ResponseEntity.badRequest().build();
    }
    log.info("logout called, memberId : {}", loggedInMember.getMemberId());
    refreshTokenService.deleteRefreshTokenByMemberId(loggedInMember.getMemberId());
    return ResponseEntity.ok().build();
  }
}