package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.dto.MemberInfo;
import com.study.securitywithjwt.dto.MemberInfoDto;
import com.study.securitywithjwt.utils.annotation.IfUserLoggedIn;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class HomeController {

  //for any user
  @GetMapping
  public String home() {
    return "home";
  }
  @PostMapping("board")
  public String dd(@IfUserLoggedIn MemberInfo memberInfo){
    System.out.println("memberInfo = " + memberInfo);
    return "board";
  }
}
