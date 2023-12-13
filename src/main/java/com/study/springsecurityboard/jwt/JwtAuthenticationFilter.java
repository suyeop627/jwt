package com.study.springsecurityboard.jwt;

import com.study.springsecurityboard.exception.JwtAuthenticationException;
import com.study.springsecurityboard.exception.JwtExceptionType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SignatureException;

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

    //access token이 필요 없는 경우(refresh token으로 처리되거나 token을 발급 받기 위한 요청)
    if (request.getRequestURI().equals("/auth/refresh") ||request.getRequestURI().equals("/auth/login")) {
      log.info("JwtAuthenticationFilter passed -> {} ", request.getRequestURI());
      filterChain.doFilter(request, response);
      return;
    }

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
      request.setAttribute("exception", JwtExceptionType.EXPIRED_TOKEN.getCode());
      log.error(JwtExceptionType.EXPIRED_TOKEN.getMessage());
      throw new JwtAuthenticationException("throw expired token exception");

    } catch (NullPointerException  | IllegalArgumentException e) {
      log.error(JwtExceptionType.TOKEN_NOT_FOUND.getMessage());
      request.setAttribute("exception", JwtExceptionType.TOKEN_NOT_FOUND.getCode());
      throw new JwtAuthenticationException("throw token not found exception");

    } catch (MalformedJwtException e){
      log.error(JwtExceptionType.INVALID_TOKEN.getMessage());
      request.setAttribute("exception", JwtExceptionType.INVALID_TOKEN.getCode());

      throw new JwtAuthenticationException("throw malformed token exception");

    } catch (Exception e){
      log.error("error occurred in jwtAuthenticationFilter");
      log.error("exception message : {}", e.getMessage());
      e.printStackTrace();
      request.setAttribute("exception", JwtExceptionType.UNKNOWN_ERROR.getCode());
      throw new JwtAuthenticationException("throw malformed token exception");
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
