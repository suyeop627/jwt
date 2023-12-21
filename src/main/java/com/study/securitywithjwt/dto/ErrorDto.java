package com.study.securitywithjwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
public class ErrorDto {
  String path;
  String message;
  int statusCode;
  LocalDateTime localDateTime;
}
