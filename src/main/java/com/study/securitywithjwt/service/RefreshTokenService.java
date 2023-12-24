package com.study.securitywithjwt.service;

import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;


  public RefreshToken insertRefreshToken(RefreshToken refreshToken) {
    log.info("Method: insertRefreshToken called with refreshTokenEntity: {}", refreshToken);
    return refreshTokenRepository.save(refreshToken);
  }


  public Optional<RefreshToken> selectRefreshTokenByTokenValue(String refreshToken) {
    log.info("Method: selectRefreshTokenByTokenValue called with refresh token: {}", refreshToken);
    return refreshTokenRepository.findByToken(refreshToken);
  }


  public Optional<RefreshToken> selectRefreshTokenByMemberEmail(String email) {
    log.info("Method: selectRefreshTokenByTokenValue called with email: {}", email);
    return refreshTokenRepository.findRefreshTokenByMemberEmail(email);
  }


  public void deleteRefreshTokenById(Long id) {
    log.info("Method: selectRefreshTokenByTokenValue called with memberId: {}", id);
    refreshTokenRepository.deleteById(id);
  }

//refresh token 만료시 호출
  public void deleteRefreshTokenByToken(String refreshToken) {
    log.info("Method: deleteRefreshTokenByToken called with refresh token: {}", refreshToken);
    refreshTokenRepository.deleteByToken(refreshToken);
  }

//로그아웃 시 호출
  @Transactional
  public void deleteRefreshTokenByMemberId(Long memberId) {
    log.info("Method: deleteRefreshTokenByMemberId called with memberId: {}", memberId);
    refreshTokenRepository.deleteByMemberId(memberId);
  }
}
