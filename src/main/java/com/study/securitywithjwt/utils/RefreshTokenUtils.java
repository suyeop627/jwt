package com.study.securitywithjwt.utils;

import com.study.securitywithjwt.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class RefreshTokenUtils {
  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshTokenUtils(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }
//매일 자정에 db에 저장된 refreshToken중, 만료된 토큰을 삭제함
  @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
  public void deleteExpiredRefreshToken() {
    LocalDateTime now = LocalDateTime.now();
    long countOfExpiredRefreshToken = refreshTokenRepository.countByExpiredAtBefore(now);
    log.info("Method: deleteExpiredRefreshToken invoked. Count of expired Refresh token: {}", countOfExpiredRefreshToken);

    refreshTokenRepository.deleteByExpiredAtBefore(now);
  }
}
