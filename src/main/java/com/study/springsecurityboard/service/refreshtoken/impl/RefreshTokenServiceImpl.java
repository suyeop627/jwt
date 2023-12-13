package com.study.springsecurityboard.service.refreshtoken.impl;

import com.study.springsecurityboard.domain.RefreshToken;
import com.study.springsecurityboard.repository.RefreshTokenRepository;
import com.study.springsecurityboard.service.refreshtoken.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
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

    Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(token);
    try {
      if(optionalToken.isPresent()){
        refreshTokenRepository.deleteById(optionalToken.get().getId());
        log.info("logout complete, member id : {}", optionalToken.get().getMemberId());
      }else{
        //todo controller exception 처리
        log.info("token not found");

      }
    }catch (Exception e){
      e.printStackTrace();
    }
  }
}
