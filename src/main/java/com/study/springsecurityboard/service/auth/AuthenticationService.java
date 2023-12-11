package com.study.springsecurityboard.service.auth;

import com.study.springsecurityboard.domain.RefreshToken;
import com.study.springsecurityboard.dto.LoginRequestDto;
import com.study.springsecurityboard.dto.LoginResponseDto;
import com.study.springsecurityboard.dto.RefreshTokenDto;

import java.util.Optional;

public interface AuthenticationService {

  LoginResponseDto login(LoginRequestDto loginRequestDto);

  Optional<RefreshToken> searchRefreshToken(String refreshToken);

  LoginResponseDto reIssueAccessToken(String token);
}
