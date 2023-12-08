package com.study.springsecurityboard.auth.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class LoginResponseDto {
  String accessToken;
  String refreshToken;
  String memberId;
  String name;
}
