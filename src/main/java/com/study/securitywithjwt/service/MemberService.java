package com.study.securitywithjwt.service;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.exception.ResourceDuplicatedException;
import com.study.securitywithjwt.exception.ResourceNotFoundException;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RefreshTokenRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
  private final RefreshTokenRepository refreshTokenRepository;

//회원 정보를 db에 저장함.
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
//MemberSignupRequestDto 를 Member 로 변환하여 반환함
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

  //MemberSignupRequestDto에 Role이 없을경우, ROLE_USER를 추가하고, Role이 포함된 경우, Set<Role>의 형식으로 변환하여 Member에 추가.
  private void addRoleToNewMember(MemberSignupRequestDto requestDto, Member newMember) {
    //일반회원 권한 추가
    if(requestDto.getRoles().size()==0){
      newMember.addRole(getRoleUserOrThrow());
    }else{
      Set<Role> roles = getRequestedRolesOrThrow(requestDto.getRoles());
      newMember.setRoles(roles);
    }
  }
  //UserRole에 해당하는 Role을 Db에서 조회하여 반환
  private Role getRoleUserOrThrow() {
    return roleRepository.findByName(UserRole.ROLE_USER)
        .orElseThrow(() -> new ResourceNotFoundException("role user is not exists"));
  }
  //UserRole이 다수의 값을 가진 경우, DB에 존재하는지 확인 후, Set<Role>로 변환하여 반환.
  private Set<Role> getRequestedRolesOrThrow(Set<UserRole> userRoles) {
    List<Role> allRoles = roleRepository.findAll();

    Set<UserRole> allUserRole =
        allRoles.stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

    Set<UserRole> nonexistentRoles =
        userRoles.stream()
            .filter(userRole -> !allUserRole.contains(userRole))
            .collect(Collectors.toSet());

    //존재하지 않는 role이 있다면, 예외 발생
    if(nonexistentRoles.size()!=0){
      throw new ResourceNotFoundException("Request contains nonexistent role. %s".formatted(nonexistentRoles));
    }
    //모두 존재한다면 request의 UserRole과 일치하는 Set<Role> 반환
    return allRoles.stream()
        .filter(role->userRoles.contains(role.getName()))
        .collect(Collectors.toSet());
  }



  //가입 및 조회에서 입력받은 필드 중, 중복을 허용하지 않는 필드의 중복여부 판단
  private void checkDuplicationOrThrow(SimpleMemberInfoDto requestDto, String fieldName) {

    if ((fieldName.equals(PHONE_FIELD_NAME) && memberRepository.existsByPhone(requestDto.getPhone()))||
        (fieldName.equals(EMAIL_FIELD_NAME) && memberRepository.existsByEmail(requestDto.getEmail()))) {
      log.error("filed name({}) of request({}) is duplicated",fieldName, requestDto);
      throw new ResourceDuplicatedException(String.format("%s entered already exists", fieldName));
    }
  }

  //회원 정보 수정
  public Member updateMember(Long memberId, MemberUpdateRequestDto updateRequestDto) {

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
    if(!updateRequestDto.getRoles().isEmpty()){
      Set<Role> rolesToUpdate = getRequestedRolesOrThrow(updateRequestDto.getRoles());
      member.setRoles(rolesToUpdate);
    }

    member.setEmail(updateRequestDto.getEmail());
    member.setPhone(updateRequestDto.getPhone());
    member.setName(updateRequestDto.getName());

    return memberRepository.save(member);
  }

  //memberId에 해당하는 회원 조회
  private Member findMemberByIdOrThrow(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format("member id %s is not found", memberId)));
  }

  //memberId에 해당하는 Member를 MemberDto로 변환하여 반환
  public MemberDto getMember(Long memberId) {
    Member member = findMemberByIdOrThrow(memberId);
    return memberDtoMapper.apply(member);
  }

  //조회된 Page<Member>를 Page<MemberDto>로 변환하여 반환
  public Page<MemberDto> getAllMembers(Integer browserPageNumber, Integer size) {
    int pageNumber = browserPageNumber - 1; //Page는 pageNumber 0부터 시작이므로, 전달받은 page에서 -1

    PageRequest pageRequest = PageRequest.of(pageNumber, size, Sort.by(Sort.Order.desc(DEFAULT_SORT_FIELD)));
    Page<Member> memberPages = memberRepository.findAll(pageRequest);
    return memberPages.map(memberDtoMapper);
  }
  //memberId에 해당하는 회원 id 삭제
  @Transactional
  public void deleteMember(Long memberId) {
    invalidateToken(memberId);
    memberRepository.findById(memberId)
        .orElseThrow(()->
            new ResourceNotFoundException(String.format("member id : %s, is nonexistent", memberId)));
    memberRepository.deleteById(memberId);
  }
  private void invalidateToken(Long memberId) {
    refreshTokenRepository.deleteByMemberId(memberId);
  }
}

