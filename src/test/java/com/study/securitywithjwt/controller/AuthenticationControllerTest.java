package com.study.securitywithjwt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.exception.CustomAuthenticationEntryPoint;
import com.study.securitywithjwt.exception.JwtAuthenticationException;
import com.study.securitywithjwt.exception.JwtExceptionType;
import com.study.securitywithjwt.jwt.JwtAuthenticationProvider;
import com.study.securitywithjwt.service.auth.AuthenticationService;
import com.study.securitywithjwt.service.refreshtoken.RefreshTokenService;
import com.study.securitywithjwt.utils.annotation.LoggedInUserInfoArgumentResolver;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTest {
  @Autowired
  MockMvc mockMvc;
  @MockBean
  private AuthenticationService authenticationService;

  @MockBean
  private JwtAuthenticationProvider jwtAuthenticationProvider;
  @MockBean
  private RefreshTokenService refreshTokenService;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @MockBean
  private LoggedInUserInfoArgumentResolver argumentResolver;

  @Test
  public void login_validState_returnLoginResponseDto() throws Exception {
    // Given
    LoginRequestDto loginRequestDto = new LoginRequestDto("test@example.com", "password");
    LoginResponseDto loginResponseDto = new LoginResponseDto("accessToken", "refreshToken", loginRequestDto.getEmail(), "testName");
    given(authenticationService.login(loginRequestDto)).willReturn(loginResponseDto);
   /* willAnswer - 전달받은 parameter로 특별한 처리 후 반환할 경우 사용
    ex -> BDDMockito.given(mockService.someMethod(BDDMockito.anyString(), BDDMockito.anyInt()))
        .willAnswer(invocation -> {
        String arg1 = invocation.getArgument(0);
            int arg2 = invocation.getArgument(1);

            // 여기에서 arg1, arg2를 사용하여 특별한 동작이나 로직을 정의할 수 있음

            return "Mocked Value";
        });
  */

    // When
    ResultActions response = mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequestDto)));
    //then
    response.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken", Matchers.is(loginResponseDto.getAccessToken())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken", Matchers.is(loginResponseDto.getRefreshToken())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is(loginResponseDto.getEmail())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(loginResponseDto.getName())))
        .andDo(MockMvcResultHandlers.print());
  }

  @Nested
  class LoginValidationTest {
    @Test
    public void login_invalidEmailAndInvalidPassword_return400ErrorDtos() throws Exception {
      // Given
      LoginRequestDto loginRequestDto = new LoginRequestDto("2", "1");
      LoginResponseDto loginResponseDto = new LoginResponseDto("accessToken", "refreshToken", loginRequestDto.getEmail(), "testName");
      given(authenticationService.login(loginRequestDto)).willReturn(loginResponseDto);

      List<ErrorDto> expectedErrors = Arrays.asList(
          new ErrorDto("/auth/login", "must be a well-formed email address", 400, LocalDateTime.now()),
          new ErrorDto("/auth/login", "password size must be between 8 and 16", 400, LocalDateTime.now())
      );


      // When
      ResultActions response = mockMvc.perform(post("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequestDto)));
      //then
      response.andExpect(MockMvcResultMatchers.status().isBadRequest())
          .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(expectedErrors.size())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].path", Matchers.containsInAnyOrder(expectedErrors.stream().map(ErrorDto::getPath).toArray())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].message", Matchers.containsInAnyOrder(expectedErrors.stream().map(ErrorDto::getMessage).toArray())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].statusCode", Matchers.containsInAnyOrder(expectedErrors.stream().map(ErrorDto::getStatusCode).toArray())))
          .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void login_invalidEmail_return400ErrorDto() throws Exception {
      // Given
      LoginRequestDto loginRequestDto = new LoginRequestDto("2", "00000000");
      LoginResponseDto loginResponseDto = new LoginResponseDto("accessToken", "refreshToken", loginRequestDto.getEmail(), "testName");
      given(authenticationService.login(loginRequestDto)).willReturn(loginResponseDto);


      ErrorDto errorDto = new ErrorDto("/auth/login", "must be a well-formed email address", 400, LocalDateTime.now());


      // When
      ResultActions response = mockMvc.perform(post("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequestDto)));
      //then
      response.andExpect(MockMvcResultMatchers.status().isBadRequest())
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].path", Matchers.is(errorDto.getPath())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.is(errorDto.getMessage())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].statusCode", Matchers.is(errorDto.getStatusCode())))
          .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void login_passwordMoreThan16_return400ErrorDto() throws Exception {
      // Given
      LoginRequestDto loginRequestDto = new LoginRequestDto("2123@test.com", "333333333333333333333333333333");
      LoginResponseDto loginResponseDto = new LoginResponseDto("accessToken", "refreshToken", loginRequestDto.getEmail(), "testName");
      given(authenticationService.login(loginRequestDto)).willReturn(loginResponseDto);

      ErrorDto errorDto = new ErrorDto("/auth/login", "password size must be between 8 and 16", HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());

      // When
      ResultActions response = mockMvc.perform(post("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequestDto)));
      //then
      response.andExpect(MockMvcResultMatchers.status().isBadRequest())
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].path", Matchers.is(errorDto.getPath())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.is(errorDto.getMessage())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].statusCode", Matchers.is(errorDto.getStatusCode())))
          .andDo(MockMvcResultHandlers.print());
    }

  }


  @Nested
  class reIssueAccessToken {
    @Test
    void reIssueAccessToken_validState_returnLoginResponseDto() throws Exception {
      //given
      RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
      refreshTokenDto.setToken("refreshToken_for_test");
      LoginResponseDto loginResponseDto = new LoginResponseDto("accessToken", "refreshToken", "test@test.com", "testName");
      RefreshToken refreshToken = new RefreshToken();
      refreshToken.setToken("token");
      given(authenticationService.selectRefreshToken(anyString())).willReturn(Optional.of(refreshToken));
      given(authenticationService.reIssueAccessToken(anyString())).willReturn(loginResponseDto);

      //when
      ResultActions response = mockMvc.perform(post("/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(refreshTokenDto)));

      //then
      response.andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken", Matchers.is(loginResponseDto.getAccessToken())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken", Matchers.is(loginResponseDto.getRefreshToken())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is(loginResponseDto.getEmail())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(loginResponseDto.getName())))
          .andDo(MockMvcResultHandlers.print());

      then(authenticationService).should(times(1)).reIssueAccessToken(anyString());
    }

    @Test
    void reIssueAccessToken_nonexistentToken_return404ErrorDto() throws Exception {
      //given
      RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
      refreshTokenDto.setToken("refreshToken_for_test");

      given(authenticationService.selectRefreshToken(anyString())).willReturn(Optional.empty());

      ErrorDto errorDto = new ErrorDto("/auth/refresh", "token doesn't exist in database", HttpStatus.NOT_FOUND.value(), LocalDateTime.now());
      //when
      ResultActions response = mockMvc.perform(post("/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(refreshTokenDto)));

      //then
      response.andExpect(MockMvcResultMatchers.status().isNotFound())
          .andExpect(MockMvcResultMatchers.jsonPath("$.path", Matchers.is(errorDto.getPath())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is(errorDto.getMessage())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode", Matchers.is(errorDto.getStatusCode())))
          .andDo(MockMvcResultHandlers.print());

      then(authenticationService).should(times(0)).reIssueAccessToken(anyString());
    }

    @Test
    void reIssueAccessToken_refreshTokenExpired_throwJwtAuthenticationException() throws Exception {
      //given
      RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
      refreshTokenDto.setToken("expired_refresh_token");
      given(authenticationService.selectRefreshToken(anyString()))
          .willReturn(Optional.of(new RefreshToken()));

      given(authenticationService.reIssueAccessToken(any()))
          .willThrow(new JwtAuthenticationException(JwtExceptionType.EXPIRED_REFRESH_TOKEN.getMessage(),
              JwtExceptionType.EXPIRED_REFRESH_TOKEN));

      ErrorDto errorDto = new ErrorDto("/auth/refresh", JwtExceptionType.EXPIRED_REFRESH_TOKEN.getMessage(), HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());
      //when
      ResultActions response = mockMvc.perform(post("/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(refreshTokenDto)));

      //then
      response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
          .andExpect(MockMvcResultMatchers.jsonPath("$.path", Matchers.is(errorDto.getPath())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is(errorDto.getMessage())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode", Matchers.is(errorDto.getStatusCode())))
          .andExpect(MockMvcResultMatchers.header().exists("JwtException"))
          .andExpect(MockMvcResultMatchers.header().string("JwtException", JwtExceptionType.EXPIRED_REFRESH_TOKEN.getCode()))
          .andDo(MockMvcResultHandlers.print());

    }

  }

  @Nested
  class logout {
    @Test
    void logout_validState_deleteTokenInDB() throws Exception {
      //given
      MemberInfo loggedInMember = new MemberInfo();
      loggedInMember.setMemberId(1L);
      given(argumentResolver.supportsParameter(any())).willReturn(true);
      given(argumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(loggedInMember);

      //when
      ResultActions response = mockMvc.perform(delete("/auth/logout")
          .header("authorization", "token"));
      //then
      response.andExpect(MockMvcResultMatchers.status().isOk())
          .andDo(MockMvcResultHandlers.print());
      then(refreshTokenService).should(times(1)).deleteRefreshTokenByMemberId(loggedInMember.getMemberId());
    }

    @Test
    void logout_noToken_throwBadRequestException() throws Exception {
      //given
      given(argumentResolver.supportsParameter(any())).willReturn(true);
      given(argumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(null);

      //when
      ResultActions response = mockMvc.perform(delete("/auth/logout"));
      //then
      response.andExpect(MockMvcResultMatchers.status().isBadRequest())
          .andDo(MockMvcResultHandlers.print());
      then(refreshTokenService).shouldHaveNoInteractions();
    }
  }

}
