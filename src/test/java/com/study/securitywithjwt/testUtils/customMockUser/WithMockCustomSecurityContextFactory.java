package com.study.securitywithjwt.testUtils.customMockUser;

import com.study.securitywithjwt.dto.MemberInfoInToken;
import com.study.securitywithjwt.jwt.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WithMockCustomSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
  @Override
  public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

    MemberInfoInToken memberInfo = new MemberInfoInToken();
    memberInfo.setEmail(customUser.username());
    memberInfo.setName(customUser.name());


    Set<SimpleGrantedAuthority> authorities = Stream.of(customUser.roles())
        .map(role->"ROLE_" +role)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());

    String mockToken = "token_token_token_token";

    Authentication auth = new JwtAuthenticationToken(memberInfo, mockToken , authorities);
    securityContext.setAuthentication(auth);
    return securityContext;
  }
}
