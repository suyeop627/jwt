package com.study.securitywithjwt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.dto.MemberSignupRequestDto;
import com.study.securitywithjwt.dto.MemberSignupResponseDto;
import com.study.securitywithjwt.exception.ErrorDto;
import com.study.securitywithjwt.jwt.JwtAuthenticationProvider;
import com.study.securitywithjwt.service.member.MemberService;
import com.study.securitywithjwt.utils.member.Gender;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(MemberController.class)
class MemberControllerTest {
  @MockBean
  MemberService memberService;

  @MockBean
  JwtAuthenticationProvider jwtAuthenticationProvider;

  @MockBean
  PasswordEncoder passwordEncoder;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void signup_validState_returnSignupResponseDto() throws Exception {
    //given
    MemberSignupRequestDto memberSignupRequestDto = new MemberSignupRequestDto();
    memberSignupRequestDto.setGender(Gender.MALE);
    memberSignupRequestDto.setPassword("00000000");
    memberSignupRequestDto.setEmail("test@test.com");
    memberSignupRequestDto.setName("testName");

    MemberSignupResponseDto expectedMemberSignupResponseDto = MemberSignupResponseDto.builder()
        .memberId(1L)
        .email(memberSignupRequestDto.getEmail())
        .regdate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))//소수점 이하 자리수 문제 방지하기 위해서 자릿수 조절.
        .name(memberSignupRequestDto.getName())
        .build();
    given(memberService.addMember(any())).willReturn(expectedMemberSignupResponseDto);

    //when
    ResultActions response = mockMvc.perform(post("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(expectedMemberSignupResponseDto)));

    //then
    response.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.header().string("location", Matchers.endsWith("members/" + expectedMemberSignupResponseDto.getMemberId())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.memberId").value(Matchers.is(expectedMemberSignupResponseDto.getMemberId()), Long.class))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is(expectedMemberSignupResponseDto.getEmail())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(expectedMemberSignupResponseDto.getName())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.regdate", Matchers.is(expectedMemberSignupResponseDto.getRegdate().toString())))
        .andDo(MockMvcResultHandlers.print());

    //value() -타입을 떠나서 같은 값을 가지는지 확인 / type을 지정해줄 수도 잇음.
    // value(Matchers.is(expectedMemberSignupResponseDto.getMemberId()),Long.class))
    // value(expectedMemberSignupResponseDto.getMemberId()))

    //Matchers.is / equalTo- 타입, 값 정확하게 확인
  }

  @Nested
  class SignUpValidationTest {
    @Test
    public void signup_invalidEmailAndPasswordAndName_return400ErrorDtos() throws Exception {
      // Given
      MemberSignupRequestDto signupRequestDto = new MemberSignupRequestDto();
      signupRequestDto.setPassword("1");
      signupRequestDto.setEmail("a");
      signupRequestDto.setGender(Gender.MALE);
      signupRequestDto.setName("1");

      List<ErrorDto> expectedErrors = Arrays.asList(
          new ErrorDto("/members", "must be a well-formed email address", HttpStatus.BAD_REQUEST.value(), LocalDateTime.now()),
          new ErrorDto("/members", "password size must be between 8 and 16", HttpStatus.BAD_REQUEST.value(), LocalDateTime.now()),
          new ErrorDto("/members", "name size must be between 2 and 16", HttpStatus.BAD_REQUEST.value(), LocalDateTime.now())
      );

      // When
      ResultActions response = mockMvc.perform(post("/members")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequestDto)));
      //then
      response.andExpect(MockMvcResultMatchers.status().isBadRequest())
          .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(expectedErrors.size())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].path", Matchers.containsInAnyOrder(expectedErrors.stream().map(ErrorDto::getPath).toArray())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].message", Matchers.containsInAnyOrder(expectedErrors.stream().map(ErrorDto::getMessage).toArray())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].statusCode", Matchers.containsInAnyOrder(expectedErrors.stream().map(ErrorDto::getStatusCode).toArray())))
          .andDo(MockMvcResultHandlers.print());

      then(memberService).shouldHaveNoInteractions();

    }

    @Test
    public void signup_passwordLessThan8_return400ErrorDto() throws Exception {
      // Given
      MemberSignupRequestDto signupRequestDto = new MemberSignupRequestDto();
      signupRequestDto.setPassword("2");
      signupRequestDto.setEmail("test@test.com");
      signupRequestDto.setGender(Gender.MALE);
      signupRequestDto.setName("testName");

      ErrorDto errorDto = new ErrorDto("/members", "password size must be between 8 and 16", HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());


      // When
      ResultActions response = mockMvc.perform(post("/members")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequestDto)));
      //then
      compareResponseWithSingleExpectedErrorDto(errorDto, response);

      then(memberService).shouldHaveNoInteractions();

    }


    @Test
    public void signup_nameLessThen2_return400ErrorDto() throws Exception {
      // Given
      MemberSignupRequestDto signupRequestDto = new MemberSignupRequestDto();
      signupRequestDto.setPassword("3123123123");
      signupRequestDto.setEmail("test@test.com");
      signupRequestDto.setGender(Gender.MALE);
      signupRequestDto.setName("2");

      ErrorDto errorDto = new ErrorDto("/members", "name size must be between 2 and 16", HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
      // When
      ResultActions response = mockMvc.perform(post("/members")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequestDto)));

      //then
      compareResponseWithSingleExpectedErrorDto(errorDto, response);

      then(memberService).shouldHaveNoInteractions();

    }


    @Test
    public void signup_invalidEmail_return400errorDto() throws Exception {
      // Given
      MemberSignupRequestDto signupRequestDto = new MemberSignupRequestDto();
      signupRequestDto.setPassword("323333123");
      signupRequestDto.setEmail("1");
      signupRequestDto.setGender(Gender.MALE);
      signupRequestDto.setName("testName");

      ErrorDto errorDto = new ErrorDto("/members", "must be a well-formed email address", HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());

      // When
      ResultActions response = mockMvc.perform(post("/members")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequestDto)));
      //then
      compareResponseWithSingleExpectedErrorDto(errorDto, response);

      then(memberService).shouldHaveNoInteractions();
    }

    private void compareResponseWithSingleExpectedErrorDto(ErrorDto errorDto, ResultActions response) throws Exception {
      response.andExpect(MockMvcResultMatchers.status().isBadRequest())
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].path", Matchers.is(errorDto.getPath())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.is(errorDto.getMessage())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].statusCode", Matchers.is(errorDto.getStatusCode())))
          .andDo(MockMvcResultHandlers.print());
    }
  }


}