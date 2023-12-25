package com.study.securitywithjwt.exception;

import lombok.Getter;
//Jwt 관련 예외 처리를 위한 jwt 예외 관련 상수
public enum JwtExceptionType {
  TOKEN_NOT_FOUND("NOT_FOUND_TOKEN", "Token not found in header"),
  INVALID_TOKEN("INVALID_TOKEN", "Token is invalid"),
  EXPIRED_ACCESS_TOKEN("EXPIRED_ACCESS_TOKEN", "Access token is expired"),

  EXPIRED_REFRESH_TOKEN("EXPIRED_REFRESH_TOKEN", "Refresh token expired, reauthenticate needed"),
  UNKNOWN_ERROR("UNKNOWN_ERROR", "Exception regarding JWT occurred"),

  INVALID_SIGNATURE("INVALID_SIGNATURE", "Token contains Invalid signature");
  @Getter
  private final String code;
  @Getter
  private final String message;

  JwtExceptionType(String code, String message) {
    this.code = code;
    this.message = message;
  }


}
