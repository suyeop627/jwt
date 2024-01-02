package com.study.securitywithjwt.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class RequestHandlerUtils {
  public static String getHttpMethodAndURI(HttpServletRequest request){
    return request.getMethod() + " " + request.getRequestURI();
  }
}
