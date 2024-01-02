package com.study.securitywithjwt.repository;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


//@ActiveProfiles("test")
@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)//@DataJpaTest 사용시, 내장 H2 db사용 - mysql적용 안돼서, mysql적용되도록 설정한 db를 사용하도록 replace X
class RefreshTokenRepositoryTest {
  @Autowired
  RefreshTokenRepository refreshTokenRepository;

  @Autowired
  MemberRepository memberRepository;
  @Autowired
  RoleRepository roleRepository;
  String token;
  Member savedMember;

  @BeforeEach
  void setUp() {
    Role adminRole = new Role(1L, UserRole.ROLE_ADMIN);//role 먼저 저장하지 않으면 jointable 생성단계에서 에러발생함
    roleRepository.save(adminRole);
    Member member1 = Member.builder()
        .email("member1@test.com")
        .gender(Gender.MALE)
        .name("member1")
        .roles(Set.of(adminRole))
        .regdate(LocalDateTime.now())
        .build();
    savedMember = memberRepository.save(member1);
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
  void tearDown() {
    refreshTokenRepository.deleteAll();
    memberRepository.deleteAll();

  }

  @Nested
  class findByToken {
    @Test
    void findByToken_existToken_returnOptionalRefreshToken() {
      //given - beforeEach
      //when
      Optional<RefreshToken> refreshTokenFoundByToken = refreshTokenRepository.findByToken(token);
      //then
      assertThat(refreshTokenFoundByToken.isPresent()).isTrue();
    }

    @Test
    void findByToken_nonexistentToken_returnOptionalEmpty() {
      //given
      token = token + 1; //token not saved
      //when
      Optional<RefreshToken> refreshTokenFoundByToken = refreshTokenRepository.findByToken(token);
      //then
      assertThat(refreshTokenFoundByToken.isEmpty()).isTrue();
    }
  }

  @Nested
  class findRefreshTokenByMemberEmail {
    @Test
    void findRefreshTokenByMemberEmail_existEmail_returnOptionalRefreshToken() {
      //given
      String memberEmail = savedMember.getEmail(); //saved email
      //when
      Optional<RefreshToken> refreshTokenFoundByEmail = refreshTokenRepository.findRefreshTokenByMemberEmail(memberEmail);
      //then
      assertThat(refreshTokenFoundByEmail.isPresent()).isTrue();
    }

    @Test
    void findRefreshTokenByMemberEmail_nonexistentEmail_returnOptionalEmpty() {
      //given
      String memberEmail = "member2@test.com"; //email not saved
      //when
      Optional<RefreshToken> refreshTokenFoundByEmail = refreshTokenRepository.findRefreshTokenByMemberEmail(memberEmail);
      //when
      assertThat(refreshTokenFoundByEmail.isEmpty()).isTrue();
    }
  }

  @Nested
  class deleteByMemberId {
    @Test
    void deleteByMemberId_noneExistentMemberId_deleteRefreshToken() {
      //given
      Long memberId = 100L;
      long refreshTokenRepositoryCountBeforeDeletion = refreshTokenRepository.count();
      //when
      refreshTokenRepository.deleteByMemberId(memberId);
      //then
      assertThat(refreshTokenRepository.count() == refreshTokenRepositoryCountBeforeDeletion).isTrue();
    }

    @Test
    void deleteByMemberId_validState_deleteRefreshToken() {
      //given
      long refreshTokenRepositoryCountBeforeDeletion = refreshTokenRepository.count();
      Long memberId = savedMember.getMemberId();
      //when
      refreshTokenRepository.deleteByMemberId(memberId);
      //then
      assertThat(refreshTokenRepository.count() == refreshTokenRepositoryCountBeforeDeletion - 1).isTrue();
    }
  }

  @Nested
  class CleanupRefreshToken {
    int expiredTokenCount = 4;
    int generatedRefreshTokenCount = 10;
    long countBeforeSaveAll;
    List<RefreshToken> refreshTokenList;

    @BeforeEach
    void setUp(){
      refreshTokenList = new ArrayList<>();
      countBeforeSaveAll = refreshTokenRepository.count();

      for (long i = 1; i <= generatedRefreshTokenCount; i++) {
        LocalDateTime expiredAt = i <= expiredTokenCount ?
            LocalDateTime.now().minusHours(i) :
            LocalDateTime.now().plusHours(i);

        RefreshToken refreshToken = RefreshToken.builder()
            .token("testToken" + i)
            .memberId(i)
            .expiredAt(expiredAt)
            .build();

        refreshTokenList.add(refreshToken);
      }

      refreshTokenRepository.saveAll(refreshTokenList);
    }
    @AfterEach
    void tearDown(){
      refreshTokenRepository.deleteAll();
    }

    @Test
    void cleanupRefreshToken_countExpiredToken_returnCount(){
      //given
      LocalDateTime now = LocalDateTime.now();
      //when
      long countByExpiredAtBefore = refreshTokenRepository.countByExpiredAtBefore(now);
      //then
      assertThat(countByExpiredAtBefore).isEqualTo(expiredTokenCount);
    }
    @Test
    void cleanupRefreshToken_expiredTokenExist_deleteExpiredToken() {
      //given
      LocalDateTime now = LocalDateTime.now();
      //when
      refreshTokenRepository.deleteByExpiredAtBefore(now);
      //then
      assertThat(refreshTokenRepository.count()).isEqualTo((generatedRefreshTokenCount - expiredTokenCount) + countBeforeSaveAll);
    }
  }
}


