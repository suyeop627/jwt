package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.dto.MemberInfo;
import com.study.securitywithjwt.utils.annotation.LoggedInUserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

  //for any user
  @GetMapping
  public String home() {
    return "home";
  }
  @PostMapping("board")
  public String dd(@LoggedInUserInfo MemberInfo memberInfo){
    System.out.println("memberInfo = " + memberInfo);
    return "board";
  }
}
