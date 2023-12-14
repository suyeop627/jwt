package com.study.securitywithjwt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDto {
  @Email
  private String email;
  @Size(min=8, max = 16, message = "password must be 8~16 characters")
  private String password;
}
