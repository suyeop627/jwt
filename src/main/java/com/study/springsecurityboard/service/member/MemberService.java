package com.study.springsecurityboard.service.member;

import com.study.springsecurityboard.dto.MemberInfoDto;
import com.study.springsecurityboard.dto.MemberSignupRequestDto;
import com.study.springsecurityboard.dto.MemberSignupResponseDto;
import org.springframework.http.ResponseEntity;

public interface MemberService {
  MemberSignupResponseDto addMember(MemberSignupRequestDto requestDto);

  MemberInfoDto getMember(Integer memberId);
}
