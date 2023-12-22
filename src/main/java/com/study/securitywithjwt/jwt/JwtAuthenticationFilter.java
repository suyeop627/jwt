package com.study.securitywithjwt.jwt;

import com.study.securitywithjwt.exception.JwtAuthenticationException;
import com.study.securitywithjwt.exception.JwtExceptionType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

  /*
  * jwt 관련 예외가 발생하더라도 POST /error로 리다이렉트 함.
  * /error에 리다이렉트 될 경우, header에 토큰을 포함하지 않으므로 항상 InsufficentAuthetnication 발생
  * 예외 응답을 세분화 하기 위해, jwt 관련 에러 발생 시에는 entrypoint에서 commence를 직접 호출*/
  private final AuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    log.info("JwtAuthenticationFilter activated");

    //access token이 필요 없는 경우(refresh token으로 처리되거나 token을 발급 받기 위한 요청)
    if (request.getRequestURI().equals("/auth/refresh") ||request.getRequestURI().equals("/auth/login")) {
      log.info("JwtAuthenticationFilter passed, {} ", request.getRequestURI());
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String token = getTokenFromRequest(request);
      if(token==null){
        log.info("token is null, call filterChain.doFilter()");
        filterChain.doFilter(request, response);
        return;
      }

      JwtAuthenticationToken unAuthenticatedToken = new JwtAuthenticationToken(token);

      //principal -> memberInfoDto
      JwtAuthenticationToken authenticatedToken = jwtAuthenticationProvider.authenticate(unAuthenticatedToken);

      SecurityContextHolder.getContext().setAuthentication(authenticatedToken);

      filterChain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
      callAuthenticationEntryPoint(request, response, JwtExceptionType.EXPIRED_ACCESS_TOKEN);

    } catch (NullPointerException  | IllegalArgumentException e) {
      callAuthenticationEntryPoint(request, response, JwtExceptionType.TOKEN_NOT_FOUND);

    } catch (MalformedJwtException e) {
      callAuthenticationEntryPoint(request, response, JwtExceptionType.INVALID_TOKEN);

    } catch (Exception e){
      log.error("error occurred in jwtAuthenticationFilter");
      log.error("exception message : {}", e.getMessage());
      e.printStackTrace();

      request.setAttribute("exception", JwtExceptionType.UNKNOWN_ERROR.getCode());
      throw new JwtAuthenticationException("throw malformed token exception", JwtExceptionType.UNKNOWN_ERROR);
    }

  }
  private String getTokenFromRequest(HttpServletRequest request) {
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return null;
    }
    return authorizationHeader.split(" ")[1];
  }
  private void callAuthenticationEntryPoint(HttpServletRequest request, HttpServletResponse response, JwtExceptionType jwtExceptionType) throws IOException, ServletException {
    log.error("exception {} thrown. {}",jwtExceptionType.getCode(), jwtExceptionType.getMessage());
    JwtAuthenticationException exception = new JwtAuthenticationException(jwtExceptionType);
    authenticationEntryPoint.commence(request, response,exception);
  }
}
