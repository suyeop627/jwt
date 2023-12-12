package com.study.springsecurityboard.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
    String exception = (String) request.getAttribute("exception");
    if(exception!=null){
      log.error("authentication entry point");
      if(exception.equals(JwtExceptionType.EXPIRED_TOKEN.getCode())){
        log.error("token expired");
        setResponse(response, JwtExceptionType.EXPIRED_TOKEN);
      }else if(exception.equals(JwtExceptionType.INVALID_TOKEN.getCode())){
        log.error("token invalid");
        setResponse(response, JwtExceptionType.INVALID_TOKEN);
      }else if(exception.equals(JwtExceptionType.TOKEN_NOT_FOUND.getCode())){
        log.error("token not found");
        setResponse(response, JwtExceptionType.TOKEN_NOT_FOUND);
      }else{
        log.error("unknown error");
        setResponse(response, JwtExceptionType.UNKNOWN_ERROR);
      }
    }

  }

  private void setResponse(HttpServletResponse response, JwtExceptionType jwtExceptionType) throws IOException {
    try (OutputStream outputStream = response.getOutputStream()) { //try with resource - close()를 자동으로 호출해줌.
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      Map<String, Object> errorMap = new HashMap<>();
      errorMap.put("code", jwtExceptionType.getCode());
      errorMap.put("message", jwtExceptionType.getMessage());
      new ObjectMapper().writeValue(outputStream, errorMap);
    }
  }
}
