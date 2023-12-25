package com.study.securitywithjwt.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.dto.ErrorDto;
import com.study.securitywithjwt.utils.LoggingUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
//Spring Security의 filter에서 발생한 AuthenticationException 처리 클래스
@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;
  @Value("${jwt.exception.response.header}")
  private String JWT_EXCEPTION_HEADER;


  public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
    log.info("entry point access ");
    if (authException instanceof JwtAuthenticationException jwtException) {
      //String exception = (String) request.getAttribute("exception");
      //if(exception!=null) -> filter에서 entry point 주입받아 바로 호출하도록 처리함.
      //수정 전 : request.getAttribute()로 예외 종류 판별 후 if - elsif로 분류
      //수정 후 : jwtAuthenticationException 생성 후, getJwtExceptionType() 메서드로 일괄 처리
      log.error(jwtException.getJwtExceptionType().getMessage());
      response.setHeader(JWT_EXCEPTION_HEADER, jwtException.getJwtExceptionType().getCode());
    }
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    ErrorDto errorDto = ErrorDto.builder()
        .localDateTime(LocalDateTime.now())
        .message(authException.getMessage())
        .path(request.getRequestURI())
        .statusCode(HttpServletResponse.SC_UNAUTHORIZED)
        .build();

    LoggingUtils.loggingErrorDto(errorDto);

    try (PrintWriter writer = response.getWriter()) {
      objectMapper.writeValue(writer, errorDto);
    }
  }
}
