package com.study.securitywithjwt.repository;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.jwt.JwtUtils;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import io.jsonwebtoken.Jwts;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Set;


//@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)//@DataJpaTest 사용시, 내장 H2 db사용 - mysql적용 안돼서, mysql적용되도록 설정한 db를 사용하도록 replace X
class RefreshTokenRepositoryTest {
  @Autowired
  RefreshTokenRepository refreshTokenRepository;

  @Autowired
  MemberRepository memberRepository;
  String token;
  @BeforeEach
  void setUp() {
    Member member1 = Member.builder()
        .email("member1@test.com")
        .gender(Gender.MALE)
        .name("member1")
        .roles(Set.of(new Role(1L, UserRole.ROLE_ADMIN)))
        .regdate(LocalDateTime.now())
        .build();
    Member savedMember = memberRepository.save(member1);

    token = Jwts.builder()
        .subject("member1@test.com")
        .issuedAt(new Date())
        .expiration(new Date(new Date().getTime() + 1000 * 60))
        .compact();
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(token);
    refreshToken.setMemberId(savedMember.getMemberId());
    refreshTokenRepository.save(refreshToken);

  }
  @AfterEach
  void tearDown(){
    refreshTokenRepository.deleteAll();
    memberRepository.deleteAll();
  }


  @Test
  void findByToken_returnOptionalRefreshToken() {
    Optional<RefreshToken> refreshTokenFoundByToken = refreshTokenRepository.findByToken(token);
    Assertions.assertThat(refreshTokenFoundByToken.isPresent()).isTrue();
  }

  @Test
  void findByToken_returnOptionalEmpty() {
    Optional<RefreshToken> refreshTokenFoundByToken = refreshTokenRepository.findByToken(token+"1");
    Assertions.assertThat(refreshTokenFoundByToken.isEmpty()).isTrue();
  }


  @Test
  void findRefreshTokenByMemberEmail_returnOptionalRefreshToken() {
    Optional<RefreshToken> refreshTokenFoundByEmail = refreshTokenRepository.findRefreshTokenByMemberEmail("member1@test.com");
    Assertions.assertThat(refreshTokenFoundByEmail.isPresent()).isTrue();
  }

  @Test
  void findRefreshTokenByMemberEmail_returnOptionalEmpty() {
    Optional<RefreshToken> refreshTokenFoundByEmail = refreshTokenRepository.findRefreshTokenByMemberEmail("member2@test.com");
    Assertions.assertThat(refreshTokenFoundByEmail.isEmpty()).isTrue();
  }
}