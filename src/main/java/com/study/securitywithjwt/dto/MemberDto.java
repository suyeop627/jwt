package com.study.securitywithjwt.dto;

import com.study.securitywithjwt.utils.member.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
//member dto for response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
  private Long memberId;
  private String email;
  private String name;
  private Set<String> roles = new HashSet<>();
  private Gender gender;
}
