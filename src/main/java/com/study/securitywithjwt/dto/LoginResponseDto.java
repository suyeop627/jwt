package com.study.securitywithjwt.dto;

import lombok.*;
//회원 로그인 성공시 응답 dto
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
