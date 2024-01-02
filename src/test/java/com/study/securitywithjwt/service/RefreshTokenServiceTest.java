package com.study.securitywithjwt.service;

import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
  @Mock
  private RefreshTokenRepository refreshTokenRepository;
  @InjectMocks
  private RefreshTokenService refreshTokenService;
  private RefreshToken refreshToken;
  @BeforeEach
  void setUp() {
    refreshToken = new RefreshToken();
    refreshToken.setToken("refreshToken_for_test");
    refreshToken.setMemberId(1L);
    refreshToken.setId(1L);
  }

  @Test
  void insertRefreshToken_validState_returnSavedRefreshToken() {
    // Given
    given(refreshTokenRepository.save(refreshToken)).willReturn(refreshToken);

    // When
    RefreshToken result = refreshTokenService.insertRefreshToken(refreshToken);

    // Then
    assertEquals(refreshToken, result);
    then(refreshTokenRepository).should(times(1)).save(refreshToken);
  }
@Nested
class SelectRefreshToken{
  @Test
  void selectRefreshTokenByTokenValue_existToken_returnOptionalRefreshToken() {
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
  void selectRefreshTokenByTokenValue_nonexistentToken_returnOptionalEmpty() {
    // Given
    String refreshTokenValue = "refreshToken_for_test";
    given(refreshTokenRepository.findByToken(refreshTokenValue)).willReturn(Optional.empty());

    // When
    Optional<RefreshToken> result = refreshTokenService.selectRefreshTokenByTokenValue(refreshTokenValue);

    // Then
    assertThat(result).isEmpty();
    then(refreshTokenRepository).should(times(1)).findByToken(refreshTokenValue);
  }
  @Test
  void selectRefreshTokenByMemberEmail_existToken_returnOptionalRefreshToken() {
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
  void selectRefreshTokenByMemberEmail_nonexistentToken_returnOptionalRefreshToken() {
    // Given
    String email = "test@example.com";
    given(refreshTokenRepository.findRefreshTokenByMemberEmail(email)).willReturn(Optional.empty());

    // When
    Optional<RefreshToken> result = refreshTokenService.selectRefreshTokenByMemberEmail(email);

    // Then
    assertThat(result).isEmpty();
    then(refreshTokenRepository).should(times(1)).findRefreshTokenByMemberEmail(email);
  }

}
 @Nested
 class DeleteRefreshToken{
   @Test
   void deleteRefreshTokenById_validState_callDeleteById() {
     // Given
     Long id = 1L;

     // When
     refreshTokenService.deleteRefreshTokenById(id);

     // Then
     then(refreshTokenRepository).should(times(1)).deleteById(id);
   }

   @Test
   void deleteRefreshToken_validState_callDeleteById() {
     // Given
     String token = "refreshToken_for_test";
     // When
     refreshTokenService.deleteRefreshTokenByToken(token);

     // Then
     then(refreshTokenRepository).should(times(1)).deleteByToken(token);
   }
 }
}
