package com.study.securitywithjwt.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter//getter 없으면 HttpMediaTypeNotAcceptableException 발생
public class LoginResponseDto {
  String accessToken;
  String refreshToken;
  String email;
  String name;
}
