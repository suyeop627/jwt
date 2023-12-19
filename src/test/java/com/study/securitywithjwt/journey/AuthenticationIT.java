package com.study.securitywithjwt.journey;

import com.study.securitywithjwt.dto.LoginRequestDto;
import com.study.securitywithjwt.dto.LoginResponseDto;
import com.study.securitywithjwt.dto.MemberSignupRequestDto;
import com.study.securitywithjwt.dto.MemberSignupResponseDto;
import com.study.securitywithjwt.jwt.JwtUtils;
import com.study.securitywithjwt.utils.member.Gender;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:/application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthenticationIT {

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void loginAndSignupIT(){
    String email = "test123@test.com";
    String password = "00000000"; //password size is too short
    LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);

    MemberSignupRequestDto signupRequestDto = new MemberSignupRequestDto();
    signupRequestDto.setEmail(email);
    signupRequestDto.setName("name");
    signupRequestDto.setPassword(password);
    signupRequestDto.setGender(Gender.MALE);


    //before signup
    postLoginRequestWithLoginRequestDto(loginRequestDto)
        .expectStatus()
        .isUnauthorized();


    //sign up - email malformed
    signupRequestDto.setEmail("test");
    signupRequestDto.setPassword("00000000");

    postSignUpRequestWIthSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();

    //sign up - password malformed
    signupRequestDto.setEmail("test@test.com");
    signupRequestDto.setPassword("000");
    postSignUpRequestWIthSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();

    //sign up - name malformed
    signupRequestDto.setEmail("test@test.com");
    signupRequestDto.setPassword("00000000");
    signupRequestDto.setName("d");
    postSignUpRequestWIthSignupRequestDto(signupRequestDto)
        .expectStatus()
        .isBadRequest();

    //sign up - ok
    signupRequestDto.setEmail("test@test.com");
    signupRequestDto.setPassword("00000000");
    signupRequestDto.setName("name");

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


}
