package com.study.securitywithjwt.testUtils.customMockUser;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomSecurityContextFactory.class)
public @interface WithMockCustomUser {
  String username() default "test@test.com";

  String name() default "test";

  String[] roles() default {"USER"};

  String memberId() default "3";


}
