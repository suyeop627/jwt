package com.study.securitywithjwt.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ControllerExceptionHandler {
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorDto> handleException(BadCredentialsException e, HttpServletRequest request) {

    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.UNAUTHORIZED.value());

    return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
  }


  @ExceptionHandler(InsufficientAuthenticationException.class)
  public ResponseEntity<ErrorDto> handleException(InsufficientAuthenticationException e, HttpServletRequest request) {

    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.FORBIDDEN.value());

    return new ResponseEntity<>(errorDto, HttpStatus.FORBIDDEN);
  }


  @ExceptionHandler(ResourceDuplicatedException.class)
  public ResponseEntity<ErrorDto> handleException(ResourceDuplicatedException e, HttpServletRequest request) {

    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.CONFLICT.value());

    return new ResponseEntity<>(errorDto, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorDto> handleException(ResourceNotFoundException e, HttpServletRequest request) {

    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.NOT_FOUND.value());

    return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ErrorDto> handleException(JwtException e, HttpServletRequest request) {

    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.UNAUTHORIZED.value());

    return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
  }




  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDto> handleException(Exception e, HttpServletRequest request) {

    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.INTERNAL_SERVER_ERROR.value());

    return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ErrorDto createErrorDto(HttpServletRequest request, Exception e, int errorCode){
    return ErrorDto.builder()
        .path(request.getRequestURI())
        .message(e.getMessage())
        .localDateTime(LocalDateTime.now())
        .statusCode(errorCode)
        .build();
  }

}