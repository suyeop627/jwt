package com.study.securitywithjwt.dto;

import lombok.*;

import java.time.LocalDateTime;
//회원 가입 성공시, 응답 정보 전달 dto
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSignupResponseDto {
  private Long memberId;
  private String email;
  private String name;
  private LocalDateTime regdate;
}
