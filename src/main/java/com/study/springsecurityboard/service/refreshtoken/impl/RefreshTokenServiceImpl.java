package com.study.springsecurityboard.service.refreshtoken.impl;

import com.study.springsecurityboard.domain.RefreshToken;
import com.study.springsecurityboard.repository.RefreshTokenRepository;
import com.study.springsecurityboard.service.refreshtoken.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  public RefreshToken insertRefreshToken(RefreshToken refreshToken) {
    return refreshTokenRepository.save(refreshToken);
  }

  @Override
  public Optional<RefreshToken> searchRefreshTokenByTokenValue(String refreshToken) {
    return refreshTokenRepository.findByToken(refreshToken);
  }

  @Override
  public Optional<RefreshToken> searchRefreshTokenByMemberEmail(String email) {
    return refreshTokenRepository.findRefreshTokenByMemberEmail(email);
  }

  @Override
  public void deleteRefreshTokenById(Long id) {
    refreshTokenRepository.deleteById(id);
  }

  @Override
  public void deleteRefreshToken(String token) {
    refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
  }
}
