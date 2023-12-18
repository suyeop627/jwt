package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.MemberInfo;
import com.study.securitywithjwt.utils.annotation.LoggedInUserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthTestController {

  @GetMapping//permit all
  public ResponseEntity<?> getForAllUsers(@LoggedInUserInfo MemberInfo memberInfo){
    return ResponseEntity.ok().body(memberInfo);
  }

  @PutMapping//authenitcated
  public ResponseEntity<?> putForAuthenticatedUsers(@LoggedInUserInfo MemberInfo memberInfo){
    return ResponseEntity.ok().body(memberInfo);
  }

  @PatchMapping//authenticated
  public ResponseEntity<?> patchForAuthenticatedUsers(@LoggedInUserInfo MemberInfo memberInfo){
    return ResponseEntity.ok().body(memberInfo);
  }
  @PostMapping//authenticated
  public ResponseEntity<?> postForAuthenticatedUsers(@LoggedInUserInfo MemberInfo memberInfo){
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
}
