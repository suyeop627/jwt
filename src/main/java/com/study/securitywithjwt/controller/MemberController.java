package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.service.MemberService;
import com.study.securitywithjwt.utils.RequestValidationUtils;
import com.study.securitywithjwt.utils.annotation.LoggedInUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    ResponseEntity<Set<ErrorDto>> errorDtoSet = RequestValidationUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;
    MemberSignupResponseDto memberSignupResponseDto = memberService.addMember(signupRequestDto);
    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(memberSignupResponseDto.getMemberId())
        .toUri();
    log.info("Created user uri: {}", uri);
    return ResponseEntity.created(uri).body(memberSignupResponseDto);
  }

  //회원 목록 조회
  @GetMapping
  public ResponseEntity<Page<MemberDto>> getAllMembers(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                                       @RequestParam(name = "size", defaultValue = "10") Integer size) {
    Page<MemberDto> members = memberService.getAllMembers(page, size);
    return ResponseEntity.ok().body(members);
  }

  //단일 회원 정보 조회
  @GetMapping("/{memberId}")
  public ResponseEntity<MemberDto> getMember(@PathVariable("memberId") Long memberId) {
    return ResponseEntity.ok().body(memberService.getMember(memberId));
  }
  //회원 정보 수정
  @PutMapping("/{memberId}")
  public ResponseEntity<?> updateMember(@PathVariable("memberId") Long memberId,
                                        @Valid @RequestBody MemberUpdateRequestDto updateRequestDto,
                                        BindingResult bindingResult,
                                        HttpServletRequest request) throws BadRequestException {
    log.info("Attempting signup for user {}", updateRequestDto);
    ResponseEntity<Set<ErrorDto>> errorDtoSet = RequestValidationUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;
    if (!updateRequestDto.getMemberId().equals(memberId)) {
      throw new BadRequestException("invalid access to update member");
    }

    return ResponseEntity.ok().body(memberService.updateMember(memberId, updateRequestDto));
  }

  //회원 삭제
  @DeleteMapping("/{memberId}")
  public ResponseEntity<?> deleteMember(@PathVariable("memberId") Long memberId,
                                                @LoggedInUserInfo MemberInfoInToken loginMember) throws BadRequestException {
    memberService.deleteMember(memberId);
    return ResponseEntity.ok().build();
  }


}
