package com.study.securitywithjwt.service.member;

import com.study.securitywithjwt.dto.MemberInfoDto;
import com.study.securitywithjwt.dto.MemberSignupRequestDto;
import com.study.securitywithjwt.dto.MemberSignupResponseDto;

public interface MemberService {
  MemberSignupResponseDto addMember(MemberSignupRequestDto requestDto);

  MemberInfoDto getMember(Integer memberId);
}
