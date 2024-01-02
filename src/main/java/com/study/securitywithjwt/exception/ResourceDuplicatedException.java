package com.study.securitywithjwt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
//unique 속성을 가진 필드의 입력값이 기존 저장값들과 중복된 경우
@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceDuplicatedException extends RuntimeException{
  public ResourceDuplicatedException(String message) {
    super(message);
  }
}
