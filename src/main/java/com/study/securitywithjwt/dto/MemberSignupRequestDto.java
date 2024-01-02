package com.study.securitywithjwt.dto;

import com.study.securitywithjwt.utils.member.Gender;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

//회원 가입 정보 전달 dto
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberSignupRequestDto extends SimpleMemberInfoDto {
  //SimpleMemberInfoDto
  // - String email
  // - String name
  // - String phone
  // - String password
  // - Set<UserRole> roles

  @NotNull(message = "gender must not be null")
  private Gender gender;
}
