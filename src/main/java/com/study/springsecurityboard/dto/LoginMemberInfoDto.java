package com.study.springsecurityboard.dto;

import lombok.Data;

@Data
public class LoginMemberInfoDto {
  private Long memberId;
  private String email;
  private String name;

}
