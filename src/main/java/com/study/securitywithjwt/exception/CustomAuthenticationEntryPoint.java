package com.study.securitywithjwt.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.dto.ErrorDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }




  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
    log.info("entry point access ");
    if (authException instanceof JwtAuthenticationException exception) {
      //String exception = (String) request.getAttribute("exception");
      //if(exception!=null)
      log.error("authentication entry point");
      if (exception.getJwtExceptionType() == JwtExceptionType.EXPIRED_ACCESS_TOKEN) {
        log.error("token expired");
        setResponse(request, response, exception);
      } else if (exception.getJwtExceptionType() == JwtExceptionType.INVALID_TOKEN) {
        log.error("token invalid");
        setResponse(request, response, exception);
      } else if (exception.getJwtExceptionType() == JwtExceptionType.TOKEN_NOT_FOUND) {
        log.error("token not found");
        setResponse(request, response, exception);
      } else {
        log.error("unknown error regarding jwt occurred");
        setResponse(request, response, exception);
      }
    } else {
      setResponse(request, response, authException);
    }

  }

  private void setResponse(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException {

    if (exception instanceof JwtAuthenticationException jwtException) {
      response.setHeader("JwtException", jwtException.getJwtExceptionType().getCode());
    }

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    ErrorDto errorDto = ErrorDto.builder()
        .localDateTime(LocalDateTime.now())
        .message(exception.getMessage())
        .path(request.getRequestURI())
        .statusCode(HttpServletResponse.SC_UNAUTHORIZED)
        .build();

    try (PrintWriter writer = response.getWriter()) {
      objectMapper.writeValue(writer, errorDto);
    }
  }
}
