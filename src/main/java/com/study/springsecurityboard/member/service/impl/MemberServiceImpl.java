package com.study.springsecurityboard.member.service.impl;

import com.study.springsecurityboard.member.domain.Member;
import com.study.springsecurityboard.member.domain.Role;
import com.study.springsecurityboard.member.dto.MemberSignupRequestDto;
import com.study.springsecurityboard.member.dto.MemberSignupResponseDto;
import com.study.springsecurityboard.member.repository.MemberRepository;
import com.study.springsecurityboard.member.repository.RoleRepository;
import com.study.springsecurityboard.member.service.MemberService;
import com.study.springsecurityboard.member.utils.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;
  private final RoleRepository roleRepository;

  @Override
  public MemberSignupResponseDto addMember(MemberSignupRequestDto requestDto) {
    Member newMember = Member.builder()
        .email(requestDto.getEmail())
        .name(requestDto.getName())
        .password(passwordEncoder.encode(requestDto.getPassword()))
        .gender(requestDto.getGender())
        .build();

    Optional<Role> roleUser = roleRepository.findByName(UserRole.ROLE_USER);

    newMember.addRole(roleUser.get());

    Member savedMember = memberRepository.save(newMember);

    return MemberSignupResponseDto.builder()
        .memberId(savedMember.getMemberId())
        .email(savedMember.getEmail())
        .regdate(savedMember.getRegdate())
        .name(savedMember.getName())
        .build();
  }
}

