package com.study.securitywithjwt.utils;

import com.study.securitywithjwt.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ControllerUtils {
  //controller에서 바인딩 된 에러가 있을 경우 처리

  public static ResponseEntity<Set<ErrorDto>> getErrorResponseFromBindingResult(BindingResult bindingResult, HttpServletRequest request) {
    if (bindingResult.hasErrors()) {
      List<FieldError> fieldErrors = bindingResult.getFieldErrors();
      Set<ErrorDto> errorDtoSet = fieldErrors.stream()
          .map(error -> new ErrorDto(request.getRequestURI(), error.getDefaultMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now()))
          .collect(Collectors.toSet());
      return ResponseEntity.badRequest().body(errorDtoSet);
    }
    return null;
  }
  //자원 생성 후, 자원 생성 uri 반환
  public static URI getCreatedUri(Long id) {
    return ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(id)
        .toUri();
  }
}
