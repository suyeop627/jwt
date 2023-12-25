package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.service.MemberService;
import com.study.securitywithjwt.utils.ControllerUtils;
import com.study.securitywithjwt.utils.annotation.TokenToMemberInfo;
import com.study.securitywithjwt.utils.member.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;
//회원 CRUD 처리 클래스
@RestController
@RequiredArgsConstructor
@RequestMapping("members")
@Slf4j
public class MemberController {
  private final MemberService memberService;
  //회원 가입
  //회원 정보 입력값 유효성 검사 후, 이상 있을 시 ErrorDto 반환
  //가입 성공시, 201 및 생성된 URI 반환
  @PostMapping
  public ResponseEntity<?> signup(@Valid @RequestBody MemberSignupRequestDto signupRequestDto, BindingResult bindingResult, HttpServletRequest request) {
    log.info("Attempting signup for user with email: {}, name: {}", signupRequestDto.getEmail(), signupRequestDto.getName());

    ResponseEntity<Set<ErrorDto>> errorDtoSet = ControllerUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    MemberSignupResponseDto memberSignupResponseDto = memberService.addMember(signupRequestDto);
    URI uri = ControllerUtils.getCreatedUri(memberSignupResponseDto.getMemberId());

    log.info("Created user uri: {}", uri);
    return ResponseEntity.created(uri).body(memberSignupResponseDto);
  }

  //회원 목록 조회
  //Page<MemberDto> 를 반환하며, 기본 10개의 MemberDto를 하나의 페이지로 반환
  @GetMapping
  public ResponseEntity<Page<MemberDto>> getAllMembers(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                                       @RequestParam(name = "size", defaultValue = "10") Integer size) {
    log.info("Method: getAllMembers called with page: {}, size: {}", page, size);
    Page<MemberDto> memberDtoPage = memberService.getAllMembers(page, size);

    log.info("Retrieved {} members in page {}. Total pages: {}, Total members: {}.",
        memberDtoPage.getNumberOfElements(), page, memberDtoPage.getTotalPages(), memberDtoPage.getTotalElements());
    return ResponseEntity.ok().body(memberDtoPage);
  }

  //단일 회원 정보 조회
  @GetMapping("/{memberId}")
  public ResponseEntity<MemberDto> getMember(@PathVariable("memberId") Long memberId) {
    log.info("Method: getMember called with memberId {}", memberId);
    return ResponseEntity.ok().body(memberService.getMember(memberId));
  }

  //회원 정보 수정
  //ROLE_ADMIN : 모든 회원의 정보 수정 가능
  //ROLE_USER : 전달받은 memberId와 access token에 담긴 memberId가 일치하는 경우에만 수정 가능
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @PutMapping("/{memberId}")
  public ResponseEntity<?> updateMember(@Valid @RequestBody MemberUpdateRequestDto updateRequestDto, //최상단으로
                                        BindingResult bindingResult,
                                        @PathVariable("memberId") Long memberId,
                                        @TokenToMemberInfo LoginMemberInfo loginMember,
                                        HttpServletRequest request) {

    log.info("Attempting to update target member(memberId: {}) by member(memberId: {}, roles: {})",
        memberId, loginMember.getMemberId(), loginMember.getRoles());

    ResponseEntity<Set<ErrorDto>> errorDtoSet = ControllerUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    isAdminOrMemberOwnOrThrow(memberId, loginMember);
    log.info("Update request successful. Target member(memberId: {}) info updated by member(memberId: {}, roles: {})",
        memberId, loginMember.getMemberId(), loginMember.getRoles());
    return ResponseEntity.ok().body(memberService.updateMember(memberId, updateRequestDto));
  }

  //ROLE_ADMIN이거나, 로그인한 회원의 Id와 수정할 회원의 Id가 일치하는지 판단
  //조건을 충족하지 못할 경우, AccessDeniedException을 던짐
  private void isAdminOrMemberOwnOrThrow(Long uriMemberId, LoginMemberInfo loginMember) {
    if (!loginMember.getRoles().contains(UserRole.ROLE_ADMIN.name())
        && !uriMemberId.equals(loginMember.getMemberId())) {
      throw new AccessDeniedException(
          String.format("Member (memberId: %s, roles: %s) does not have permission to change data of the target member(memberId: %s).",
              loginMember.getMemberId(),loginMember.getRoles(), uriMemberId)
      );
    }
  }

  //회원 삭제
  //ROLE_ADMIN : 모든 회원의 정보 삭제 가능
  //ROLE_USER : 전달받은 memberId와 access token에 담긴 memberId가 일치하는 경우에만 삭제 가능
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @DeleteMapping("/{memberId}")
  public ResponseEntity<?> deleteMember(@PathVariable("memberId") Long memberId,
                                        @TokenToMemberInfo LoginMemberInfo loginMember) {
    log.info("member(memberId: {}) deletion request from memberId: {}, role: {}", memberId,
        loginMember.getMemberId(), loginMember.getRoles());

    isAdminOrMemberOwnOrThrow(memberId, loginMember);

    memberService.deleteMember(memberId);
    log.info("Deletion request successful. Target member(memberId: {}) info deleted by member(memberId: {}, roles: {})",
        memberId, loginMember.getMemberId(), loginMember.getRoles());
    return ResponseEntity.ok().build();
  }
}
