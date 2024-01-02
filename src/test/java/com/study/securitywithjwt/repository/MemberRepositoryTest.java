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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


//@ActiveProfiles("test")
@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)//@DataJpaTest 사용시, 내장 H2 db사용 - mysql적용 안돼서, mysql적용되도록 설정한 db를 사용하도록 replace X
class MemberRepositoryTest {
  @Autowired
  RoleRepository roleRepository;
  @Autowired
  MemberRepository memberRepository;
  Member savedMember1;
  Member savedMember2;
  @Nested
  class selectMember {
    @BeforeEach
    void setUp() {
      savedMember1 = Member.builder()
          .email("member1@test.com")
          .gender(Gender.MALE)
          .name("member1")
          .roles(Set.of(new Role(1L, UserRole.ROLE_ADMIN)))
          .regdate(LocalDateTime.now())
          .phone("01011111111")
          .build();

      savedMember2 = Member.builder()
          .email("member2@test.com")
          .gender(Gender.FEMALE)
          .name("member2")
          .roles(Set.of(new Role(1L, UserRole.ROLE_ADMIN), new Role(2L, UserRole.ROLE_USER)))
          .regdate(LocalDateTime.now())
          .phone("0102222222")
          .build();


      memberRepository.save(savedMember1);
      memberRepository.save(savedMember2);
      Role role = new Role(1L, UserRole.ROLE_ADMIN);
      roleRepository.save(role);
    }

    @AfterEach
    void tearDown() {
      memberRepository.deleteAll();
      roleRepository.deleteAll();
    }

    @Test
    void findByEmail_validState_returnOptionalMember() {
      //given
      String savedMemberEmail = "member1@test.com";
      //when
      Optional<Member> foundMember = memberRepository.findByEmail(savedMemberEmail);
      //then
      assertThat(foundMember.isPresent()).isTrue();
      assertThat((foundMember.get())).isEqualTo(savedMember1);
    }


    @Test
    void existsByEmail_nonexistentEmail_returnFalse() {
      //given
      String unsavedMemberEmail = "member3@test.com";
      //when
      boolean existsMemberByEmail = memberRepository.existsByEmail(unsavedMemberEmail);
      //then
      assertThat(existsMemberByEmail).isFalse();
    }

    @Test
    void existsByEmail_existEmail_returnTrue() {
      //given
      String existEmail = "member1@test.com";
      //when
      boolean existsMemberByEmail = memberRepository.existsByEmail(existEmail);
      //then
      assertThat(existsMemberByEmail).isTrue();
    }


    @Test
    void existsByPhone_nonexistentPhone_returnFalse() {
      //given
      String nonexistentPhone = "010123123123";
      //when
      boolean existsMemberByEmail = memberRepository.existsByPhone(nonexistentPhone);
      //then
      assertThat(existsMemberByEmail).isFalse();
    }

    @Test
    void existsByPhone_existPhone_returnTrue() {
      //given
      String existPhone = "01011111111";
      //when
      boolean existsMemberByEmail = memberRepository.existsByPhone(existPhone);
      //then
      assertThat(existsMemberByEmail).isTrue();
    }
  }


  @Nested
  class MemberPagination {
    @BeforeEach
    void setUp() {
      Role role = new Role(1L, UserRole.ROLE_ADMIN);
      roleRepository.save(role);
    }
    @AfterEach
    void tearDown(){
      memberRepository.deleteAll();
      roleRepository.deleteAll();
    }

    @Test
    void findAllWithPageRequest() {
      //given
      int totalElement = (int) (Math.random() * 30) + 1;//1~30
      int pageSize = (int) (Math.random() * totalElement) + 1; //1~totalElement

      //브라우저에서 선택하는 page 번호 -1 (0부터 시작)
      int pageNumber = (int) (Math.random() * (Math.ceil((double) totalElement / pageSize))); //0~Math.ceil((double)totalElement / pageSize)


      for (int i = 1; i <= totalElement; i++) {//BeforeEach에서 member0, member1 추가해서 2부터 저장
        Member member = Member.builder()
            .phone(String.format("010000000%d", i))
            .roles(Set.of(new Role(1L, UserRole.ROLE_ADMIN)))
            .gender(Gender.MALE).regdate(LocalDateTime.now())
            .password("00000000")
            .email(String.format("member%d@test.com", i))
            .name("name" + i).build();
        memberRepository.save(member);
      }

      PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc("memberId")));

      //when
      Page<Member> memberPages = memberRepository.findAll(pageRequest);
      System.out.printf("totalElement : %s, pageSize : %s, pageNumber : %s%n%n", totalElement, pageSize, pageNumber);
      memberPages.stream().forEach(member -> System.out.println("member = " + member));

      //then
      assertThat(memberPages.getTotalPages()).isEqualTo((int) Math.ceil((double) totalElement / pageSize));
      assertThat(memberPages.getSize()).isEqualTo(pageSize);
      assertThat(memberPages.getNumber()).isEqualTo(pageNumber);
      assertThat(memberPages.getTotalElements()).isEqualTo(totalElement);
      //total 30, pageNumber 2, size 10 -> 30~21 / 20~11 / 10 ~1 -> memberId  = firstMemberID = total-(pageNumber*pageSize)
      //total 5 pageNumber 3, size 2 ->  5~4 / 3~2 / 1/
      int memberNumberOfTopOfFirstPage = (totalElement) - ( pageSize * pageNumber);

      assertThat(memberPages.getContent().get(0).getName()).isEqualTo("name"+(memberNumberOfTopOfFirstPage));
    }
  }
}