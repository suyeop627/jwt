package com.study.springsecurityboard.member.controller;

import com.study.springsecurityboard.member.dto.MemberSignupRequestDto;
import com.study.springsecurityboard.member.dto.MemberSignupResponseDto;
import com.study.springsecurityboard.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("members")
@Slf4j
public class MemberController {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<MemberSignupResponseDto> signup(@Valid @RequestBody MemberSignupRequestDto request, BindingResult bindingResult){
    log.info("MemberSignupRequestDto : {}", request);
    if(bindingResult.hasErrors()){
      System.out.println("bindingResult = " + bindingResult);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    MemberSignupResponseDto memberSignupResponseDto = memberService.addMember(request);

    return new ResponseEntity<>(memberSignupResponseDto, HttpStatus.CREATED);
  }
}
