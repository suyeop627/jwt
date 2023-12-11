package com.study.springsecurityboard.service.refreshtoken;

import com.study.springsecurityboard.domain.RefreshToken;

import java.util.Optional;


public interface RefreshTokenService {

  RefreshToken insertRefreshToken(RefreshToken refreshTokenInstance);

  Optional<RefreshToken> searchRefreshTokenByTokenValue(String refreshToken);

  Optional<RefreshToken> searchRefreshTokenByMemberEmail(String email);

  void deleteRefreshTokenById(Long id);

  void deleteRefreshToken(String token);
}
