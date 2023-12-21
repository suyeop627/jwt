package com.study.securitywithjwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 스프링 시큐리티에서 사용될 Bean을 등록할 클래스
 */
@Configuration
public class SecurityConfig{

/**
* DaoAuthenticationProvider에서 사용할 PasswordEncoder를 Bean으로 등록
*/
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * AuthenticationProvider에 의존관계 주입
   * @param userDetailsService 사용자 로그인 시, DB에 저장된 정보를 조회함
   * @param passwordEncoder 암호화된 password 관련 처리 담당
   * @return DaoAuthenticationProvider
   */
  @Bean
  public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
    daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
    daoAuthenticationProvider.setUserDetailsService(userDetailsService);
    return daoAuthenticationProvider;
  }

  /**
   * AuthenticationManager Bean 등록
   * @param authenticationConfiguration authentication manager를 구성
   * @return spring security에서 제공하는 AuthenticationManager의 구현체인, ProviderManager 반환
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

}
