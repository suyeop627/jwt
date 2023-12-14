package com.study.securitywithjwt.service.member.impl;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.MemberInfoDto;
import com.study.securitywithjwt.dto.MemberSignupRequestDto;
import com.study.securitywithjwt.dto.MemberSignupResponseDto;
import com.study.securitywithjwt.exception.ResourceDuplicatedException;
import com.study.securitywithjwt.exception.ResourceNotFoundException;
import com.study.securitywithjwt.dao.MemberRepository;
import com.study.securitywithjwt.dao.RoleRepository;
import com.study.securitywithjwt.service.member.MemberService;
import com.study.securitywithjwt.utils.member.UserRole;
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
          .name(member.getName())
          .build();
    }else{
      throw new ResourceNotFoundException(String.format("member id %s is not found", memberId));
    }
  }
}

