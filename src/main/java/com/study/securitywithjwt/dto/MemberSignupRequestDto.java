package com.study.securitywithjwt.dto;

import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

//회원 가입 정보 전달 dto
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberSignupRequestDto extends SimpleMemberInfoDto {
  //SimpleMemberInfoDto contains email, name, phone, password field

  @NotNull(message = "gender must not be null")
  private Gender gender;

  private Set<UserRole> userRoles;
}
