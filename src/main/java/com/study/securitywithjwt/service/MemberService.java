package com.study.securitywithjwt.service;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.exception.ResourceDuplicatedException;
import com.study.securitywithjwt.exception.ResourceNotFoundException;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RoleRepository;
import com.study.securitywithjwt.utils.member.MemberDtoMapper;
import com.study.securitywithjwt.utils.member.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;
  private final RoleRepository roleRepository;
  private final MemberDtoMapper memberDtoMapper;
  private final String DEFAULT_SORT_FIELD = "memberId";
  private final String EMAIL_FIELD_NAME = "email";
  private final String PHONE_FIELD_NAME = "phone";


  public MemberSignupResponseDto addMember(MemberSignupRequestDto requestDto) {
    checkDuplicationOrThrow(requestDto, EMAIL_FIELD_NAME);
    checkDuplicationOrThrow(requestDto, PHONE_FIELD_NAME);

    Member newMember = createMemberFromSignupRequest(requestDto);

    //일반회원 권한 추가
    newMember.addRole(getRoleUserOrThrow());

    Member savedMember = memberRepository.save(newMember);

    return MemberSignupResponseDto.builder()
        .memberId(savedMember.getMemberId())
        .email(savedMember.getEmail())
        .regdate(savedMember.getRegdate())
        .name(savedMember.getName())
        .build();
  }

  private Member createMemberFromSignupRequest(MemberSignupRequestDto requestDto) {
    return Member.builder()
        .email(requestDto.getEmail())
        .name(requestDto.getName())
        .password(passwordEncoder.encode(requestDto.getPassword()))
        .gender(requestDto.getGender())
        .build();
  }

  private Role getRoleUserOrThrow() {
    return roleRepository.findByName(UserRole.ROLE_USER)
        .orElseThrow(() -> new ResourceNotFoundException("role is not exists"));
  }

  private void checkDuplicationOrThrow(SimpleMemberInfoDto requestDto, String fieldName) {

    if ((fieldName.equals(PHONE_FIELD_NAME) && memberRepository.existsByPhone(requestDto.getPhone()))||
        (fieldName.equals(EMAIL_FIELD_NAME) && memberRepository.existsByEmail(requestDto.getEmail()))) {
      throw new ResourceDuplicatedException(String.format("%s number entered already exists", fieldName));
    }
  }
  public MemberDto updateMember(Long memberId, MemberUpdateRequestDto updateRequestDto) {

    Member member = findMemberByIdOrThrow(memberId);

    if(!member.getEmail().equals(updateRequestDto.getEmail())){
      checkDuplicationOrThrow(updateRequestDto, EMAIL_FIELD_NAME);
    }
    if(!member.getPhone().equals(updateRequestDto.getPhone())){
      checkDuplicationOrThrow(updateRequestDto, PHONE_FIELD_NAME);
    }
    if(passwordEncoder.matches(updateRequestDto.getPassword(), member.getPassword())){
      member.setPassword(passwordEncoder.encode(updateRequestDto.getPassword()));
    }

    member.setEmail(updateRequestDto.getEmail());
    member.setPhone(updateRequestDto.getPhone());
    member.setGender(updateRequestDto.getGender());
    member.setName(updateRequestDto.getName());

    memberRepository.save(member);

    return memberDtoMapper.apply(member);
  }

  private Member findMemberByIdOrThrow(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format("member id %s is not found", memberId)));
  }

  public MemberDto getMember(Long memberId) {
    Member member = findMemberByIdOrThrow(memberId);
    return memberDtoMapper.apply(member);
  }

  public Page<MemberDto> getAllMembers(Integer browserPageNumber, Integer size) {
    int pageNumber = browserPageNumber - 1; //Page는 pageNumber 0부터 시작이므로, 전달받은 page에서 -1

    PageRequest pageRequest = PageRequest.of(pageNumber, size, Sort.by(Sort.Order.desc(DEFAULT_SORT_FIELD)));
    Page<Member> memberPages = memberRepository.findAll(pageRequest);
    return memberPages.map(memberDtoMapper);
  }

  public void deleteMember(Long memberId) {
    memberRepository.deleteById(memberId);
  }
}

