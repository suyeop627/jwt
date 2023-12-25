package com.study.securitywithjwt.dto;

import lombok.*;
//refresh token 전달 dto. 클라이언트의 요청 body에 담긴 토큰을 바인딩하는 역할
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDto {
private String token;
}
