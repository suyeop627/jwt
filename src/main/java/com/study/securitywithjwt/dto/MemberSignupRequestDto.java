package com.study.securitywithjwt.dto;

import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

//회원 가입 정보 전달 dto
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberSignupRequestDto extends SimpleMemberInfoDto {
  //SimpleMemberInfoDto contains email, name, phone field
  @Size(min=8, max = 16, message = "password size must be between 8 and 16")
  @NotNull(message = "password must not be null")
  private String password;

  @NotNull(message = "gender must not be null")
  private Gender gender;

  private Set<UserRole> userRoles;
}
