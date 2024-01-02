package com.study.securitywithjwt.jwt;

import com.study.securitywithjwt.dto.LoginMemberInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
//Jwt 생성, 파싱 등 처리 기능 담당
@Service
public class JwtUtils {
  @Value("${jwt.key.accessToken}")
  private String ACCESS_TOKEN_KEY;
  @Value("${jwt.key.refreshToken}")
  private String REFRESH_TOKEN_KEY;
  @Value("${jwt.type.accessToken}")
  private String ACCESS_TOKEN_TYPE;
  @Value("${jwt.type.refreshToken}")
  private String REFRESH_TOKEN_TYPE;
  private final Long ACCESS_TOKEN_DURATION =  30 * 60 * 1000L; // 30 minutes
  private final Long REFRESH_TOKEN_DURATION = 7 * 24 * 60 * 60 * 1000L; // 7 days

//  for test
//  private final Long ACCESS_TOKEN_DURATION =  1L;
//  private final Long REFRESH_TOKEN_DURATION = 1* 1000L;

//토큰 발행
  public String issueToken(LoginMemberInfo loginMemberInfo, String type) {
    String token = Jwts.builder()
        .subject(loginMemberInfo.getEmail())
        .issuedAt(new Date())
        .expiration(new Date(new Date().getTime()+getDuration(type)))
        .claim("name", loginMemberInfo.getName())
        .claim("roles", loginMemberInfo.getRoles())
        .claim("memberId", loginMemberInfo.getMemberId())
        .signWith(getSecretKey(type))
        .compact();
    return token;
  }

  //토큰 타입에 따라 토큰의 expiration 조회
  private long getDuration(String type) {
    return type.equals(ACCESS_TOKEN_TYPE) ? ACCESS_TOKEN_DURATION : REFRESH_TOKEN_DURATION;
  }
  //access token의 클레임 반환
  public Claims extractClaimsFromAccessToken(String token){
    return getClaims(token, ACCESS_TOKEN_TYPE);
  }
  //refresh token의 클레임 반환
  public Claims extractClaimsFromRefreshToken(String token){
    return getClaims(token, REFRESH_TOKEN_TYPE);
  }
  //토킅에서 클레임 반환
  private Claims getClaims(String token, String type) {
    return Jwts.parser().verifyWith(getSecretKey(type)).build().parseSignedClaims(token).getPayload();
  }
//토큰 타입에 따라 사용될 키 반환
  private SecretKey getSecretKey(String type){
    String key = type.equals(ACCESS_TOKEN_TYPE) ? ACCESS_TOKEN_KEY : REFRESH_TOKEN_KEY;
    return Keys.hmacShaKeyFor(key.getBytes());
  }


// exception으로 대체
//  //토큰 유효성 검사
//  public boolean isTokenAccessTokenValid(String token, String username) {
//    String subject = getSubject(getClaimsFromAccessToken(token));
//    return subject.equals(username) && isTokenNotExpired(token);
//  }
//
//  public boolean isTokenNotExpired(String token) {
//    Date now = new Date();
//    return getClaimsFromAccessToken(token).getExpiration().before(now);
//  }

}
