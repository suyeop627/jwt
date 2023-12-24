package com.study.securitywithjwt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.TestConfig;
import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.MemberUpdateRequestDto;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RoleRepository;
import com.study.securitywithjwt.service.MemberService;
import com.study.securitywithjwt.testUtils.customMockUser.WithMockCustomUser;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
@Import(TestConfig.class)
public class MemberPermissionTest {
  @Autowired
  MockMvc mockMvc;
  @Autowired
  PasswordEncoder passwordEncoder;
  @Autowired
  RoleRepository roleRepository;
  @Autowired
  MemberRepository memberRepository;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  MemberService memberService;
  //@BeforeEach에서 생성되는 Member의 필드 값 중복 방지
  //java.util.concurrent.atomic : atomic 연산 지원
  //atomic 연산 : 중간에 다른 스레드가 개입하여 데이터의 무결성이 깨지는 상황을 방지하기 위한 연산
  //vs static 변수 : static은 모든 인스턴스가 공유함. atomic은 인스턴스 단위로 존재하며 인스턴스간 공유되지 않음.

  //스레드 :스레드(Thread)는 컴퓨터 프로세스 내에서 실행되는 실행 흐름을 나타냅니다.
  // 간단히 말해, 하나의 프로세스 안에서 여러 작업을 동시에 수행할 수 있도록 하는 실행 단위입니다.
  // 스레드는 각각 독립적인 코드 실행 경로를 가지며, 서로 다른 스레드는 동시에 실행될 수 있습니다.

  //프로세스 : 프로세스(Process)는 실행 중인 프로그램으로, 메모리에 올라가서 CPU의 실행을 기다리는 상태를 나타냅니다.
  // 각 프로세스는 독립된 메모리 공간을 가지며, 운영체제에 의해 관리되는 실행 단위입니다.

  private static final AtomicLong counter = new AtomicLong(1);

  @Nested
  class DeletionAuthTest {
    @WithMockCustomUser(roles = "USER", memberId = "1")
    @Test
    void deleteMember_deleteOwn_deleteMemberAndReturnOk() throws Exception {
      //given
      long memberIdToDelete = 1L;

      //when
      ResultActions response = mockMvc
          .perform(delete("/members/" + memberIdToDelete));

      //then
      response
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockCustomUser(roles = "USER", memberId = "100")
    void deleteMember_differentMemberByRoleUser_deleteMemberAndReturn403() throws Exception {
      //given
      long memberIdToDelete = 1L;

      //when
      ResultActions response = mockMvc
          .perform(delete("/members/" + memberIdToDelete));
      //then
      response
          .andExpect(MockMvcResultMatchers.status()
              .isForbidden());
    }

    @Test
    @WithMockCustomUser(roles = "ADMIN", memberId = "100")
    void deleteMember_differentMemberByRoleAdmin_deleteMemberAndReturn200() throws Exception {
      //given
      long memberIdToDelete = 1L;
      //when
      ResultActions response = mockMvc
          .perform(delete("/members/" + memberIdToDelete));

      //then
      response
          .andExpect(MockMvcResultMatchers.status()
              .isOk());
    }
  }

  @Nested
  class UpdatePermissionTest{
    Member testMember;
    //TestConfig 에서 admin(id=1), manager(id=2), user (id=3) member 를 생성하여 사용함.

      @BeforeEach
      void setUp(){
        long avoidDuplicationNumber = counter.getAndIncrement();
        System.out.println("avoidDuplicationNumber = " + avoidDuplicationNumber);

        testMember = Member.builder()
            .email("testUser%d@test.com".formatted(avoidDuplicationNumber))
            .name("testUser")
            .phone("0101234883"+avoidDuplicationNumber)
            .password(passwordEncoder.encode("00000000"))
            .regdate(LocalDateTime.now())
            .gender(Gender.MALE)
            .roles(Set.of(new Role(3L, UserRole.ROLE_USER)))
            .build();
        memberRepository.save(testMember);
      }

      //role user -> own data update -> ok
      @Test
      @WithMockCustomUser(roles="USER", memberId = "3")
      void updateMember_roleUserUpdateOwn_returnOk() throws Exception {
        //given
        long memberId = 3L;

        MemberUpdateRequestDto memberUpdateRequestDto = getMemberUpdateRequestWithoutId();
        memberUpdateRequestDto.setMemberId(memberId);

        //when
        ResultActions response = mockMvc
            .perform(put("/members/" + memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberUpdateRequestDto)));

        //then
        response.andExpect(MockMvcResultMatchers.status()
            .isOk());
      }

      //role user -> other member update -> forbidden
      @Test
      @WithMockCustomUser(roles="USER", memberId = "3")
      void updateMember_roleUserUpdateOtherMember_returnForbidden() throws Exception {
        //given
        Long memberId = 3L;
        Long memberIdToUpdate = testMember.getMemberId();
        MemberUpdateRequestDto memberUpdateRequestDto = getMemberUpdateRequestWithoutId();
        memberUpdateRequestDto.setMemberId(memberIdToUpdate);

        //when
        ResultActions response = mockMvc
            .perform(put("/members/" + memberIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberUpdateRequestDto)));
        //then
        response.andExpect(MockMvcResultMatchers.status()
            .isForbidden());
      }
    //role admin -> other member update
      @Test
      @WithMockCustomUser(roles="ADMIN", memberId = "1")
      void updateMember_roleAdminUpdateOtherMember_returnOk() throws Exception {
        //given
        Long memberId = 1L;
        Long memberIdToUpdate = testMember.getMemberId();

        MemberUpdateRequestDto memberUpdateRequestDto = getMemberUpdateRequestWithoutId();
        memberUpdateRequestDto.setMemberId(memberIdToUpdate);
        //when
        ResultActions response = mockMvc
            .perform(put("/members/" + memberIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberUpdateRequestDto)));
        //then
        response.andExpect(MockMvcResultMatchers.status()
            .isOk());
      }

    private MemberUpdateRequestDto getMemberUpdateRequestWithoutId() {
      MemberUpdateRequestDto memberUpdateRequestDto = new MemberUpdateRequestDto();
      memberUpdateRequestDto.setPassword("00000000");
      memberUpdateRequestDto.setName("name");
      memberUpdateRequestDto.setEmail("test2123@123.com");
      memberUpdateRequestDto.setPhone("01012341234");
      return memberUpdateRequestDto;
    }
  }
}
