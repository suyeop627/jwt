package com.study.securitywithjwt.utils;

import com.study.securitywithjwt.dto.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingUtils {
  public static void loggingErrorDto(ErrorDto errorDto) {
    log.error("ControllerExceptionHandler invoked, path: {}, statusCode: {}, message: {}, localDateTime: {}",
        errorDto.getPath(), errorDto.getStatusCode(), errorDto.getMessage(), errorDto.getLocalDateTime());
  }
}
