package com.study.springsecurityboard.service.member.impl;

import com.study.springsecurityboard.domain.Member;
import com.study.springsecurityboard.domain.Role;
import com.study.springsecurityboard.dto.MemberInfoDto;
import com.study.springsecurityboard.dto.MemberSignupRequestDto;
import com.study.springsecurityboard.dto.MemberSignupResponseDto;
import com.study.springsecurityboard.exception.ResourceDuplicatedException;
import com.study.springsecurityboard.exception.ResourceNotFoundException;
import com.study.springsecurityboard.repository.MemberRepository;
import com.study.springsecurityboard.repository.RoleRepository;
import com.study.springsecurityboard.service.member.MemberService;
import com.study.springsecurityboard.utils.member.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    String userEmail = requestDto.getEmail();
    //email 중복 확인
    if (memberRepository.existsMemberByEmail(userEmail)) {
      throw new ResourceDuplicatedException("email entered exist already");
    }

    Member newMember = Member.builder()
        .email(requestDto.getEmail())
        .name(requestDto.getName())
        .password(passwordEncoder.encode(requestDto.getPassword()))
        .gender(requestDto.getGender())
        .build();

    //일반회원 권한 추가
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

  @Override
  public MemberInfoDto getMember(Integer memberId) {
    Optional<Member> optionalMember = memberRepository.findById(Long.valueOf(memberId));

    if (optionalMember.isPresent()) {
      Member member = optionalMember.get();

      return MemberInfoDto.builder()
          .memberId(member.getMemberId())
          .email(member.getEmail())
          .gender(member.getGender().name())
          .name(member.getName())
          .build();
    }else{
      throw new ResourceNotFoundException(String.format("member id %s is not found", memberId));
    }
  }
}

