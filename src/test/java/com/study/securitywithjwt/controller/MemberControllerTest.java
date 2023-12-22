package com.study.securitywithjwt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.dto.ErrorDto;
import com.study.securitywithjwt.dto.MemberDto;
import com.study.securitywithjwt.dto.MemberSignupRequestDto;
import com.study.securitywithjwt.dto.MemberSignupResponseDto;
import com.study.securitywithjwt.exception.CustomAuthenticationEntryPoint;
import com.study.securitywithjwt.exception.ResourceNotFoundException;
import com.study.securitywithjwt.jwt.JwtAuthenticationProvider;
import com.study.securitywithjwt.service.MemberService;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(MemberController.class)
@TestPropertySource("classpath:application-test.properties")
class MemberControllerTest {
  @MockBean
  MemberService memberService;

  @MockBean
  JwtAuthenticationProvider jwtAuthenticationProvider;

  @MockBean
  PasswordEncoder passwordEncoder;

  @MockBean
  CustomAuthenticationEntryPoint authenticationEntryPoint;

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
    memberSignupRequestDto.setPhone("01011111111");

    MemberSignupResponseDto expectedMemberSignupResponseDto = MemberSignupResponseDto.builder()
        .memberId(1L)
        .email(memberSignupRequestDto.getEmail())
        .regdate(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))//소수점 이하 자리수 문제 방지하기 위해서 자릿수 조절.
        .name(memberSignupRequestDto.getName())
        .build();

    given(memberService.addMember(any())).willReturn(expectedMemberSignupResponseDto);
    //when
    ResultActions response = mockMvc.perform(post("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(memberSignupRequestDto)));

    //then
    response.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.header().string("location", Matchers.endsWith("members/" + expectedMemberSignupResponseDto.getMemberId())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.memberId").value(Matchers.is(expectedMemberSignupResponseDto.getMemberId()), Long.class))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is(expectedMemberSignupResponseDto.getEmail())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(expectedMemberSignupResponseDto.getName())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.regdate", Matchers.is(expectedMemberSignupResponseDto.getRegdate().toString()+":00")))
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
      signupRequestDto.setPhone("01000000000");
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
      signupRequestDto.setPhone("01000000000");
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
      signupRequestDto.setPhone("01000000000");
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
      signupRequestDto.setPhone("01000000000");
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


    @Test
    public void signup_invalidPhone_return400errorDto() throws Exception {
      // Given
      MemberSignupRequestDto signupRequestDto = new MemberSignupRequestDto();
      signupRequestDto.setPassword("323333123");
      signupRequestDto.setPhone("11000000000");
      signupRequestDto.setEmail("test@test.com");
      signupRequestDto.setGender(Gender.MALE);
      signupRequestDto.setName("testName");

      ErrorDto errorDto = new ErrorDto("/members", "must be well-formed phone number", HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());

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

//todo memberController getAllmember 테스트
  //todo member crud
  //todo customer crud
  @Test
  public void getMember_validState_returnMemberDto() throws Exception {
    //given
    Long memberId = 1L;
    String email = "member1@test.com";
    String phone = "0102222222";
    String name = "member1";
    MemberDto memberDto = MemberDto.builder()
        .memberId(memberId)
        .email(email)
        .gender(Gender.FEMALE)
        .name(name)
        .roles(Set.of(UserRole.ROLE_USER.name()))
        .phone(phone)
        .build();
    given(memberService.getMember(anyLong())).willReturn(memberDto);
    //when
    ResultActions response = mockMvc.perform(get("/members/" + memberId));

    //then
    response.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.memberId", Matchers.is(memberId.intValue())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(name)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.phone", Matchers.is(phone)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is(email)));

  }
  @Test
  public void getMember_memberNonexistent_returnMemberDto() throws Exception {
    //given
    Long memberId = 1L;
    given(memberService.getMember(anyLong())).willThrow(new ResourceNotFoundException(String.format("member id %s is not found", memberId)));
    //when
    ResultActions response = mockMvc.perform(get("/members/" + memberId));

    //then
    response.andExpect(MockMvcResultMatchers.status().isNotFound());
    assertThatThrownBy(()->memberService.getMember(memberId)).isInstanceOf(ResourceNotFoundException.class);
  }
  @Test
  public void getAllMembers_validState_returnPageOfMemberDto() throws Exception {
    //given
    int totalElement = 30;
    int pageSize = 10;
    int pageNumber = 1;

    List<MemberDto> pageContents = new ArrayList<>();
    for (int i = 0; i < pageSize; i++) {
      MemberDto member = MemberDto.builder()
          .memberId((long) i)
          .phone(String.format("010000000%d", i))
          .roles(Set.of(UserRole.ROLE_USER.name()))
          .gender(Gender.MALE)
          .email(String.format("member%d@test.com", i))
          .name("name" + i)
          .build();
      pageContents.add(member);

    }
    PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc("memberId")));
    Page<MemberDto> memberDtoPage = new PageImpl<>(pageContents,pageRequest,totalElement);

    given(memberService.getAllMembers(anyInt(), anyInt())).willReturn(memberDtoPage);

    // When
    ResultActions response = mockMvc
        .perform(get("/members")
            .param("page", String.valueOf(pageNumber))
            .param("size", String.valueOf(pageSize)))
        .andExpect(MockMvcResultMatchers.status().isOk());

    //then
    System.out.println(response);
    response
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].memberId", Matchers.is(0)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[9].memberId", Matchers.is(9)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.size", Matchers.is(pageSize)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.number", Matchers.is(pageNumber)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(totalElement)));
  }
}
