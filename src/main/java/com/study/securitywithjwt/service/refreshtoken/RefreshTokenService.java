package com.study.securitywithjwt.service.refreshtoken;

import com.study.securitywithjwt.domain.RefreshToken;

import java.util.Optional;


public interface RefreshTokenService {

  RefreshToken insertRefreshToken(RefreshToken refreshTokenInstance);

  Optional<RefreshToken> selectRefreshTokenByTokenValue(String refreshToken);

  Optional<RefreshToken> selectRefreshTokenByMemberEmail(String email);

  void deleteRefreshTokenById(Long id);

  void deleteRefreshToken(String token);

  void deleteRefreshTokenByMemberId(Long memberId);
}
