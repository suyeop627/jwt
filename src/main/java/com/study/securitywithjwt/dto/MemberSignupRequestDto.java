package com.study.securitywithjwt.dto;

import com.study.securitywithjwt.utils.member.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberSignupRequestDto {
  @Email(message = "email format is wrong")
  private String email;

  @Size(min=2, max=16, message = "name must be 2~ 16 characters")
  private String name;

  @Size(min=8, max = 16, message = "password must be 8~16 characters")
  private String password;

  private Gender gender;
}
