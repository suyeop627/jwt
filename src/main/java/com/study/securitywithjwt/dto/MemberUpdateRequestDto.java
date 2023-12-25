package com.study.securitywithjwt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

//회원 정보 수정시, 회원 정보 전달 dto
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//equals및 hashCode 호출 시에 부모클래스의 필드와 자손클래스에서 추가된 필드를 모두 비교.  callSuper = false 로 하면 부모클래스의 필드는 무시한 채, 자손클래스의 필드만 비교
public class MemberUpdateRequestDto extends SimpleMemberInfoDto {
  //SimpleMemberInfoDto contains email, name, phone field
  @NotNull(message = "memberId must not be null")
  private Long memberId;

}
