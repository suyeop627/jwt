package com.study.securitywithjwt.utils;

import com.study.securitywithjwt.dto.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingUtils {
  //authenticationEntryPoint 및 ControllerException에서 ErrorDto를 생성한 뒤 로그 출력함.
  public static void loggingErrorDto(ErrorDto errorDto) {
    log.debug("ErrorDto created. ErrorDto(path: {}, statusCode: {}, message: {}, localDateTime: {})",
        errorDto.getPath(), errorDto.getStatusCode(), errorDto.getMessage(), errorDto.getLocalDateTime());
  }
}
