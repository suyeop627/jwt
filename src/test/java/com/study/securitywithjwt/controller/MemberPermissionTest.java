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
          .andExpect(MockMvcResultMatchers.status().isForbidden());
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
          .andExpect(MockMvcResultMatchers.status().isOk());
    }
  }

  @Nested
  class UpdatePermissionTest{
    Member testMember;

      @BeforeEach
      void setUp(){
        testMember = Member.builder()
            .memberId(1L)
            .email("admin@test.com")
            .name("administrator")
            .password(passwordEncoder.encode("00000000"))
            .regdate(LocalDateTime.now())
            .gender(Gender.MALE)
            .roles(Set.of(new Role(3L, UserRole.ROLE_USER)))
            .build();
        Member savedMember = memberRepository.save(testMember);
      }

      //role user -> own data update -> ok
      @Test
      @WithMockCustomUser(roles="USER", memberId = "3")
      void updateMember_roleUserUpdateOwn_returnOk() throws Exception {
        //given
        Long memberId = 3L;

        MemberUpdateRequestDto memberUpdateRequestDto = getMemberUpdateRequestWithId3();


        ResultActions response = mockMvc
            .perform(put("/members/" + memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberUpdateRequestDto)));

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
        MemberUpdateRequestDto memberUpdateRequestDto = getMemberUpdateRequestWithId3();

        ResultActions response = mockMvc
            .perform(put("/members/" + memberIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberUpdateRequestDto)));

        response.andExpect(MockMvcResultMatchers.status()
            .isForbidden());
      }
    //role admin -> other member update
      @Test
      @WithMockCustomUser(roles="ADMIN", memberId = "1")
      void updateMember_roleAdminUpdateOtherMember_returnOk() throws Exception {
        MemberUpdateRequestDto memberUpdateRequestDto = getMemberUpdateRequestWithId3();
        //given
        Long memberId = 1L;
        Long memberIdToUpdate = testMember.getMemberId();

        ResultActions response = mockMvc
            .perform(put("/members/" + memberIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberUpdateRequestDto)));

        response.andExpect(MockMvcResultMatchers.status()
            .isOk());
      }

    private MemberUpdateRequestDto getMemberUpdateRequestWithId3() {
      MemberUpdateRequestDto memberUpdateRequestDto = new MemberUpdateRequestDto();
      memberUpdateRequestDto.setMemberId(3L);
      memberUpdateRequestDto.setPassword("00000000");
      memberUpdateRequestDto.setName("name");
      memberUpdateRequestDto.setEmail("test2123@123.com");
      memberUpdateRequestDto.setPhone("01012341234");
      return memberUpdateRequestDto;
    }


  }
}
