package com.study.securitywithjwt.repository;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


//@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)//@DataJpaTest 사용시, 내장 H2 db사용 - mysql적용 안돼서, mysql적용되도록 설정한 db를 사용하도록 replace X
class MemberRepositoryTest {

  @Autowired
  MemberRepository memberRepository;
  Member member1;
  Member member2;

  @BeforeEach
  void setUp() {
    member1 = Member.builder()
        .email("member1@test.com")
        .gender(Gender.MALE)
        .name("member1")
        .roles(Set.of(new Role(1L, UserRole.ROLE_ADMIN)))
        .regdate(LocalDateTime.now())
        .build();

    member2 = Member.builder()
        .email("member2@test.com")
        .gender(Gender.FEMALE)
        .name("member2")
        .roles(Set.of(new Role(1L, UserRole.ROLE_ADMIN), new Role(2L, UserRole.ROLE_USER)))
        .regdate(LocalDateTime.now())
        .build();


    memberRepository.save(member1);
    memberRepository.save(member2);
  }

  @AfterEach
  void tearDown() {
    memberRepository.deleteAll();
  }

  @Test
  void findByEmail_validState_returnOptionalMember() {
    Optional<Member> foundMember = memberRepository.findByEmail("member1@test.com");
    assertThat(foundMember.isPresent()).isTrue();
    assertThat((foundMember.get())).isEqualTo(member1);
  }

  @Nested
  class existsMemberByEmail {
    @Test
    void existsMemberByEmail_nonexistentEmail_returnFalse() {
      boolean existsMemberByEmail = memberRepository.existsMemberByEmail("member3@test.com");
      assertThat(existsMemberByEmail).isFalse();
    }

    @Test
    void existsMemberByEmail_existEmail_returnTrue() {
      boolean existsMemberByEmail = memberRepository.existsMemberByEmail("member1@test.com");
      assertThat(existsMemberByEmail).isTrue();
    }
  }

}