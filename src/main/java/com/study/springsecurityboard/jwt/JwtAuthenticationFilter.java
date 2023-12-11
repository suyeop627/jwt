package com.study.springsecurityboard.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.springsecurityboard.utils.member.LoginStatus;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/*jwt 인증처리
 * Authentication이 있는지 ?
 * Token이 있는지?
 *   *토큰은 유효한지
 * */
@RequiredArgsConstructor
@Configuration
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtAuthenticationProvider jwtAuthenticationProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    log.info("JwtAuthenticationFilter activated");

    if (request.getRequestURI().equals("/auth/login") || request.getRequestURI().equals("/auth/refresh")) {
      log.info("JwtAuthenticationFilter passed because this request is for login or refresh token ");
      filterChain.doFilter(request, response);
      return;
    }

    //토큰 유무 확인 -> 토큰 없으면 null 리턴
    try {
      String token = getTokenFromRequest(request);
      if(token==null){
        filterChain.doFilter(request, response);
        return;
      }
      JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(token);

      Authentication authentication = jwtAuthenticationProvider.authenticate(jwtAuthenticationToken);

      SecurityContextHolder.getContext().setAuthentication(authentication);

      filterChain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
      log.error("Access Token received was expired");
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      Map<String, String> errorMap = Map.of("error", e.getMessage(), "status", LoginStatus.ACCESS_TOKEN_EXPIRED.name());

      new ObjectMapper().writeValue(response.getOutputStream(), errorMap);

    } catch (Exception e) {

      log.error("Error occurred : {}", e.getMessage());
      e.printStackTrace();
      log.error(String.valueOf(e.getCause()));
      response.setHeader("error", e.getMessage());
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      Map<String, String> errorMap = Map.of("error", e.getMessage());
      new ObjectMapper().writeValue(response.getOutputStream(), errorMap);
    }

  }

  private String getTokenFromRequest(HttpServletRequest request) {
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return null;
    }
    return authorizationHeader.split(" ")[1];
  }
}
