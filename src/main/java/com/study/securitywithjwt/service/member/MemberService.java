package com.study.securitywithjwt.service.member;

import com.study.securitywithjwt.dto.MemberDto;
import com.study.securitywithjwt.dto.MemberSignupRequestDto;
import com.study.securitywithjwt.dto.MemberSignupResponseDto;

public interface MemberService {
  MemberSignupResponseDto addMember(MemberSignupRequestDto requestDto);

  MemberDto getMember(Integer memberId);
}
