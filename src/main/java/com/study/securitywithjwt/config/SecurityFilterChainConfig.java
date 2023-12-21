package com.study.securitywithjwt.config;

import com.study.securitywithjwt.exception.CustomAuthenticationEntryPoint;
import com.study.securitywithjwt.jwt.JwtAuthenticationFilter;
import com.study.securitywithjwt.jwt.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

/**
 * Security filter chain 설정 클래스
 */
@Configuration
@EnableWebSecurity
public class SecurityFilterChainConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationProvider jwtAuthenticationProvider;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  public SecurityFilterChainConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                                   JwtAuthenticationProvider jwtAuthenticationProvider,
                                   CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {

    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
  }

  /**
   *SecurityFilterChain의 보안 구성을 정의함.<br>
   * csrf 비활성화 - jwt 사용시, stateless한 세션을 사용하므로, csrf의 위험성이 낮음<br>
   * cors 기본값 사용<br>
   * formLogin, HttpBasic 인증 비활성화<br>
   * Http 요청 권한 설정<br>
   * 세션 설정(STATELESS)<br>
   * JWT 인증 filter 및 provider 추가<br>
   * 인증 예외처리 설정<br>
   * @param http http 보안 구성을 정의함
   * @return SecurityFilterChain 보안 구성을 정의한 SecurityFIlterChain 반환
   *
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(httpRequest ->
            httpRequest
                .requestMatchers(HttpMethod.POST,"/error").permitAll()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .requestMatchers(HttpMethod.POST, "/members", "/auth/login", "/auth/refresh")
                .permitAll()
                .requestMatchers(HttpMethod.GET,"/api")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/admin")
                .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/admin")
                .hasRole("ADMIN")
                .anyRequest()
                .authenticated()

        ).sessionManagement(securitySessionManagementConfigurer -> securitySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authenticationProvider(jwtAuthenticationProvider)
        .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(customAuthenticationEntryPoint));

    return http.build();
  }


}
