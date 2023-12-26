package com.study.securitywithjwt.exception;

import com.study.securitywithjwt.dto.ErrorDto;
import com.study.securitywithjwt.utils.LoggingUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static com.study.securitywithjwt.utils.RequestHandlerUtils.getHttpMethodAndURI;

//Controller 에서 발생하는 예외 처리 클래스
@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

  @Value("${jwt.exception.response.header}")
  private String JWT_EXCEPTION_HEADER;

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorDto> handleException(BadCredentialsException e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.UNAUTHORIZED.value());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
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
    if(e instanceof ExpiredJwtException){
      return createExpiredJwtResponse(errorDto, JwtExceptionType.EXPIRED_ACCESS_TOKEN);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
  }

  @ExceptionHandler(JwtAuthenticationException.class)
  public ResponseEntity<ErrorDto> handleException(JwtAuthenticationException e, HttpServletRequest request){
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.UNAUTHORIZED.value());
    if(e.getJwtExceptionType()==JwtExceptionType.EXPIRED_REFRESH_TOKEN){
      return createExpiredJwtResponse(errorDto, JwtExceptionType.EXPIRED_REFRESH_TOKEN);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorDto> handleException(AccessDeniedException e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.FORBIDDEN.value());
    return new ResponseEntity<>(errorDto, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDto> handleException(Exception e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.INTERNAL_SERVER_ERROR.value());
    return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ErrorDto createErrorDto(HttpServletRequest request, Exception e, int errorCode){
    ErrorDto errorDto = ErrorDto.builder()
        .path(getHttpMethodAndURI(request))
        .message(e.getMessage())
        .localDateTime(LocalDateTime.now())
        .statusCode(errorCode)
        .build();

    LoggingUtils.loggingErrorDto(errorDto);
    return errorDto;
  }
  private ResponseEntity<ErrorDto> createExpiredJwtResponse(ErrorDto errorDto, JwtExceptionType jwtExceptionType) {
    log.error("{}. Exception code attached to header. JwtException: {}", jwtExceptionType.getMessage(), jwtExceptionType.getStatus());
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .header(JWT_EXCEPTION_HEADER, jwtExceptionType.getStatus()) //EXPIRED_ACCESS_TOKEN or EXPIRED_REFRESH_TOKEN
        .body(errorDto);
  }
}
