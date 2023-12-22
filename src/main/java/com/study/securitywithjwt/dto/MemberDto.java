package com.study.securitywithjwt.dto;

import com.study.securitywithjwt.utils.member.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
//회원 정보 조회시, 회원 정보 전달 dto
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
  private Long memberId;
  private String email;
  private String name;
  private String phone;
  @Builder.Default
  private Set<String> roles = new HashSet<>();
  private Gender gender;
}
