package com.study.securitywithjwt.journey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.exception.JwtExceptionType;
import com.study.securitywithjwt.jwt.JwtUtils;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RefreshTokenRepository;
import com.study.securitywithjwt.utils.member.Gender;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:/application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
public class AuthenticationIT {
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  RefreshTokenRepository refreshTokenRepository;
  @Autowired
  MemberRepository memberRepository;
  @AfterEach
  void tearDown() {
    memberRepository.deleteAll();
    refreshTokenRepository.deleteAll();
  }
  @Test
  void loginAndSignupIT() {
    String validEmail = "test@test.com";
    String validPassword = "00000000";
    String validPhone = "01011111111";
    String validName = "name";
    LoginRequestDto loginRequestDto = new LoginRequestDto(validEmail, validPassword);

    MemberSignupRequestDto signupRequestDto = new MemberSignupRequestDto();
    signupRequestDto.setEmail(validEmail);
    signupRequestDto.setName(validName);
    signupRequestDto.setPassword(validPassword);
    signupRequestDto.setGender(Gender.MALE);
    signupRequestDto.setPhone(validPhone);


    //before signup - login fail
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isUnauthorized();

    //sign up - email malformed
    signupRequestDto.setEmail("test");

    postSignUpRequestWIthSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();
    signupRequestDto.setEmail(validEmail);

    //sign up - password malformed
    signupRequestDto.setPassword("000");
    postSignUpRequestWIthSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();
    signupRequestDto.setPassword(validPassword);

    //sign up - name malformed
    signupRequestDto.setName("d");
    postSignUpRequestWIthSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();
    signupRequestDto.setName(validName);

    //sign up - phone malformed
    signupRequestDto.setPhone("11111111111");
    postSignUpRequestWIthSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();
    signupRequestDto.setPhone(validPhone);

    //sign up - ok
    MemberSignupResponseDto signupResponseDto = MemberSignupResponseDto.builder()
        .email(signupRequestDto.getEmail()).name(signupRequestDto.getName()).build();
    EntityExchangeResult<MemberSignupResponseDto> responseEntityExchangeResult = postSignUpRequestWIthSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isCreated().expectBody(MemberSignupResponseDto.class)
        .returnResult();

    //compare with request dto  - response dto
    assertThat(responseEntityExchangeResult.getResponseBody())
        .isNotNull()
        .extracting(MemberSignupResponseDto::getName)
        .isNotNull()
        .isEqualTo(signupResponseDto.getName());
    assertThat(responseEntityExchangeResult.getResponseBody())
        .isNotNull()
        .extracting(MemberSignupResponseDto::getEmail)
        .isNotNull()
        .isEqualTo(signupRequestDto.getEmail());

    //login - email malformed
    loginRequestDto.setEmail("test");
    loginRequestDto.setPassword("00000000");
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isBadRequest();

    //login - password malformed
    loginRequestDto.setEmail("test@test.com");
    loginRequestDto.setPassword("00000000000000000000000000000000");
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isBadRequest();

    //login - password wrong
    loginRequestDto.setEmail("test@test.com");
    loginRequestDto.setPassword("11111111");
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isUnauthorized();

    //login - un saved member
    loginRequestDto.setEmail("test123@test.com");
    loginRequestDto.setPassword("00000000");
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isUnauthorized();

    //login - ok
    loginRequestDto.setEmail("test@test.com");
    loginRequestDto.setPassword("00000000");
    EntityExchangeResult<LoginResponseDto> loginResponseEntityExchangeResult = postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isOk()
        .expectBody(LoginResponseDto.class)
        .returnResult();

    assertThat(loginResponseEntityExchangeResult.getResponseBody())
        .isNotNull()
        .extracting(LoginResponseDto::getEmail)
        .isEqualTo(loginRequestDto.getEmail());

    String refreshToken = loginResponseEntityExchangeResult.getResponseBody().getRefreshToken();
    String accessToken = loginResponseEntityExchangeResult.getResponseBody().getAccessToken();

    Claims claimsFromAccessToken = jwtUtils.getClaimsFromAccessToken(accessToken);
    assertThat(claimsFromAccessToken.getSubject()).isEqualTo(loginRequestDto.getEmail());
    assertThat(claimsFromAccessToken.get("name")).isEqualTo(signupRequestDto.getName());
    assertThat(claimsFromAccessToken.getExpiration()).isAfter(new Date());
    assertThat(claimsFromAccessToken.getIssuedAt()).isBefore(new Date());
    assertThat(claimsFromAccessToken.get("roles")).isEqualTo(List.of("ROLE_USER"));


    Claims claimsFromRefreshToken = jwtUtils.getClaimsFromRefreshToken(refreshToken);
    assertThat(claimsFromRefreshToken.getSubject()).isEqualTo(loginRequestDto.getEmail());
    assertThat(claimsFromRefreshToken.get("name")).isEqualTo(signupRequestDto.getName());

    long delta = 60 * 1000L;
    Date REFRESH_TOKEN_EXPIRATION = new Date(new Date().getTime() + (7 * 24 * 60 * 60 * 1000L));

    assertThat(claimsFromRefreshToken.getExpiration()).isCloseTo(REFRESH_TOKEN_EXPIRATION, delta);

    assertThat(claimsFromRefreshToken.getIssuedAt()).isBefore(new Date());
    assertThat(claimsFromRefreshToken.get("roles")).isEqualTo(List.of("ROLE_USER"));

  }

