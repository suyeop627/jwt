package com.study.securitywithjwt.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

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
//  private final Long REFRESH_TOKEN_DURATION = 30 * 60 * 1000L;


  public String issueToken(Long memberId, String subject, String name, Set<String> roles, String type) {
    String token = Jwts.builder()
        .subject(subject)
        .issuedAt(new Date())
        .expiration(new Date(new Date().getTime()+getDuration(type)))
        .claim("name", name)
        .claim("roles", roles)
        .claim("memberId", memberId)
        .signWith(getSecretKey(type))
        .compact();
    return token;
  }

  private long getDuration(String type) {
    return type.equals(ACCESS_TOKEN_TYPE) ? ACCESS_TOKEN_DURATION : REFRESH_TOKEN_DURATION;
  }

  public Claims getClaimsFromAccessToken(String token){
    return getClaims(token, ACCESS_TOKEN_TYPE);
  }

  public Claims getClaimsFromRefreshToken(String token){
    return getClaims(token, REFRESH_TOKEN_TYPE);
  }

  private Claims getClaims(String token, String type) {
    return Jwts.parser().verifyWith(getSecretKey(type)).build().parseSignedClaims(token).getPayload();
  }

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
