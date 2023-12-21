package com.study.securitywithjwt.jwt;

import com.study.securitywithjwt.exception.CustomAuthenticationEntryPoint;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
  @Mock
  private JwtAuthenticationProvider jwtAuthenticationProvider;
  @Mock
  private CustomAuthenticationEntryPoint authenticationEntryPoint;
  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  HttpServletRequest request;
  HttpServletResponse response;
  FilterChain filterChain;

  @BeforeEach
  void setUp(){
    //MockHttpServletRequest request = new MockHttpServletRequest(); - getParameter(), setAttribute(), getRequestURI() 등의 메서드는 기본적으로 동작이 정의돼있음.
     request = mock(HttpServletRequest.class);//모든 메서드 호출에 대해 기본적으로 null값을 반환함. when()등으로 정의해서 사용.
     response = mock(HttpServletResponse.class);
     filterChain = mock(FilterChain.class);
  }


  @Test
  void doFilterInternal_requestForRefreshToken_passFilter() throws ServletException, IOException {
    // Given
    given(request.getRequestURI()).willReturn("/auth/refresh");

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    then(filterChain).should(times(1)).doFilter(request, response);
    then(jwtAuthenticationProvider).shouldHaveNoInteractions();
  }

  @Test
  void doFilterInternal_requestForLogin_passFilter() throws ServletException, IOException {
    // Given
    given(request.getRequestURI()).willReturn("/auth/login");

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    then(filterChain).should(times(1)).doFilter(request, response);
    then(jwtAuthenticationProvider).shouldHaveNoInteractions();
  }

  @Test
  void doFilterInternal_noTokenInHeader_passFilter() throws ServletException, IOException {
    // Given
    given(request.getRequestURI()).willReturn("/api");

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    then(jwtAuthenticationProvider).shouldHaveNoInteractions();
    then(filterChain).should(times(1)).doFilter(request,response);

  }

  @Test
  void doFilterInternal_expiredToken_callAuthenticationEntryPoint() throws ServletException, IOException {
    // Given
    given(request.getRequestURI()).willReturn("/api");
    given(request.getHeader("Authorization")).willReturn("Bearer expired-token");
    given(jwtAuthenticationProvider.authenticate(any(JwtAuthenticationToken.class)))
        .willThrow(new ExpiredJwtException(null, null,"throw expired jwt exception"));

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
    // /Then
    then(authenticationEntryPoint).should(times(1)).commence(any(), any(), any());
    then(filterChain).shouldHaveNoInteractions();
  }
}
