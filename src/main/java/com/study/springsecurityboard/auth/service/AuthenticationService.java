package com.study.springsecurityboard.auth.service;

import com.study.springsecurityboard.auth.dto.LoginRequestDto;
import com.study.springsecurityboard.auth.dto.LoginResponseDto;

public interface AuthenticationService {

  LoginResponseDto login(LoginRequestDto loginRequestDto);

}
