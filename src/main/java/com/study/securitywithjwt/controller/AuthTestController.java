package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.dto.MemberInfoInToken;
import com.study.securitywithjwt.utils.annotation.LoggedInUserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthTestController {

  @GetMapping//permit all
  public ResponseEntity<?> getForAllUsers(@LoggedInUserInfo MemberInfoInToken memberDto){
    return ResponseEntity.ok().body(memberDto);
  }

  @PostMapping//authenitcated
  public ResponseEntity<?> postForAuthenticatedUsers(@LoggedInUserInfo MemberInfoInToken memberDto){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    System.out.println("authentication = " + authentication);


    return ResponseEntity.ok().body(memberDto);
  }

  @GetMapping("/admin") //manager / role_admin
  public ResponseEntity<?> getForAdminAndManager(@LoggedInUserInfo MemberInfoInToken memberDto){
    return ResponseEntity.ok().body(memberDto);
  }

  @PostMapping("/admin") //role_admin only
  public ResponseEntity<?> postForAdmin(@LoggedInUserInfo MemberInfoInToken memberDto){
    return ResponseEntity.ok().body(memberDto);
  }

  @DeleteMapping("/admin") //role_admin only
  public ResponseEntity<?> deleteForAdmin(@LoggedInUserInfo MemberInfoInToken memberDto){
    return ResponseEntity.ok().body(memberDto);
  }
}
