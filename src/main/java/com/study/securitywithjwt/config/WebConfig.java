package com.study.securitywithjwt.config;

import com.study.securitywithjwt.utils.annotation.LoggedInUserInfoArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

//Cors 설정 및 argumentResolver를 추가하는 설정 클래스
@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final LoggedInUserInfoArgumentResolver loggedInUserInfoArgumentResolver;

  public WebConfig(LoggedInUserInfoArgumentResolver loggedInUserInfoArgumentResolver) {
    this.loggedInUserInfoArgumentResolver = loggedInUserInfoArgumentResolver;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.addAllowedOrigin("http://localhost:3000");
    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "DELETE", "PATCH", "PUT", "OPTION"));
    corsConfiguration.addAllowedHeader("Origin");
    corsConfiguration.addAllowedHeader("Content-Type");
    corsConfiguration.addAllowedHeader("Accept");
    corsConfiguration.addAllowedHeader("Authorization");

    UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
    urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

    return urlBasedCorsConfigurationSource;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(loggedInUserInfoArgumentResolver);
  }
}
