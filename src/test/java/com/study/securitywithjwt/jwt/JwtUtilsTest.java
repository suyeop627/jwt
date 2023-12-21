package com.study.securitywithjwt.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {

  JwtUtils jwtUtils;
  String ACCESS_TOKEN_KEY_FOR_TEST = "test_access_key_test_access_key_test_access_key_test_access_key";
  String REFRESH_TOKEN_KEY_FOR_TEST = "test_refresh_key_test_refresh_key_test_refresh_key_test_refresh_key";

  String ACCESS_TOKEN_TYPE = "ACCESS";
  String REFRESH_TOKEN_TYPE = "REFRESH";

  @BeforeEach
  void setUp() {
    jwtUtils = new JwtUtils();
    //private field의 값을 바꾸는 테스트용 유틸로, 동적으로 값을 지정할 수 있게 해줌
    ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_KEY", ACCESS_TOKEN_KEY_FOR_TEST);
    ReflectionTestUtils.setField(jwtUtils, "REFRESH_TOKEN_KEY", REFRESH_TOKEN_KEY_FOR_TEST);
    ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_TYPE", ACCESS_TOKEN_TYPE);
    ReflectionTestUtils.setField(jwtUtils, "REFRESH_TOKEN_TYPE", REFRESH_TOKEN_TYPE);
  }


  @Test
  void issueToken_validState_returnJwt() {
    //given
    Long memberId = 1L;
    String subject = "test@test.com";
    String name = "testName";
    Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");
    String type = ACCESS_TOKEN_TYPE;
    //when
    String issuedToken = jwtUtils.issueToken(memberId, subject, name, roles, type);

    //then
    Claims claimsFromIssuedToken = jwtUtils.getClaimsFromAccessToken(issuedToken);
    assertThat(claimsFromIssuedToken.getSubject()).isEqualTo(subject);
    assertThat(claimsFromIssuedToken.get("name")).isEqualTo(name);
    assertThat(claimsFromIssuedToken.get("roles")).isEqualTo(new ArrayList<>(roles));//Claims는 기본적으로 arraylist로 저장함
  }

  @Test
  void getClaimsFromAccessToken_validState_returnClaims() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
    //given
    Long ACCESS_TOKEN_DURATION_FOR_TEST = 1000000L;


    //Field accessTokenKeyField = JwtUtils.class.getField("ACCESS_TOKEN_KEY");
    Class<?> jwtUtilsClass = Class.forName("com.study.securitywithjwt.jwt.JwtUtils");
    Field accessTokenKeyField = jwtUtilsClass.getDeclaredField("ACCESS_TOKEN_DURATION");
    accessTokenKeyField.setAccessible(true);
    accessTokenKeyField.set(jwtUtils, ACCESS_TOKEN_DURATION_FOR_TEST);


    Long memberId = 1L;
    String subject = "test@test.com";
    String name = "testName";
    Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");
    String type = ACCESS_TOKEN_TYPE;
    String issuedToken = jwtUtils.issueToken(memberId, subject, name, roles, type);

    //when
    Claims claimsFromIssuedToken = jwtUtils.getClaimsFromAccessToken(issuedToken);

    //then
    assertThat(claimsFromIssuedToken).isNotNull();
    assertThat(claimsFromIssuedToken.getExpiration())
        .isEqualTo(new Date(claimsFromIssuedToken.getIssuedAt().getTime()+ACCESS_TOKEN_DURATION_FOR_TEST));
  }

  @Test
  void getClaimsFromRefreshToken_validState_returnClaims() throws IllegalAccessException, NoSuchFieldException {
    //given
    Long REFRESH_TOKEN_DURATION_FOR_TEST = 1000000L;

    Field refreshTokenKeyField = JwtUtils.class.getDeclaredField("REFRESH_TOKEN_DURATION");
    refreshTokenKeyField.setAccessible(true);
    refreshTokenKeyField.set(jwtUtils, REFRESH_TOKEN_DURATION_FOR_TEST);


    Long memberId = 1L;
    String subject = "test@test.com";
    String name = "testName";
    Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");
    String type = REFRESH_TOKEN_TYPE;
    String issuedToken = jwtUtils.issueToken(memberId, subject, name, roles, type);

    //when
    Claims claimsFromIssuedToken = jwtUtils.getClaimsFromRefreshToken(issuedToken);

    //then
    assertThat(claimsFromIssuedToken).isNotNull();
    assertThat(claimsFromIssuedToken.getExpiration())
        .isEqualTo(new Date(claimsFromIssuedToken.getIssuedAt().getTime()+REFRESH_TOKEN_DURATION_FOR_TEST));
  }

  @Nested
  class TokenExceptionTest{
    @Test
    void getClaimsFromAccessToken_expiredToken_throwExpiredJwtException() {
      //given
      String token_expired = Jwts.builder()
          .subject("test")
          .issuedAt(new Date())
          .expiration(new Date(new Date().getTime()-100))
          .claim("name", "name")
          .claim("roles", Set.of("ROLE_USER"))
          .claim("memberId", 1L)
          .signWith(Keys.hmacShaKeyFor(ACCESS_TOKEN_KEY_FOR_TEST.getBytes()))
          .compact();

      //when, then
      assertThatThrownBy(() -> jwtUtils.getClaimsFromAccessToken(token_expired)).isInstanceOf(ExpiredJwtException.class);

    }

    @Test
    void getClaimsFromAccessToken_tokenNullOrEmpty_throwIllegalArgumentException() {
      //given
      String token_illegalArgs1 = " ";
      String token_null = null;

      //when, then
      assertThatThrownBy(() -> jwtUtils.getClaimsFromAccessToken(token_illegalArgs1)).isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> jwtUtils.getClaimsFromAccessToken(token_null)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void getClaimsFromAccessToken_InvalidSignature_throwSignatureException() {
      //given
      String token_signatureInvalid= Jwts.builder()
          .subject("test")
          .issuedAt(new Date())
          .expiration(new Date(new Date().getTime()-100))
          .claim("name", "name")
          .claim("roles", Set.of("ROLE_USER"))
          .claim("memberId", 1L)
          .signWith(Jwts.SIG.HS256.key().build())
          .compact();

      //when, then
      assertThatThrownBy(() -> jwtUtils.getClaimsFromAccessToken(token_signatureInvalid)).isInstanceOf(SignatureException.class);
    }

    @Test
    void getClaimsFromAccessToken_JsonFromMapOrBase64EncodedJsonFromMap_throwMalformedJwtException() throws JsonProcessingException {
      //given
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, String> map = Map.of("a", "b");
      String token_malFormed_anywayJson = objectMapper.writeValueAsString(map);
      String token_malFormed2_anywayBase64 = Base64.getEncoder().encodeToString(token_malFormed_anywayJson.getBytes());

      //when, then
      assertThatThrownBy(() -> jwtUtils.getClaimsFromAccessToken(token_malFormed_anywayJson)).isInstanceOf(MalformedJwtException.class);
      assertThatThrownBy(() -> jwtUtils.getClaimsFromAccessToken(token_malFormed2_anywayBase64)).isInstanceOf(MalformedJwtException.class);
    }
  }

}