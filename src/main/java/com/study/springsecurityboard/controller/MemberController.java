package com.study.springsecurityboard.controller;

import com.study.springsecurityboard.dto.MemberInfoDto;
import com.study.springsecurityboard.dto.MemberSignupRequestDto;
import com.study.springsecurityboard.dto.MemberSignupResponseDto;
import com.study.springsecurityboard.service.member.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("members")
@Slf4j
public class MemberController {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<MemberSignupResponseDto> signup(@Valid @RequestBody MemberSignupRequestDto request, BindingResult bindingResult){
    log.info("Attempting signup for user {}", request);
    if(bindingResult.hasErrors()){
      log.info("Error occurred  {}", bindingResult);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    MemberSignupResponseDto memberSignupResponseDto = memberService.addMember(request);
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
