package com.study.securitywithjwt.dto;

import lombok.*;

import java.util.Set;
//access(refresh) token 에 저장된 회원 정보
//@LoggedInUserInfo 어노테이션과 함께 사용될 경우, 토큰에 저장된 회원 정보를 저장함
//토큰 로그인 시, Authentication의 principal로 사용됨.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginMemberInfo {
  @NonNull
  private Long memberId;
  @NonNull
  private String email;
  @NonNull
  private String name;
  @NonNull
  private Set<String> roles;
}
