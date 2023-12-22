package com.study.securitywithjwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
//access(refresh) token에 저장된 회원 정보
//@LoggedInUserInfo 어노테이션과 함께 사용될 경우, 토큰에 저장된 회원 정보를 전달함.
//토큰 로그인 시, Authentication의 principal로 사용됨.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoInToken {
  private Long memberId;
  private String email;
  private String name;
  private Set<String> roles;
}
