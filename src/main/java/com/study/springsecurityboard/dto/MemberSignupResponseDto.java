package com.study.springsecurityboard.dto;

import lombok.*;

import java.time.LocalDateTime;
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
