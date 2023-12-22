package com.study.securitywithjwt.dto;

import lombok.*;
//refresh token 전달 dto -> 요청 body에 담긴 토큰을 저장함.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDto {
private String token;
}
