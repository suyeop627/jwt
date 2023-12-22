package com.study.securitywithjwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

//principal of Authentication
//member info in token
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
