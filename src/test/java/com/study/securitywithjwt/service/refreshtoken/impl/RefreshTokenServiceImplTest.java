package com.study.securitywithjwt.service.refreshtoken.impl;

import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks
  private RefreshTokenServiceImpl refreshTokenService;

  private RefreshToken refreshToken;

  @BeforeEach
  void setUp() {
    refreshToken = new RefreshToken();
    refreshToken.setToken("refreshToken_for_test");
    refreshToken.setMemberId(1L);
    refreshToken.setId(1L);
  }

  @Test
  void testInsertRefreshToken() {
    // Given
    given(refreshTokenRepository.save(refreshToken)).willReturn(refreshToken);

    // When
    RefreshToken result = refreshTokenService.insertRefreshToken(refreshToken);

    // Then
    assertEquals(refreshToken, result);
    then(refreshTokenRepository).should(times(1)).save(refreshToken);
  }

  @Test
  void testSelectRefreshTokenByTokenValue() {
    // Given
    String refreshTokenValue = "refreshToken_for_test";
    given(refreshTokenRepository.findByToken(refreshTokenValue)).willReturn(Optional.of(refreshToken));

    // When
    Optional<RefreshToken> result = refreshTokenService.selectRefreshTokenByTokenValue(refreshTokenValue);

    // Then
    assertEquals(Optional.of(refreshToken), result);
    then(refreshTokenRepository).should(times(1)).findByToken(refreshTokenValue);
  }

  @Test
  void testSelectRefreshTokenByMemberEmail() {
    // Given
    String email = "test@example.com";
    given(refreshTokenRepository.findRefreshTokenByMemberEmail(email)).willReturn(Optional.of(refreshToken));

    // When
    Optional<RefreshToken> result = refreshTokenService.selectRefreshTokenByMemberEmail(email);

    // Then
    assertEquals(Optional.of(refreshToken), result);
    then(refreshTokenRepository).should(times(1)).findRefreshTokenByMemberEmail(email);
  }

  @Test
  void testDeleteRefreshTokenById() {
    // Given
    Long id = 1L;

    // When
    refreshTokenService.deleteRefreshTokenById(id);

    // Then
    then(refreshTokenRepository).should(times(1)).deleteById(id);
  }

  @Test
  void testDeleteRefreshToken() {
    // Given
    String token = "refreshToken_for_test";
    given(refreshTokenRepository.findByToken(token)).willReturn(Optional.of(refreshToken));

    // When
    refreshTokenService.deleteRefreshToken(token);

    // Then
    then(refreshTokenRepository).should(times(1)).findByToken(token);
    then(refreshTokenRepository).should(times(1)).deleteById(refreshToken.getId());
  }
}
