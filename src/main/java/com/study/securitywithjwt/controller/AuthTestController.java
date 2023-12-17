package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.domain.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class AuthTestController {

  @GetMapping//permit all
  public ResponseEntity<?> forAuthenticatedUser(){
    return ResponseEntity.ok().body("d");
  }


  @PutMapping//authenitcated
  public ResponseEntity<?> forUserAndAdmin(){
    return ResponseEntity.ok().build();
  }

  @PatchMapping//authenticated
  public ResponseEntity<?> forAdminUser(){
    return ResponseEntity.ok().build();
  }


  @GetMapping("/admin") //role_admin only
  public ResponseEntity<?> forAllUser(){
    return ResponseEntity.ok().build();
  }

}
