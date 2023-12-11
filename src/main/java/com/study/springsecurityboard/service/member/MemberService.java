package com.study.springsecurityboard.service.member;

import com.study.springsecurityboard.dto.MemberSignupRequestDto;
import com.study.springsecurityboard.dto.MemberSignupResponseDto;

public interface MemberService {
  MemberSignupResponseDto insertMember(MemberSignupRequestDto requestDto);
}