  private WebTestClient.ResponseSpec postSignUpRequestWIthSignupRequestDto(MemberSignupRequestDto signupRequestDto) {
    return webTestClient.post()
        .uri("/members")
        .bodyValue(signupRequestDto)
        .exchange();
  }

  private WebTestClient.ResponseSpec postLoginRequestWithLoginRequestDto(LoginRequestDto loginRequestDto) {
    return webTestClient.post()
        .uri("/auth/login")
        .bodyValue(loginRequestDto)
        .exchange();
  }

  @Nested
  class JwtReIssue {
    MemberSignupRequestDto signupRequestDto;

    @BeforeEach
    void setUP() {
      signupRequestDto = new MemberSignupRequestDto();
      signupRequestDto.setEmail("testEmail@email.com");
      signupRequestDto.setPassword("00000000");
      signupRequestDto.setName("testName");
      signupRequestDto.setGender(Gender.MALE);
      signupRequestDto.setPhone("01000000000");


      webTestClient.post().uri("/members")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(signupRequestDto)
          .exchange()
          .expectStatus().isCreated();
    }



    @Test
    void reIssueAccessToken() {
      ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_DURATION", -1000L);
      //issue access token(expired) & refresh token

      LoginRequestDto loginRequestDto = new LoginRequestDto(signupRequestDto.getEmail(), signupRequestDto.getPassword());

      EntityExchangeResult<LoginResponseDto> loginResponseDtoEntityExchangeResult =
          webTestClient.post()
              .uri("/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(loginRequestDto)
              .exchange()
              .expectStatus().isOk()
              .expectBody(LoginResponseDto.class)
              .returnResult();

      assertThat(loginResponseDtoEntityExchangeResult.getResponseBody()).isNotNull();
      String accessTokenExpired = loginResponseDtoEntityExchangeResult.getResponseBody().getAccessToken();
      String refreshToken = loginResponseDtoEntityExchangeResult.getResponseBody().getRefreshToken();

      log.info("reissued accessToken (expired) : {}", accessTokenExpired);
      log.info("reissued refreshToken (expired) : {}", refreshToken);
      log.info("request post \"/api\"");
      log.info("refreshTokenRepository count {} after request /login ", refreshTokenRepository.count());

      //when request for api ->then  throw expired token exception
      EntityExchangeResult<ErrorDto> entityExchangeResult = webTestClient.post()
          .uri("/api")
          .header("Authorization", String.format("Bearer %s", accessTokenExpired))
          .exchange()
          .expectStatus()
          .isUnauthorized()
          .expectHeader()
          .valueEquals("JwtException", JwtExceptionType.EXPIRED_ACCESS_TOKEN.getCode())
          .expectBody(ErrorDto.class)
          .returnResult();

      assertThat(entityExchangeResult.getResponseBody()).isNotNull()
          .extracting(ErrorDto::getMessage).isEqualTo(JwtExceptionType.EXPIRED_ACCESS_TOKEN.getMessage());

      RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
      refreshTokenDto.setToken(refreshToken);

      ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_DURATION", 60 * 60 * 1000L);


      //request with body contains refresh token ->   reIssue access token
      EntityExchangeResult<LoginResponseDto> loginResponseDtoEntityExchangeResultAfterRefresh =
          webTestClient.post().uri("/auth/refresh")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(refreshTokenDto)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(LoginResponseDto.class)
              .returnResult();
      assertThat(loginResponseDtoEntityExchangeResultAfterRefresh.getResponseBody()).isNotNull();
      assertThat(loginResponseDtoEntityExchangeResultAfterRefresh.getResponseBody().getAccessToken()).isNotEqualTo(accessTokenExpired);
      assertThat(loginResponseDtoEntityExchangeResultAfterRefresh.getResponseBody().getRefreshToken()).isEqualTo(refreshToken);



      log.info("refreshTokenRepository count {} after request /refresh ", refreshTokenRepository.count());
      assertThat(refreshTokenRepository.count() == 1).isTrue();

      //request for api with access token -> ok

      String reIssuedAccessTokenWithBearer = String.format("Bearer %s", loginResponseDtoEntityExchangeResultAfterRefresh.getResponseBody().getAccessToken());
      System.out.println("reIssuedAccessTokenWithBearer = " + reIssuedAccessTokenWithBearer);

      webTestClient.post().uri("/api")
          .header("Authorization", reIssuedAccessTokenWithBearer)
          .exchange()
          .expectStatus()
          .isOk();
      webTestClient.delete().uri("/auth/logout")
          .header("Authorization", reIssuedAccessTokenWithBearer)
          .exchange()
          .expectStatus()
          .isOk();

      assertThat(refreshTokenRepository.count() == 0).isTrue();
      log.info("refreshTokenRepository count {} after request /logout ", refreshTokenRepository.count());
    }

    @Test
    void reIssueRefreshToken() {
      ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_DURATION", -1000L);
      ReflectionTestUtils.setField(jwtUtils, "REFRESH_TOKEN_DURATION", -1000L);
      //login -> issue access token(expired) & refresh token(expired)
      LoginRequestDto loginRequestDto = new LoginRequestDto(signupRequestDto.getEmail(), signupRequestDto.getPassword());

      EntityExchangeResult<LoginResponseDto> loginResponseDtoEntityExchangeResult =
          webTestClient.post().uri("/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(loginRequestDto)
              .exchange()
              .expectStatus().isOk()
              .expectBody(LoginResponseDto.class)
              .returnResult();

      assertThat(loginResponseDtoEntityExchangeResult.getResponseBody()).isNotNull();
      assertThat(refreshTokenRepository.count() == 1).isTrue();
      String accessTokenExpired = loginResponseDtoEntityExchangeResult.getResponseBody().getAccessToken();
      String refreshTokenExpired = loginResponseDtoEntityExchangeResult.getResponseBody().getRefreshToken();


      //when request for api -> throw expired token exception
      webTestClient.post()
          .uri("/api")
          .header("Authorization", "Bearer " + accessTokenExpired)
          .exchange()
          .expectStatus()
          .isUnauthorized()
          .expectHeader()
          .valueEquals("jwtException", JwtExceptionType.EXPIRED_ACCESS_TOKEN.getCode())
          .expectBody(ErrorDto.class);


      //request with refresh token in body -> logout ->  throw expired token exception
      RefreshTokenDto expiredRefreshTokenDto = new RefreshTokenDto();
      expiredRefreshTokenDto.setToken(refreshTokenExpired);
      EntityExchangeResult<ErrorDto> entityExchangeResultWhenRefreshTokenExpired = webTestClient.post()
          .uri("/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(expiredRefreshTokenDto)
          .exchange()
          .expectStatus()
          .isUnauthorized()
          .expectHeader()
          .valueEquals("JwtException", JwtExceptionType.EXPIRED_REFRESH_TOKEN.getCode())
          .expectBody(ErrorDto.class)
          .returnResult();


      System.out.println("entityExchangeResult1.getResponseBody() = " + entityExchangeResultWhenRefreshTokenExpired.getResponseBody());

      assertThat(refreshTokenRepository.count() == 0).isTrue();
      assertThat(entityExchangeResultWhenRefreshTokenExpired.getResponseBody()).isNotNull();
      assertThat(entityExchangeResultWhenRefreshTokenExpired.getResponseBody().getMessage()).isEqualTo(JwtExceptionType.EXPIRED_REFRESH_TOKEN.getMessage());


      //login ->issue access token & refresh token valid

      ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_DURATION", 60 * 60 * 1000L);
      ReflectionTestUtils.setField(jwtUtils, "REFRESH_TOKEN_DURATION", 60 * 60 * 1000L);

      EntityExchangeResult<LoginResponseDto> loginResponseDtoEntityExchangeResultWithValidTokens = webTestClient.post()
          .uri("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(loginRequestDto)
          .exchange().expectStatus()
          .isOk()
          .expectBody(LoginResponseDto.class).returnResult();

      assertThat(loginResponseDtoEntityExchangeResultWithValidTokens.getResponseBody()).isNotNull();
      String validAccessToken = loginResponseDtoEntityExchangeResultWithValidTokens.getResponseBody().getAccessToken();
      String validRefreshToken = loginResponseDtoEntityExchangeResultWithValidTokens.getResponseBody().getRefreshToken();


      //request for api with access token -> ok

      webTestClient.post()
          .uri("/api")
          .header("Authorization", String.format("Bearer %s", validAccessToken))
          .exchange()
          .expectStatus().isOk();

      assertThat(loginResponseDtoEntityExchangeResultWithValidTokens.getResponseBody()).isNotNull();

      webTestClient.delete().uri("/auth/logout")
          .header("Authorization", String.format("Bearer %s", validAccessToken))
          .exchange()
          .expectStatus()
          .isOk();
      assertThat(refreshTokenRepository.count() == 0).isTrue();
    }
  }
}
