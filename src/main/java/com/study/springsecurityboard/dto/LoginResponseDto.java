package com.study.springsecurityboard.dto;

import com.study.springsecurityboard.domain.Role;
import com.study.springsecurityboard.utils.LoginStatus;
import lombok.*;

import java.util.Set;

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
  Set<Role> roles;
  LoginStatus status;
}
