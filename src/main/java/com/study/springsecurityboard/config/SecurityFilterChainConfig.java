package com.study.springsecurityboard.config;

import com.study.springsecurityboard.exception.CustomAuthenticationEntryPoint;
import com.study.springsecurityboard.jwt.JwtAuthenticationFilter;
import com.study.springsecurityboard.jwt.JwtAuthenticationProvider;
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

@Configuration
@EnableWebSecurity
public class SecurityFilterChainConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationProvider jwtAuthenticationProvider;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  public SecurityFilterChainConfig(JwtAuthenticationFilter jwtAuthenticationFilter, JwtAuthenticationProvider jwtAuthenticationProvider, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(httpRequest ->
            httpRequest
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .requestMatchers(HttpMethod.POST, "/members", "/auth/login", "auth/refresh")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/board")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/**")
                .hasAnyRole("USER", "ADMIN")
                .requestMatchers("/admin")
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
