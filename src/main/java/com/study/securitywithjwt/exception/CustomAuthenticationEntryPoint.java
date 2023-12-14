package com.study.securitywithjwt.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
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
    String exception = (String) request.getAttribute("exception");
    if (exception != null) {
      log.error("authentication entry point");
      if (exception.equals(JwtExceptionType.EXPIRED_TOKEN.getCode())) {
        log.error("token expired");
        setResponse(request, response, JwtExceptionType.EXPIRED_TOKEN);
      } else if (exception.equals(JwtExceptionType.INVALID_TOKEN.getCode())) {
        log.error("token invalid");
        setResponse(request, response, JwtExceptionType.INVALID_TOKEN);
      } else if (exception.equals(JwtExceptionType.TOKEN_NOT_FOUND.getCode())) {
        log.error("token not found");
        setResponse(request, response, JwtExceptionType.TOKEN_NOT_FOUND);
      } else {
        log.error("unknown error");
        setResponse(request, response, JwtExceptionType.UNKNOWN_ERROR);
      }
    }else{
      setResponse(request, response, authException.getMessage());
    }

  }

  private void setResponse(HttpServletRequest request, HttpServletResponse response, JwtExceptionType jwtException) throws IOException {

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setHeader("jwtException", jwtException.getCode());
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    ErrorDto errorDto = ErrorDto.builder()
        .localDateTime(LocalDateTime.now())
        .message(jwtException.getMessage())
        .path(request.getRequestURI())
        .statusCode(HttpServletResponse.SC_UNAUTHORIZED)
        .build();

    try (PrintWriter writer = response.getWriter()) { //try with resource - flush(), close()를 자동으로 호출해줌.
      objectMapper.writeValue(writer, errorDto);
    }
  }


  private void setResponse(HttpServletRequest request, HttpServletResponse response, String exceptionMessage) throws IOException {
    try (OutputStream outputStream = response.getOutputStream()) { //try with resource - close()를 자동으로 호출해줌.
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      ErrorDto errorDto = ErrorDto.builder()
          .localDateTime(LocalDateTime.now())
          .message(exceptionMessage)
          .path(request.getRequestURI())
          .statusCode(HttpServletResponse.SC_UNAUTHORIZED)
          .build();

      new ObjectMapper().writeValue(outputStream,errorDto);
    }
  }
}
