package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.dto.MemberInfo;
import com.study.securitywithjwt.utils.annotation.LoggedInUserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthTestController {

  @GetMapping//permit all
  public ResponseEntity<?> getForAllUsers(@LoggedInUserInfo MemberInfo memberInfo){
    return ResponseEntity.ok().body(memberInfo);
  }

  @PostMapping//authenitcated
  public ResponseEntity<?> postForAuthenticatedUsers(@LoggedInUserInfo MemberInfo memberInfo){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    System.out.println("authentication = " + authentication);


    return ResponseEntity.ok().body(memberInfo);
  }

  @GetMapping("/admin") //manager / role_admin
  public ResponseEntity<?> getForAdminAndManager(@LoggedInUserInfo MemberInfo memberInfo){
    return ResponseEntity.ok().body(memberInfo);
  }

  @PostMapping("/admin") //role_admin only
  public ResponseEntity<?> postForAdmin(@LoggedInUserInfo MemberInfo memberInfo){
    return ResponseEntity.ok().body(memberInfo);
  }

  @DeleteMapping("/admin") //role_admin only
  public ResponseEntity<?> deleteForAdmin(@LoggedInUserInfo MemberInfo memberInfo){
    return ResponseEntity.ok().body(memberInfo);
  }
}
