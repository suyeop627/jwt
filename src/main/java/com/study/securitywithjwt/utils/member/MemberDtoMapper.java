package com.study.securitywithjwt.utils.member;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.dto.MemberDto;
import org.springframework.stereotype.Component;

import java.util.function.Function;
@Component
public class MemberDtoMapper implements Function<Member, MemberDto> {

  @Override
  public MemberDto apply(Member member) {
    return MemberDto.builder()
        .memberId(member.getMemberId())
        .roles(member.getRoleNameSet())
        .email(member.getEmail())
        .name(member.getName())
        .gender(member.getGender())
        .phone(member.getPhone())
        .build();
  }
}
