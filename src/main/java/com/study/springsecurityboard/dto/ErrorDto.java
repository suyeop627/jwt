package com.study.springsecurityboard.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ErrorDto {
  String path;
  String message;
  String statusCode;
  LocalDateTime localDateTime;
}
