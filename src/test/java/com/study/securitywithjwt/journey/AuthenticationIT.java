package com.study.securitywithjwt.journey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.exception.JwtExceptionType;
import com.study.securitywithjwt.jwt.JwtUtils;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RefreshTokenRepository;
import com.study.securitywithjwt.service.RefreshTokenService;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
  RefreshTokenService refreshTokenService;
  @Autowired
  MemberRepository memberRepository;
  @Value("${jwt.exception.response.header}")
  private String JWT_EXCEPTION_HEADER;
  @AfterEach
  void tearDown() {
    memberRepository.deleteAll();
    refreshTokenRepository.deleteAll();
  }

  @Test
  void loginAndSignupIT() {
    String validEmail = "test@test.com";
    String validPassword = "00000000";
    String validPhone = "01009472622";
    String validName = "name";
    LoginRequestDto loginRequestDto = new LoginRequestDto(validEmail, validPassword);

    MemberSignupRequestDto signupRequestDto = new MemberSignupRequestDto();
    signupRequestDto.setEmail(validEmail);
    signupRequestDto.setName(validName);
    signupRequestDto.setPassword(validPassword);
    signupRequestDto.setGender(Gender.MALE);
    signupRequestDto.setPhone(validPhone);
    System.out.println("signupRequestDto = " + signupRequestDto);

    //signup - beforeSignup - login fail(401)
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isUnauthorized();

    //signup - email malformed -> sign up fail(400)
    signupRequestDto.setEmail("test");

    postSignUpRequestWithSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();
    signupRequestDto.setEmail(validEmail);

    //signup - password less then 8 - sign up fail (400)
    signupRequestDto.setPassword("000");
    postSignUpRequestWithSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();
    signupRequestDto.setPassword(validPassword);

    //signup - password more then 16 - sign up fail (400)
    signupRequestDto.setPassword("00000000000000000000");
    postSignUpRequestWithSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();
    signupRequestDto.setPassword(validPassword);


    //signup - name less then 2 - sign up fail (400)
    signupRequestDto.setName("d");
    postSignUpRequestWithSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();
    signupRequestDto.setName(validName);

    //signup - name more than 16 - sign up fail (400)
    signupRequestDto.setName("testNameTestNameTestNameTestNameTestNameTestName");
    postSignUpRequestWithSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();
    signupRequestDto.setName(validName);

    //signup - phone malformed - sign up fail (400)
    signupRequestDto.setPhone("11111111111");
    postSignUpRequestWithSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();
    signupRequestDto.setPhone(validPhone);

    //signup - all validation ok and no roles - sign up success(201)
    MemberSignupResponseDto signupResponseDto = MemberSignupResponseDto.builder()
        .email(signupRequestDto.getEmail()).name(signupRequestDto.getName()).build();
    MemberSignupResponseDto responseDto = postSignUpRequestWithSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isCreated().expectBody(MemberSignupResponseDto.class)
        .returnResult()
        .getResponseBody();

    //signup - compare signup request dto with response dto
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getName())
        .isNotNull()
        .isEqualTo(signupResponseDto.getName());
    assertThat(responseDto)
        .isNotNull()
        .extracting(MemberSignupResponseDto::getEmail)
        .isNotNull()
        .isEqualTo(signupRequestDto.getEmail());



    //signup - all validation ok and multiple roles - sign up success(201)
    MemberSignupRequestDto signupRequestDtoWithRoles = new MemberSignupRequestDto();
    signupRequestDtoWithRoles.setEmail("rolesTest@Test.com");
    signupRequestDtoWithRoles.setName(validName);
    signupRequestDtoWithRoles.setPassword(validPassword);
    signupRequestDtoWithRoles.setGender(Gender.MALE);
    signupRequestDtoWithRoles.setPhone("01099887766");
    signupRequestDtoWithRoles.setRoles(Set.of(UserRole.ROLE_ADMIN, UserRole.ROLE_MANAGER));

    System.out.println("signupRequestDto = " + signupRequestDtoWithRoles);

    MemberSignupResponseDto responseDtoWithRoles = postSignUpRequestWithSignupRequestDto(signupRequestDtoWithRoles)
        .expectStatus()
        .isCreated().expectBody(MemberSignupResponseDto.class)
        .returnResult()
        .getResponseBody();

    //signup - compare signup request dto with response dto

    Member savedMemberWithRoles = memberRepository.findById(responseDtoWithRoles.getMemberId()).get();
    assertThat(savedMemberWithRoles.getRoles().size() == 2).isTrue();
    assertThat(savedMemberWithRoles.getRoles().stream().filter(role->role.getName()==UserRole.ROLE_ADMIN).count()).isEqualTo(1);
    assertThat(savedMemberWithRoles.getRoles().stream().filter(role->role.getName()==UserRole.ROLE_MANAGER).count()).isEqualTo(1);

    //login - email malformed - login fail (400)
    loginRequestDto.setEmail("test");
    loginRequestDto.setPassword("00000000");
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isBadRequest();

    //login - password more than 16 - login fail (400)
    loginRequestDto.setEmail("test@test.com");
    loginRequestDto.setPassword("00000000000000000000000000000000");
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isBadRequest();

    //login - password doesn't match - login fail (401)
    loginRequestDto.setEmail("test@test.com");
    loginRequestDto.setPassword("11111111");
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isUnauthorized();

    //login - un saved member - login fail(401)
    loginRequestDto.setEmail("test123@test.com");
    loginRequestDto.setPassword("00000000");
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isUnauthorized();

    //login - valid state - login success (200)
    loginRequestDto.setEmail("test@test.com");
    loginRequestDto.setPassword("00000000");
    EntityExchangeResult<LoginResponseDto> loginResponseEntityExchangeResult = postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isOk()
        .expectBody(LoginResponseDto.class)
        .returnResult();

    //compare with login request dto with response
    assertThat(loginResponseEntityExchangeResult.getResponseBody())
        .isNotNull()
        .extracting(LoginResponseDto::getEmail)
        .isEqualTo(loginRequestDto.getEmail());

    //parsing access token in response, and compare with member information
    String accessToken = loginResponseEntityExchangeResult.getResponseBody().getAccessToken();

    Claims claimsFromAccessToken = jwtUtils.extractClaimsFromAccessToken(accessToken);
    assertThat(claimsFromAccessToken.getSubject()).isEqualTo(loginRequestDto.getEmail());
    assertThat(claimsFromAccessToken.get("name")).isEqualTo(signupRequestDto.getName());
    assertThat(claimsFromAccessToken.getExpiration()).isAfter(new Date());
    assertThat(claimsFromAccessToken.getIssuedAt()).isBefore(new Date());
    assertThat(claimsFromAccessToken.get("roles")).isEqualTo(List.of("ROLE_USER"));

    //parsing refresh token in response, and compare with member information
    String refreshToken = loginResponseEntityExchangeResult.getResponseBody().getRefreshToken();
    Claims claimsFromRefreshToken = jwtUtils.extractClaimsFromRefreshToken(refreshToken);
    assertThat(claimsFromRefreshToken.getSubject()).isEqualTo(loginRequestDto.getEmail());
    assertThat(claimsFromRefreshToken.get("name")).isEqualTo(signupRequestDto.getName());

    //compare expiration date of refresh token  with a margin of error of 1 minute
    long delta = 60 * 1000L;
    Date REFRESH_TOKEN_EXPIRATION = new Date(new Date().getTime() + (7 * 24 * 60 * 60 * 1000L));

    assertThat(claimsFromRefreshToken.getExpiration()).isCloseTo(REFRESH_TOKEN_EXPIRATION, delta);
    assertThat(claimsFromRefreshToken.getIssuedAt()).isBefore(new Date());
    assertThat(claimsFromRefreshToken.get("roles")).isEqualTo(List.of("ROLE_USER"));

    //logout - no access token - 404
    webTestClient.delete().uri("/auth")
        .exchange()
        .expectStatus()
        .isBadRequest();

    //logout - valid refresh token - 200
    webTestClient.delete().uri("/auth")
        .header("Authorization", String.format("Bearer %s", refreshToken))
        .exchange()
        .expectStatus()
        .isOk();

  }

  private WebTestClient.ResponseSpec postSignUpRequestWithSignupRequestDto(MemberSignupRequestDto signupRequestDto) {
    return webTestClient.post()
        .uri("/members")
        .bodyValue(signupRequestDto)
        .exchange();
  }

  private WebTestClient.ResponseSpec postLoginRequestWithLoginRequestDto(LoginRequestDto loginRequestDto) {
    return webTestClient.post()
        .uri("/auth")
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
          .expectStatus()
          .isCreated();
    }


    @Test
    void reIssueAccessToken() {

      //issue access token(expired) & refresh token
      LoginRequestDto loginRequestDto = new LoginRequestDto(signupRequestDto.getEmail(), signupRequestDto.getPassword());

      //set access token duration to generate expired token
      ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_DURATION", -1000L);

      LoginResponseDto loginResponseWithExpiredAccessToken =
          webTestClient.post()
              .uri("/auth")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(loginRequestDto)
              .exchange()
              .expectStatus().isOk()
              .expectBody(LoginResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(loginResponseWithExpiredAccessToken).isNotNull();
      String accessTokenExpired = loginResponseWithExpiredAccessToken.getAccessToken();
      String refreshToken = loginResponseWithExpiredAccessToken.getRefreshToken();

      //getMember - expired access token  - getMember fail (201), response header has expired access token code
      ErrorDto errorDtoResponseForExpiredAccessToken = webTestClient.get()
          .uri("/members")
          .header("Authorization", String.format("Bearer %s", accessTokenExpired))
          .exchange()
          .expectStatus()
          .isUnauthorized()
          .expectHeader()
          .valueEquals(JWT_EXCEPTION_HEADER, JwtExceptionType.EXPIRED_ACCESS_TOKEN.getStatus())
          .expectBody(ErrorDto.class)
          .returnResult()
          .getResponseBody();

      assertThat(errorDtoResponseForExpiredAccessToken).isNotNull()
          .extracting(ErrorDto::getMessage)
          .isEqualTo(JwtExceptionType.EXPIRED_ACCESS_TOKEN.getMessage());


      RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
      refreshTokenDto.setRefreshToken(refreshToken);

      //restore access token duration
      ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_DURATION", 5 * 60 * 1000L);

      //refresh access token - request with refresh token in http body -> reIssue access token
      LoginResponseDto loginResponseAfterRefresh =
          webTestClient.put().uri("/auth")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(refreshTokenDto)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(LoginResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(loginResponseAfterRefresh).isNotNull();
      assertThat(loginResponseAfterRefresh.getAccessToken()).isNotEqualTo(accessTokenExpired);
      assertThat(loginResponseAfterRefresh.getRefreshToken()).isEqualTo(refreshToken);


      log.info("refreshTokenRepository count {} after request /refresh ", refreshTokenRepository.count());
      assertThat(refreshTokenRepository.count() == 1).isTrue();

      //getMember request with valid access token -> ok
      String reIssuedAccessTokenWithBearer = String.format("Bearer %s", loginResponseAfterRefresh.getAccessToken());

      webTestClient.get().uri("/members")
          .header("Authorization", reIssuedAccessTokenWithBearer)
          .exchange()
          .expectStatus()
          .isOk();

      //logout - valid state - 200
      webTestClient.delete().uri("/auth")
          .header("Authorization", String.format("Bearer %s", refreshToken))
          .exchange()
          .expectStatus()
          .isOk();

      assertThat(refreshTokenRepository.count() == 0).isTrue();
    }

    @Test
    void reIssueRefreshToken() {
      //set access token & refresh token duration to generate expired token
      ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_DURATION", -1000L);
      ReflectionTestUtils.setField(jwtUtils, "REFRESH_TOKEN_DURATION", -1000L);

      //login -> issue access token(expired) & refresh token(expired)
      LoginRequestDto loginRequestDto = new LoginRequestDto(signupRequestDto.getEmail(), signupRequestDto.getPassword());

      LoginResponseDto loginResponseWithExpiredTokens =
          webTestClient.post().uri("/auth")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(loginRequestDto)
              .exchange()
              .expectStatus().isOk()
              .expectBody(LoginResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(loginResponseWithExpiredTokens).isNotNull();
      assertThat(refreshTokenRepository.count() == 1).isTrue();
      String accessTokenExpired = loginResponseWithExpiredTokens.getAccessToken();
      String refreshTokenExpired = loginResponseWithExpiredTokens.getRefreshToken();


      //getMembers - expired access token - 401 & jwt exception (access token expired) in response header
      webTestClient.get()
          .uri("/members")
          .header("Authorization", "Bearer " + accessTokenExpired)
          .exchange()
          .expectStatus()
          .isUnauthorized()
          .expectHeader()
          .valueEquals(JWT_EXCEPTION_HEADER, JwtExceptionType.EXPIRED_ACCESS_TOKEN.getStatus())
          .expectBody(ErrorDto.class);

      log.info("refreshToken count1 : " + refreshTokenRepository.count());

      //refresh access token ->  401 & jwt exception (refresh token expired) in response header
      RefreshTokenDto expiredRefreshTokenDto = new RefreshTokenDto();
      expiredRefreshTokenDto.setRefreshToken(refreshTokenExpired);
      ErrorDto errorDtoResponseWithExpiredRefreshTokenHeader = webTestClient.put()
          .uri("/auth")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(expiredRefreshTokenDto)
          .exchange()
          .expectStatus()
          .isUnauthorized()
          .expectHeader()
          .valueEquals(JWT_EXCEPTION_HEADER, JwtExceptionType.EXPIRED_REFRESH_TOKEN.getStatus())
          .expectBody(ErrorDto.class)
          .returnResult()
          .getResponseBody();

      assertThat(errorDtoResponseWithExpiredRefreshTokenHeader).isNotNull();
      assertThat(errorDtoResponseWithExpiredRefreshTokenHeader.getMessage()).isEqualTo(JwtExceptionType.EXPIRED_REFRESH_TOKEN.getMessage());


      //restore access token & refresh token duration
      ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_DURATION", 60 * 60 * 1000L);
      ReflectionTestUtils.setField(jwtUtils, "REFRESH_TOKEN_DURATION", 60 * 60 * 1000L);

      //login - valid token duration - return valid access token & refresh token
      LoginResponseDto loginResponseWithValidToken = webTestClient.post()
          .uri("/auth")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(loginRequestDto)
          .exchange().expectStatus()
          .isOk()
          .expectBody(LoginResponseDto.class)
          .returnResult()
          .getResponseBody();

      assertThat(loginResponseWithValidToken).isNotNull();
      String validAccessToken = loginResponseWithValidToken.getAccessToken();
      String validRefreshToken = loginResponseWithValidToken.getRefreshToken();

      assertThat(validAccessToken).isNotNull();
      assertThat(validRefreshToken).isNotNull();


      //request for members - valid access token - 200
      webTestClient.get()
          .uri("/members")
          .header("Authorization", String.format("Bearer %s", validAccessToken))
          .exchange()
          .expectStatus()
          .isOk();

      assertThat(loginResponseWithValidToken).isNotNull();

      //logout - valid refresh token - 200
      webTestClient.delete().uri("/auth")
          .header("Authorization", String.format("Bearer %s", validRefreshToken))
          .exchange()
          .expectStatus()
          .isOk();
      assertThat(refreshTokenRepository.count() == 0).isTrue();
    }
  }
}
