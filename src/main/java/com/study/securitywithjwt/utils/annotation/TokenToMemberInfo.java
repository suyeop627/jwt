package com.study.securitywithjwt.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//authentication에 저장된 회원 정보를 LoginMemberInfo로 반환함.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface TokenToMemberInfo {
}
