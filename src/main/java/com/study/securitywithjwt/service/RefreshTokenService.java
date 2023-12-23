package com.study.securitywithjwt.service;

import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;


  public RefreshToken insertRefreshToken(RefreshToken refreshToken) {
    return refreshTokenRepository.save(refreshToken);
  }


  public Optional<RefreshToken> selectRefreshTokenByTokenValue(String refreshToken) {
    return refreshTokenRepository.findByToken(refreshToken);
  }


  public Optional<RefreshToken> selectRefreshTokenByMemberEmail(String email) {
    return refreshTokenRepository.findRefreshTokenByMemberEmail(email);
  }


  public void deleteRefreshTokenById(Long id) {
    refreshTokenRepository.deleteById(id);
  }

//refresh token 만료시 호출
  public void deleteRefreshTokenByToken(String token) {
    log.info("refreshTokenRepository.deleteByToken(token)");
    refreshTokenRepository.deleteByToken(token);
  }

//로그아웃 시 호출
  public void deleteRefreshTokenByMemberId(Long memberId) {
    refreshTokenRepository.deleteByMemberId(memberId);
  }
}
