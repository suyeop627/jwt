package com.study.securitywithjwt.utils.annotation;

import com.study.securitywithjwt.dto.MemberInfo;
import com.study.securitywithjwt.dto.MemberInfoDto;
import com.study.securitywithjwt.jwt.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
public class LoggedInUserInfoArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(LoggedInUserInfo.class) && parameter.getParameterType() == MemberInfo.class;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter,
                                ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest,
                                WebDataBinderFactory binderFactory) throws Exception {

    Authentication authentication = null;
    try {
      authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication == null) {
        throw new BadCredentialsException("LoggedInUserArgumentResolver -> authentication not found");
      }
      if (authentication instanceof AnonymousAuthenticationToken) {
        return null;
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new BadCredentialsException("exception occurred in LoggedInUserInfoArgumentResolver");
    }


    JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
    Object details = jwtAuthenticationToken.getDetails();


    Object principal = jwtAuthenticationToken.getPrincipal();
    if (principal == null) {
      return null;
    }

    MemberInfoDto memberInfoDto = (MemberInfoDto) principal;
    MemberInfo memberInfo = MemberInfo.builder()
        .memberId(memberInfoDto.getMemberId())
        .email(memberInfoDto.getEmail())
        .name(memberInfoDto.getName())
        .build();

    Collection<GrantedAuthority> authorities = jwtAuthenticationToken.getAuthorities();
    Set<String> roles = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());

    memberInfo.setRoles(roles);


    return memberInfo;
  }
}