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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    Member savedMember = memberRepository.save(newMember);

    return MemberSignupResponseDto.builder()
        .memberId(savedMember.getMemberId())
        .email(savedMember.getEmail())
        .regdate(savedMember.getRegdate())
        .name(savedMember.getName())
        .build();
  }



  private Member createMemberFromSignupRequest(MemberSignupRequestDto requestDto) {
    Member member = Member.builder()
        .email(requestDto.getEmail())
        .name(requestDto.getName())
        .phone(requestDto.getPhone())
        .password(passwordEncoder.encode(requestDto.getPassword()))
        .gender(requestDto.getGender())
        .build();

    addRoleToNewMember(requestDto, member); //call by reference

    return member;
  }

  private void addRoleToNewMember(MemberSignupRequestDto requestDto, Member newMember) {
    //일반회원 권한 추가
    if(requestDto.getUserRoles()==null){
      newMember.addRole(getRoleOrThrow(UserRole.ROLE_USER));
    }else{
      Set<Role> roles = requestDto.getUserRoles().stream().map(this::getRoleOrThrow).collect(Collectors.toSet());
      newMember.setRoles(roles);
    }
  }
  private Role getRoleOrThrow(UserRole userRole) {
    log.error("ROLE_USER is not found");
    return roleRepository.findByName(userRole)
        .orElseThrow(() -> new ResourceNotFoundException("role user is not exists"));
  }

  private void checkDuplicationOrThrow(SimpleMemberInfoDto requestDto, String fieldName) {

    if ((fieldName.equals(PHONE_FIELD_NAME) && memberRepository.existsByPhone(requestDto.getPhone()))||
        (fieldName.equals(EMAIL_FIELD_NAME) && memberRepository.existsByEmail(requestDto.getEmail()))) {
      log.error("filed name({}) of request({}) is duplicated",fieldName, requestDto);
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
    if(!passwordEncoder.matches(updateRequestDto.getPassword(), member.getPassword())){
      member.setPassword(passwordEncoder.encode(updateRequestDto.getPassword()));
    }

    member.setEmail(updateRequestDto.getEmail());
    member.setPhone(updateRequestDto.getPhone());
    member.setName(updateRequestDto.getName());
    Member updatedMember = memberRepository.save(member);

    return memberDtoMapper.apply(updatedMember);
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
    memberRepository.findById(memberId)
        .orElseThrow(()->
            new ResourceNotFoundException(String.format("member id : %s, is nonexistent", memberId)));
    memberRepository.deleteById(memberId);
  }
}

