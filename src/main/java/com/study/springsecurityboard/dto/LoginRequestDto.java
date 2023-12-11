package com.study.springsecurityboard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDto {
  @Email
  private String email;
  @Size(min=8, max = 16, message = "비밀번호는 8자 이상, 16자 이하여야 합니다.")
  private String password;
}
