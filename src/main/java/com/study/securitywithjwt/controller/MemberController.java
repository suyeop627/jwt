package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.dto.MemberInfoDto;
import com.study.securitywithjwt.dto.MemberSignupRequestDto;
import com.study.securitywithjwt.dto.MemberSignupResponseDto;
import com.study.securitywithjwt.exception.ErrorDto;
import com.study.securitywithjwt.service.member.MemberService;
import com.study.securitywithjwt.utils.RequestValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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



  @GetMapping("{memberId}")
  public ResponseEntity<MemberInfoDto> getMember(@PathVariable("memberId") Integer memberId){
    return ResponseEntity.ok().body(memberService.getMember(memberId));
  }
}
