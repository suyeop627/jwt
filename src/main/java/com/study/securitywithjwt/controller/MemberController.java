package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.dto.ErrorDto;
import com.study.securitywithjwt.dto.MemberSignupRequestDto;
import com.study.securitywithjwt.dto.MemberSignupResponseDto;
import com.study.securitywithjwt.service.member.MemberService;
import com.study.securitywithjwt.utils.RequestValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("members")
@Slf4j
public class MemberController {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<?> signup(@Valid @RequestBody MemberSignupRequestDto signupRequestDto, BindingResult bindingResult, HttpServletRequest request){
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
//TODO 권한 관련 시나리오 지정

//@GetMapping
//public ResponseEntity<MemberInfoDto> getMembers(@PathVariable("memberId") Long memberId){
//  return ResponseEntity.ok().body(memberService.getAllMembers(memberId));
//}
//
//@PutMapping("/memberId")
//public ResponseEntity<MemberInfoDto> updateMember(@PathVariable("memberId") Long memberId){
//
//  return ResponseEntity.ok().body(memberService.updateMember(memberId));
//}
//  @DeleteMapping("/memberId")
//  public ResponseEntity<MemberInfoDto> updateMember(@PathVariable("memberId") Long memberId){
//    return ResponseEntity.ok().body(memberService.deleteMember(memberId));
//  }
//
//
//  @GetMapping("/{memberId}")
//  public ResponseEntity<MemberInfoDto> getMember(@PathVariable("memberId") Integer memberId){
//    return ResponseEntity.ok().body(memberService.getMember(memberId));
//  }
//



  //todo 회원 탈퇴 추가
  //todo 코드 마지막 정리
  //todo
  // 로직 정리


  //get member list - all member

  ///get member - authenticated

  //put member - manager, admin, member by self

  //delete member - admin, member by self

}
