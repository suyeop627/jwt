package com.study.springsecurityboard.dto;

import com.study.springsecurityboard.utils.member.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberSignupRequestDto {
  @Email(message = "email 형식에 맞지 않습니다.")
  private String email;

  @Size(min=2, max=16, message = "이름은 2자 이상, 16자 이하여야 합니다.")
  private String name;

  @Size(min=8, max = 16, message = "비밀번호는 8자 이상, 16자 이하여야 합니다.")
  private String password;

  private Gender gender;
}
