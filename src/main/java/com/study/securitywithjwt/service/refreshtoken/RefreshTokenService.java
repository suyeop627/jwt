package com.study.securitywithjwt.service.refreshtoken;

import com.study.securitywithjwt.domain.RefreshToken;

import java.util.Optional;


public interface RefreshTokenService {

  RefreshToken insertRefreshToken(RefreshToken refreshTokenInstance);

  Optional<RefreshToken> searchRefreshTokenByTokenValue(String refreshToken);

  Optional<RefreshToken> searchRefreshTokenByMemberEmail(String email);

  void deleteRefreshTokenById(Long id);

  void deleteRefreshToken(String token);
}
