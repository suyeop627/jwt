package com.study.securitywithjwt.service.member.impl;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.MemberSignupRequestDto;
import com.study.securitywithjwt.dto.MemberSignupResponseDto;
import com.study.securitywithjwt.exception.ResourceDuplicatedException;
import com.study.securitywithjwt.exception.ResourceNotFoundException;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RoleRepository;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

  @Mock
  MemberRepository memberRepository;

  @Mock
  RoleRepository roleRepository;
  @InjectMocks
  MemberServiceImpl memberService;

  @Mock
  PasswordEncoder passwordEncoder;



  @Nested
  class AddMemberTest {
    @Test
    void addMember_DuplicatedEmail_throwResourceDuplicatedException() {
      //given
      MemberSignupRequestDto requestDto = new MemberSignupRequestDto();
      requestDto.setEmail("test@Email.com");

      given(memberRepository.existsMemberByEmail(anyString())).willReturn(true);

      //when , then
      assertThatThrownBy(() -> memberService.addMember(requestDto)).isInstanceOf(ResourceDuplicatedException.class);

      then(memberRepository).should(times(1)).existsMemberByEmail(requestDto.getEmail());
      then(memberRepository).shouldHaveNoMoreInteractions();
      then(roleRepository).shouldHaveNoInteractions();
      then(passwordEncoder).shouldHaveNoMoreInteractions();
    }

    @Test
    void testAddMember_validState_returnMemberSignupResponseDto() {
      //given
      MemberSignupRequestDto requestDto = new MemberSignupRequestDto();
      requestDto.setEmail("test@Email.com");
      requestDto.setName("testName");
      requestDto.setPassword("password");
      requestDto.setGender(Gender.MALE);

      given(memberRepository.existsMemberByEmail(anyString())).willReturn(false);
      given(passwordEncoder.encode(any())).willReturn("encoded_password");
      given(roleRepository.findByName(any())).willReturn(Optional.of(new Role(1L, UserRole.ROLE_USER)));

      Member savedMember = Member.builder()
          .email(requestDto.getEmail())
          .name(requestDto.getName())
          .password(passwordEncoder.encode(requestDto.getPassword()))
          .gender(requestDto.getGender())
          .build();

      given(memberRepository.save(any(Member.class))).willReturn(savedMember);

      //when
      MemberSignupResponseDto responseDto = memberService.addMember(requestDto);

      // Then
      assertThat(responseDto)
          .isNotNull()
          //filed 선언된 변수, property : getter/setter로 접근할 수 있는 속성
          .hasFieldOrProperty("memberId")
          .hasFieldOrPropertyWithValue("email", requestDto.getEmail())
          .hasFieldOrProperty("regdate")
          .hasFieldOrPropertyWithValue("name", requestDto.getName());

    }
  }

  @Nested
  class GetMemberTest {
    @Test
    void getMember_validState_return() {
      //given
      int memberId = 1;
      Member savedMember = Member.builder()
          .email("test@test.com")
          .name("test_name")
          .password("encoded_password")
          .gender(Gender.MALE)
          .build();

      given(memberRepository.findById(anyLong())).willReturn(Optional.of(savedMember));
      //when

      //then
      assertThat(memberService.getMember(memberId))
          .isNotNull()
          .hasFieldOrPropertyWithValue("memberId", savedMember.getMemberId())
          .hasFieldOrPropertyWithValue("email", savedMember.getEmail());
    }

    @Test
    void getMember_nonexistentMemberId_throwResourceNotFoundException() {
      //given
      int memberId = 1;
      given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

      //when, then
      assertThatThrownBy(() -> memberService.getMember(memberId)).isInstanceOf(ResourceNotFoundException.class);
    }
  }


}