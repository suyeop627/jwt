package com.study.securitywithjwt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.dto.ErrorDto;
import com.study.securitywithjwt.dto.LoginRequestDto;
import com.study.securitywithjwt.dto.LoginResponseDto;
import com.study.securitywithjwt.dto.RefreshTokenDto;
import com.study.securitywithjwt.exception.CustomAuthenticationEntryPoint;
import com.study.securitywithjwt.exception.JwtAuthenticationException;
import com.study.securitywithjwt.exception.JwtExceptionType;
import com.study.securitywithjwt.jwt.JwtAuthenticationProvider;
import com.study.securitywithjwt.service.AuthenticationService;
import com.study.securitywithjwt.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("classpath:application-test.properties")
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
  @Value("${jwt.exception.response.header}")
  private String JWT_EXCEPTION_HEADER;
  @MockBean
  private HttpServletRequest httpServletRequest;

  @Test
  public void login_validState_returnLoginResponseDto() throws Exception {
    // given
    LoginRequestDto loginRequestDto = new LoginRequestDto("test@example.com", "password");
    LoginResponseDto expectedLoginResponse = new LoginResponseDto("accessToken", "refreshToken", loginRequestDto.getEmail(), "testName");

    given(authenticationService.login(loginRequestDto)).willReturn(expectedLoginResponse);

    // When
    ResultActions response = mockMvc.perform(post("/auth")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequestDto)));
    //then
    response.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken", Matchers.is(expectedLoginResponse.getAccessToken())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken", Matchers.is(expectedLoginResponse.getRefreshToken())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is(expectedLoginResponse.getEmail())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(expectedLoginResponse.getName())))
        .andDo(MockMvcResultHandlers.print());
  }

  @Nested
  class LoginValidationTest {
    @Test
    public void login_invalidEmailAndInvalidPassword_return400ErrorDtos() throws Exception {
      // given
      String invalidEmail = "EMAIL";
      String invalidPassword = "pwd";
      LoginRequestDto loginRequestDto = new LoginRequestDto(invalidEmail, invalidPassword);
      LoginResponseDto loginResponseDto = new LoginResponseDto("accessToken", "refreshToken", loginRequestDto.getEmail(), "testName");
      given(authenticationService.login(loginRequestDto)).willReturn(loginResponseDto);

      List<ErrorDto> expectedErrorDto = Arrays.asList(
          new ErrorDto("POST /auth", "must be a well-formed email address", 400, LocalDateTime.now()),
          new ErrorDto("POST /auth", "password size must be between 8 and 16", 400, LocalDateTime.now())
      );

      // When
      ResultActions response = mockMvc.perform(post("/auth")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequestDto)));
      //then
      response.andExpect(MockMvcResultMatchers.status().isBadRequest())
          .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(expectedErrorDto.size())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].path", Matchers.containsInAnyOrder(expectedErrorDto.stream().map(ErrorDto::getPath).toArray())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].message", Matchers.containsInAnyOrder(expectedErrorDto.stream().map(ErrorDto::getMessage).toArray())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].statusCode", Matchers.containsInAnyOrder(expectedErrorDto.stream().map(ErrorDto::getStatusCode).toArray())))
          .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void login_invalidEmail_return400ErrorDto() throws Exception {
      // given
      String invalidEmail = "2";
      LoginRequestDto loginRequestDto = new LoginRequestDto(invalidEmail, "00000000");
      LoginResponseDto loginResponseDto = new LoginResponseDto("accessToken", "refreshToken", loginRequestDto.getEmail(), "testName");

      given(authenticationService.login(loginRequestDto)).willReturn(loginResponseDto);

      ErrorDto expectedErrorDto = new ErrorDto("POST /auth", "must be a well-formed email address", 400, LocalDateTime.now());

      // when
      ResultActions response = mockMvc.perform(post("/auth")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequestDto)));

      //then
      response.andExpect(MockMvcResultMatchers.status().isBadRequest())
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].path", Matchers.is(expectedErrorDto.getPath())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.is(expectedErrorDto.getMessage())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].statusCode", Matchers.is(expectedErrorDto.getStatusCode())))
          .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void login_passwordMoreThan16_return400ErrorDto() throws Exception {
      //given
      LoginRequestDto loginRequestDto = new LoginRequestDto("2123@test.com", "333333333333333333333333333333");
      LoginResponseDto loginResponseDto = new LoginResponseDto("accessToken", "refreshToken", loginRequestDto.getEmail(), "testName");
      given(authenticationService.login(loginRequestDto)).willReturn(loginResponseDto);

      ErrorDto expectedErrorDto = new ErrorDto("POST /auth", "password size must be between 8 and 16", HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());

      //when
      ResultActions response = mockMvc.perform(post("/auth")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequestDto)));

      //then
      response.andExpect(MockMvcResultMatchers.status().isBadRequest())
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].path", Matchers.is(expectedErrorDto.getPath())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.is(expectedErrorDto.getMessage())))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].statusCode", Matchers.is(expectedErrorDto.getStatusCode())))
          .andDo(MockMvcResultHandlers.print());
    }

  }


  @Nested
  class reIssueAccessToken {
    @Test
    void reIssueAccessToken_validState_returnLoginResponseDto() throws Exception {
      //given
      RefreshTokenDto refreshTokenDtoForRequest = new RefreshTokenDto();
      refreshTokenDtoForRequest.setRefreshToken("refreshToken_for_test");

      LoginResponseDto expectedLoginResponse = new LoginResponseDto("accessToken", "refreshToken", "test@test.com", "testName");
      RefreshToken savedRefreshToken = new RefreshToken();
      savedRefreshToken.setToken("token");

      given(refreshTokenService.selectRefreshTokenByTokenValue(anyString())).willReturn(Optional.of(savedRefreshToken));
      given(authenticationService.reAuthenticateWithRefreshToken(anyString())).willReturn(expectedLoginResponse);

      //when
      ResultActions response = mockMvc.perform(put("/auth")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(refreshTokenDtoForRequest)));

      //then
      response.andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken", Matchers.is(expectedLoginResponse.getAccessToken())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken", Matchers.is(expectedLoginResponse.getRefreshToken())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is(expectedLoginResponse.getEmail())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(expectedLoginResponse.getName())))
          .andDo(MockMvcResultHandlers.print());

      then(authenticationService).should(times(1)).reAuthenticateWithRefreshToken(anyString());
    }

    @Test
    void reIssueAccessToken_nonexistentToken_return404ErrorDto() throws Exception {
      //given
      RefreshTokenDto refreshTokenDtoForRequest = new RefreshTokenDto();
      refreshTokenDtoForRequest.setRefreshToken("refreshToken_for_test");

      given(refreshTokenService.selectRefreshTokenByTokenValue(anyString())).willReturn(Optional.empty());

      ErrorDto expectedErrorDto = new ErrorDto("PUT /auth", "The refresh token does not exist in the database.", HttpStatus.NOT_FOUND.value(), LocalDateTime.now());
      //when
      ResultActions response = mockMvc.perform(put("/auth")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(refreshTokenDtoForRequest)));

      //then
      response.andExpect(MockMvcResultMatchers.status().isNotFound())
          .andExpect(MockMvcResultMatchers.jsonPath("$.path", Matchers.is(expectedErrorDto.getPath())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is(expectedErrorDto.getMessage())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode", Matchers.is(expectedErrorDto.getStatusCode())))
          .andDo(MockMvcResultHandlers.print());

      then(authenticationService).should(times(0)).reAuthenticateWithRefreshToken(anyString());
    }

    @Test
    void reIssueAccessToken_refreshTokenExpired_throwJwtAuthenticationException() throws Exception {
      //given
      RefreshTokenDto refreshTokenDtoForRequest = new RefreshTokenDto();
      refreshTokenDtoForRequest.setRefreshToken("expired_refresh_token");
      given(refreshTokenService.selectRefreshTokenByTokenValue(anyString())).willReturn(Optional.of(new RefreshToken()));

      given(authenticationService.reAuthenticateWithRefreshToken(any()))
          .willThrow(new JwtAuthenticationException(JwtExceptionType.EXPIRED_REFRESH_TOKEN.getMessage(),
              JwtExceptionType.EXPIRED_REFRESH_TOKEN));

      ErrorDto expectedErrorDto = new ErrorDto("PUT /auth", JwtExceptionType.EXPIRED_REFRESH_TOKEN.getMessage(), HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());

      //when
      ResultActions response = mockMvc.perform(put("/auth")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(refreshTokenDtoForRequest)));

      //then
      response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
          .andExpect(MockMvcResultMatchers.jsonPath("$.path", Matchers.is(expectedErrorDto.getPath())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is(expectedErrorDto.getMessage())))
          .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode", Matchers.is(expectedErrorDto.getStatusCode())))
          .andExpect(MockMvcResultMatchers.header().exists(JWT_EXCEPTION_HEADER))
          .andExpect(MockMvcResultMatchers.header().string(JWT_EXCEPTION_HEADER, JwtExceptionType.EXPIRED_REFRESH_TOKEN.getStatus()))
          .andDo(MockMvcResultHandlers.print());
    }
  }

  @Nested
  class logout {
    @Test
    void logout_validState_deleteTokenInDB() throws Exception {
      //given
      String refreshToken = "refreshToken_deleted_when_logout_called";
      given(httpServletRequest.getHeader(anyString())).willReturn("Bearer " + refreshToken);

      //when
      ResultActions response = mockMvc.perform(delete("/auth")
          .header("Authorization", "Bearer "+refreshToken));

      //then
      response.andExpect(MockMvcResultMatchers.status().isOk())
          .andDo(MockMvcResultHandlers.print());
      then(refreshTokenService).should(times(1)).deleteRefreshTokenByToken(refreshToken);
    }
    @Test
    void logout_noTokenInHeader_deleteTokenInDB() throws Exception {
      //given
      given(httpServletRequest.getHeader(anyString())).willReturn(null);

      //when
      ResultActions response = mockMvc.perform(delete("/auth"));

      //then
      response.andExpect(MockMvcResultMatchers.status().isBadRequest())
          .andDo(MockMvcResultHandlers.print());
      then(refreshTokenService).should(times(0)).deleteRefreshTokenByToken(anyString());
    }
  }
}
