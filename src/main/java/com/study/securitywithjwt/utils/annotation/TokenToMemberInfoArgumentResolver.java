package com.study.securitywithjwt.utils.annotation;

import com.study.securitywithjwt.dto.LoginMemberInfo;
import com.study.securitywithjwt.jwt.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenToMemberInfoArgumentResolver implements HandlerMethodArgumentResolver {

  //argumentResolver 적용할 파라미터 판단.
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(TokenToMemberInfo.class) && parameter.getParameterType() == LoginMemberInfo.class;
  }
  //SecurityContext의 Authentication의 principal을 가져와서, LoginMemberInfo 의 형태로 반환
  @Override
  public Object resolveArgument(MethodParameter parameter,
                                ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest,
                                WebDataBinderFactory binderFactory) throws Exception {

    Authentication authentication = null;
    try {
      authentication = SecurityContextHolder.getContext().getAuthentication();

//      permitAll()인 uri로 접근할 경우, authentication은 AnonymousAuthenticationToken로 저장되어 따로 분류함.
      if (authentication == null /*|| authentication instanceof AnonymousAuthenticationToken*/) {
        return null;
      }

    } catch (Exception e) {
      log.error("Exception occurred in LoggedInUserInfoArgumentResolver.", e);
      throw new BadCredentialsException("exception occurred in LoggedInUserInfoArgumentResolver", e);
    }

    JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;

    Object principal = jwtAuthenticationToken.getPrincipal();
    if (principal == null) {
      return null;
    }

    LoginMemberInfo loginMemberInfo = (LoginMemberInfo) principal;

    Collection<GrantedAuthority> authorities = jwtAuthenticationToken.getAuthorities();
    Set<String> roles = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());

    loginMemberInfo.setRoles(roles);

    log.info("LoginMemberInfo generated from access token. Logged-in member: {}", loginMemberInfo);
    return loginMemberInfo;
  }
}
