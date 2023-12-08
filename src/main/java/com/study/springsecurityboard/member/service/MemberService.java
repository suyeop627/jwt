package com.study.springsecurityboard.member.service;

import com.study.springsecurityboard.member.domain.Member;
import com.study.springsecurityboard.member.dto.MemberSignupRequestDto;
import com.study.springsecurityboard.member.dto.MemberSignupResponseDto;

public interface MemberService {
  MemberSignupResponseDto addMember(MemberSignupRequestDto requestDto);
}
