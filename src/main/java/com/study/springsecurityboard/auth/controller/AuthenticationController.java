package com.study.springsecurityboard.auth.controller;

import com.study.springsecurityboard.auth.dto.LoginRequestDto;
import com.study.springsecurityboard.auth.dto.LoginResponseDto;
import com.study.springsecurityboard.member.domain.Member;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {

  @PostMapping("login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto, BindingResult bindingResult){
    if(bindingResult.hasErrors()){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }





    return null;
  }
}
