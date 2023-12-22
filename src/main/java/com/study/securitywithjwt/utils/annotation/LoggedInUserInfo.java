package com.study.securitywithjwt.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//토큰의 payload 를 기반으로 MemberInfoInToken 생성
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface LoggedInUserInfo {
}
