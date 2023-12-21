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


  public void deleteRefreshToken(String token) {

    Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(token);
    try {
      if(optionalToken.isPresent()){
        refreshTokenRepository.deleteById(optionalToken.get().getId());
        log.info("logout complete, member id : {}", optionalToken.get().getMemberId());
      }
    }catch (Exception e){
      log.error("deleteRefreshToken(token), token to delete is nonexistent. token : {}", token);
      e.printStackTrace();
    }
  }


  public void deleteRefreshTokenByMemberId(Long memberId) {
    refreshTokenRepository.deleteByMemberId(memberId);
  }
}
