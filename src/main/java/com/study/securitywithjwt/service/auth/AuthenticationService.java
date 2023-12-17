package com.study.securitywithjwt.service.auth;

import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.dto.LoginRequestDto;
import com.study.securitywithjwt.dto.LoginResponseDto;

import java.util.Optional;

public interface AuthenticationService {

  LoginResponseDto login(LoginRequestDto loginRequestDto);

  Optional<RefreshToken> selectRefreshToken(String refreshToken);

  LoginResponseDto reIssueAccessToken(String token);
}
