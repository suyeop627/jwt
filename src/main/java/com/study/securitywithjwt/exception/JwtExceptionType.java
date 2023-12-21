package com.study.securitywithjwt.exception;

import lombok.Getter;

public enum JwtExceptionType {
  TOKEN_NOT_FOUND("NOT_FOUND_TOKEN", "can not found token in header"),
  INVALID_TOKEN("INVALID_TOKEN", "token is invalid"),
  EXPIRED_ACCESS_TOKEN("EXPIRED_ACCESS_TOKEN", "access token is expired"),

  EXPIRED_REFRESH_TOKEN("EXPIRED_REFRESH_TOKEN", "refresh token expired, reauthenticate needed"),
  UNKNOWN_ERROR("UNKNOWN_ERROR", "error regarding JWT occurred");


  @Getter
  private final String code;
  @Getter
  private final String message;

  JwtExceptionType(String code, String message) {
    this.code = code;
    this.message = message;
  }


}
