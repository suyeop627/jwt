package com.study.securitywithjwt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequestDto {
  @Email
  private String email;
  @Size(min=8, max = 16, message = "password size must be between 8 and 16")
  private String password;
}
