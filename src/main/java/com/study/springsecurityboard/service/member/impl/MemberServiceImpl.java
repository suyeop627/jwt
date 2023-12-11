package com.study.springsecurityboard.service.member.impl;

import com.study.springsecurityboard.domain.Member;
import com.study.springsecurityboard.domain.Role;
import com.study.springsecurityboard.dto.MemberSignupRequestDto;
import com.study.springsecurityboard.dto.MemberSignupResponseDto;
import com.study.springsecurityboard.repository.MemberRepository;
import com.study.springsecurityboard.repository.RoleRepository;
import com.study.springsecurityboard.service.member.MemberService;
import com.study.springsecurityboard.utils.member.UserRole;
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
  public MemberSignupResponseDto insertMember(MemberSignupRequestDto requestDto) {
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

