package com.study.securitywithjwt.exception;

import org.springframework.security.core.AuthenticationException;
//filter에서 발생하는 authenticationException 중 JwtAuthenticatonException처리
public class JwtAuthenticationException extends AuthenticationException {
  private final JwtExceptionType jwtExceptionType;

  public JwtAuthenticationException(String msg, JwtExceptionType jwtExceptionType){
    super(msg);
    this.jwtExceptionType = jwtExceptionType;
  }
  public JwtAuthenticationException(JwtExceptionType jwtExceptionType){
    super(jwtExceptionType.getMessage());
    this.jwtExceptionType = jwtExceptionType;
  }
  public JwtExceptionType getJwtExceptionType() {
    return jwtExceptionType;
  }
}
