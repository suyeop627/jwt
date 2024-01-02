package com.study.securitywithjwt.exception;

import org.springframework.security.core.AuthenticationException;
//filter에서 발생하는 authenticationException 중 JwtAuthenticatonException처리
// enum 클래스인 JwtExceptionType 을 활용하여 JwtException 관련 처리를 하기 위해 생성한 예외
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
