package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.service.MemberService;
import com.study.securitywithjwt.utils.ControllerUtils;
import com.study.securitywithjwt.utils.annotation.LoggedInUserInfo;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("members")
@Slf4j
public class MemberController {

  private final MemberService memberService;


  //회원 가입
  @PostMapping
  public ResponseEntity<?> signup(@Valid @RequestBody MemberSignupRequestDto signupRequestDto, BindingResult bindingResult, HttpServletRequest request) {
    log.info("Attempting signup for user {}", signupRequestDto);

    ResponseEntity<Set<ErrorDto>> errorDtoSet = ControllerUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    MemberSignupResponseDto memberSignupResponseDto = memberService.addMember(signupRequestDto);
    URI uri = ControllerUtils.getCreatedUri(memberSignupResponseDto);

    log.info("Created user uri: {}", uri);
    return ResponseEntity.created(uri).body(memberSignupResponseDto);
  }

  //회원 목록 조회
  @GetMapping
  public ResponseEntity<Page<MemberDto>> getAllMembers(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                                       @RequestParam(name = "size", defaultValue = "10") Integer size) {
    Page<MemberDto> members = memberService.getAllMembers(page, size);
    System.out.println("members = " + members);
    System.out.println("members.getContent() = " + members.getContent());
    System.out.println("members.getTotalElements() = " + members.getTotalElements());
    System.out.println("members.getSize() = " + members.getSize());
    System.out.println("members.getNumber() = " + members.getNumber());
    System.out.println("members.getNumberOfElements() = " + members.getNumberOfElements());
    System.out.println("members.getNumber() = " + members.getNumber());
    return ResponseEntity.ok().body(members);
  }

  //단일 회원 정보 조회
  @GetMapping("/{memberId}")
  public ResponseEntity<MemberDto> getMember(@PathVariable("memberId") Long memberId) {
    return ResponseEntity.ok().body(memberService.getMember(memberId));
  }

  //회원 정보 수정
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @PutMapping("/{memberId}")
  public ResponseEntity<?> updateMember(@Valid @RequestBody MemberUpdateRequestDto updateRequestDto, //최상단으로
                                        BindingResult bindingResult,
                                        @PathVariable("memberId") Long memberId,
                                        @LoggedInUserInfo MemberInfoInToken loginUser,
                                        HttpServletRequest request) {
    log.info("Attempting update for user {} by member(memberId: {})", memberId, loginUser);
    System.out.println("bindingResult = " + bindingResult);
    ResponseEntity<Set<ErrorDto>> errorDtoSet = ControllerUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;
    isAdminOrMemberOwnOrThrow(memberId, loginUser);


    return ResponseEntity.ok().body(memberService.updateMember(memberId, updateRequestDto));
  }

  private void isAdminOrMemberOwnOrThrow(Long uriMemberId, MemberInfoInToken loginMember) {
    if (!loginMember.getRoles().contains(UserRole.ROLE_ADMIN.name())
        && !uriMemberId.equals(loginMember.getMemberId())) {
      throw new AccessDeniedException(
          String.format("member (memberId : %s, memberEmail : %s) doesn't have permission to delete member %s",
              loginMember.getMemberId(), loginMember.getEmail(), uriMemberId)
      );
    }
  }

  //회원 삭제
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @DeleteMapping("/{memberId}")
  public ResponseEntity<?> deleteMember(@PathVariable("memberId") Long memberId,
                                        @LoggedInUserInfo MemberInfoInToken loginMember) {
    log.info("member(memberId: {}) deletion request from memberId: {}, role: {}", memberId, loginMember.getMemberId(), loginMember.getRoles());

    isAdminOrMemberOwnOrThrow(memberId, loginMember);

    memberService.deleteMember(memberId);

    return ResponseEntity.ok().build();
  }


}
