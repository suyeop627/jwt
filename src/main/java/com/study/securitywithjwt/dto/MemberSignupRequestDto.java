package com.study.securitywithjwt.dto;

import com.study.securitywithjwt.utils.member.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberSignupRequestDto {
  @Email
  private String email;

  @Size(min=2, max=16, message = "name size must be between 2 and 16")
  private String name;

  @Size(min=8, max = 16, message = "password size must be between 8 and 16")
  private String password;

  private Gender gender;
}
