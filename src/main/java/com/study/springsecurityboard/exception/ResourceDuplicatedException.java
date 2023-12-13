package com.study.springsecurityboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceDuplicatedException extends RuntimeException{
  public ResourceDuplicatedException(String message) {
    super(message);
  }
}
