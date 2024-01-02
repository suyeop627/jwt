package com.study.securitywithjwt.service;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.MemberDto;
import com.study.securitywithjwt.dto.MemberSignupRequestDto;
import com.study.securitywithjwt.dto.MemberSignupResponseDto;
import com.study.securitywithjwt.exception.ResourceDuplicatedException;
import com.study.securitywithjwt.exception.ResourceNotFoundException;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RoleRepository;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.MemberDtoMapper;
import com.study.securitywithjwt.utils.member.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock
  MemberRepository memberRepository;

  @Mock
  RoleRepository roleRepository;
  @InjectMocks
  MemberService memberService;

  @Mock
  PasswordEncoder passwordEncoder;
  @Mock
  MemberDtoMapper memberDtoMapper;

  @Nested
  class AddMemberTest {
    @Test
    void addMember_DuplicatedEmail_throwResourceDuplicatedException() {
      //given
      MemberSignupRequestDto requestDto = new MemberSignupRequestDto();
      requestDto.setEmail("test@Email.com");

      given(memberRepository.existsByEmail(anyString())).willReturn(true);

      //when , then
      assertThatThrownBy(() -> memberService.addMember(requestDto)).isInstanceOf(ResourceDuplicatedException.class);

      then(memberRepository).should(times(1)).existsByEmail(requestDto.getEmail());
      then(memberRepository).shouldHaveNoMoreInteractions();
      then(roleRepository).shouldHaveNoInteractions();
      then(passwordEncoder).shouldHaveNoMoreInteractions();
    }

    @Test
    void addMember_DuplicatedPhone_throwResourceDuplicatedException() {
      //given
      MemberSignupRequestDto requestDto = new MemberSignupRequestDto();
      requestDto.setPhone("01000000000");
      given(memberRepository.existsByEmail(any())).willReturn(false);
      given(memberRepository.existsByPhone(anyString())).willReturn(true);

      //when , then
      assertThatThrownBy(() -> memberService.addMember(requestDto)).isInstanceOf(ResourceDuplicatedException.class);

      then(memberRepository).should(times(1)).existsByPhone(requestDto.getPhone());
      then(memberRepository).shouldHaveNoMoreInteractions();
      then(roleRepository).shouldHaveNoInteractions();
      then(passwordEncoder).shouldHaveNoMoreInteractions();
    }

    @Test
    void addMember_validState_returnMemberSignupResponseDto() {
      //given
      MemberSignupRequestDto requestDto = new MemberSignupRequestDto();
      requestDto.setEmail("test@Email.com");
      requestDto.setName("testName");
      requestDto.setPassword("password");
      requestDto.setPhone("01000000000");
      requestDto.setGender(Gender.MALE);

      given(memberRepository.existsByEmail(anyString())).willReturn(false);
      given(memberRepository.existsByPhone(anyString())).willReturn(false);
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
      Long memberId = 1L;
      Member savedMember = Member.builder()
          .memberId(1L)
          .phone("01000000000")
          .email("test@test.com")
          .name("test_name")
          .password("encoded_password")
          .gender(Gender.MALE)
          .build();

      given(memberRepository.findById(anyLong())).willReturn(Optional.of(savedMember));
      given(memberDtoMapper.apply(any(Member.class))).willReturn(
          MemberDto.builder()
              .memberId(1L)
              .phone("01000000000")
              .email("test@test.com")
              .name("test_name")
              .gender(Gender.MALE)
              .build()
      );
      //when
      MemberDto result = memberService.getMember(memberId);
      //then
      assertThat(result)
          .hasFieldOrPropertyWithValue("memberId", savedMember.getMemberId())
          .hasFieldOrPropertyWithValue("email", savedMember.getEmail());
    }

    @Test
    void getMember_nonexistentMemberId_throwResourceNotFoundException() {
      //given
      Long memberId = 1L;
      given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

      //when, then
      assertThatThrownBy(() -> memberService.getMember(memberId)).isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("member id %s is not found", memberId);
    }
  }


  @Test
  void getAllMember_validState_returnMemberDtoPages() {
    //given
    String email = "member@member.com";
    String phone = "01011111111";
    String name = "name";
    Member member = Member.builder()
        .memberId(1L)
        .phone(phone)
        .roles(Set.of(new Role(1L, UserRole.ROLE_ADMIN)))
        .gender(Gender.MALE)
        .regdate(LocalDateTime.now())
        .password("00000000")
        .email(email)
        .name(name)
        .build();


    Page<Member> memberPage = new PageImpl<>(List.of(member));

    given(memberRepository.findAll(any(PageRequest.class))).willReturn(memberPage);
    given(memberDtoMapper.apply(any(Member.class))).willReturn(
        new MemberDto(
            member.getMemberId(),
            member.getEmail(),
            member.getName(),
            member.getPhone(),
            member.getRoleNameSet(),
            member.getGender()
        )
    );

    //when
    Page<MemberDto> memberDtoPage = memberService.getAllMembers(1,1 );

    //then
    assertThat(memberDtoPage.getContent().get(0).getPhone()).isEqualTo(phone);
    assertThat(memberDtoPage.getContent().get(0).getEmail()).isEqualTo(email);
    assertThat(memberDtoPage.getContent().get(0).getName()).isEqualTo(name);
  }
}