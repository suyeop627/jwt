package com.study.securitywithjwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
//애플리케이션에서 발생하는 에러 전달 dto
@Data
@Builder
@AllArgsConstructor
public class ErrorDto {
  String path;
  String message;
  int statusCode;
  LocalDateTime localDateTime;
}
