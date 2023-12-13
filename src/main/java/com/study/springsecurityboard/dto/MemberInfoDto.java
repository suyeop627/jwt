package com.study.springsecurityboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MemberInfoDto {
  private Long memberId;
  private String email;
  private String name;
  private String gender;

}
