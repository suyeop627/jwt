package com.study.springsecurityboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board")
public class BoardController {
@GetMapping
  public String boardHome(){
  return "boardHome";
}
  @PostMapping
  public String writePost(){
    return "writePost";
  }
}
