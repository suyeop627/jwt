package com.study.securitywithjwt.dao;

import com.study.securitywithjwt.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
  Optional<RefreshToken> findByToken(String refreshToken);

  @Query("SELECT r FROM RefreshToken r JOIN Member m ON m.memberId=r.memberId WHERE m.email = :email")
  Optional<RefreshToken> findRefreshTokenByMemberEmail(@Param("email") String email);
}
