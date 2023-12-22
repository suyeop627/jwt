package com.study.securitywithjwt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
//회원 로그인 정보 전달 dto
@Data
@AllArgsConstructor
public class LoginRequestDto {
  @Email
  @NotNull
  private String email;
  @Size(min=8, max = 16, message = "password size must be between 8 and 16")
  @NotNull
  private String password;
}
