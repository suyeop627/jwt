package com.study.securitywithjwt.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class MemberInfo {
  private Long memberId;
  private String email;
  private String name;
  private Set<String> roles = new HashSet<>();

}
