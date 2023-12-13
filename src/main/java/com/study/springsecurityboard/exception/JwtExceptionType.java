package com.study.springsecurityboard.exception;

import lombok.Getter;

public enum JwtExceptionType {
  TOKEN_NOT_FOUND("NOT_FOUND_TOKEN", "can not found token in header"),
  INVALID_TOKEN("INVALID_TOKEN", "token is invalid"),
  EXPIRED_TOKEN("EXPIRED_TOKEN", "token is expired"),

  UNKNOWN_ERROR("UNKNOWN_ERROR", "unknown error occurred");

  @Getter
  private final String code;
  @Getter
  private final String message;

  JwtExceptionType(String code, String message) {
    this.code = code;
    this.message = message;
  }


}
