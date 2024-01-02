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

//refresh token 저장
  public RefreshToken insertRefreshToken(RefreshToken refreshToken) {
    log.info("Method: insertRefreshToken called with refreshTokenEntity: {}", refreshToken);
    return refreshTokenRepository.save(refreshToken);
  }

//refresh token(토큰 값 자체)에 해당하는 refresh token 객체 조회
  public Optional<RefreshToken> selectRefreshTokenByTokenValue(String refreshToken) {
    log.info("Method: selectRefreshTokenByTokenValue called with refresh token: {}", refreshToken);
    return refreshTokenRepository.findByToken(refreshToken);
  }

  //회원 email에 해당하는 refresh token 조회
  public Optional<RefreshToken> selectRefreshTokenByMemberEmail(String email) {
    log.info("Method: selectRefreshTokenByTokenValue called with email: {}", email);
    return refreshTokenRepository.findRefreshTokenByMemberEmail(email);
  }

  //refresth token id에 해당하는 값 삭제
  public void deleteRefreshTokenById(Long refreshTokenId) {
    log.info("Method: selectRefreshTokenByTokenValue called with memberId: {}", refreshTokenId);
    refreshTokenRepository.deleteById(refreshTokenId);
  }

//refresh token 만료시 호출되어, 해당 토큰 값에 해당하는 기존 refresh token 삭제.
  @Transactional
  public void deleteRefreshTokenByToken(String refreshToken) {
    log.info("Method: deleteRefreshTokenByToken called with refresh token: {}", refreshToken);
    refreshTokenRepository.deleteByToken(refreshToken);
  }
}
