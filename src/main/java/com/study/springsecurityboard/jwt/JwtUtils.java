package com.study.springsecurityboard.jwt;

import com.study.springsecurityboard.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtUtils {
  @Value("${jwt.secretKey.accessToken}")
  private String ACCESS_TOKEN_KEY;

  @Value("${jwt.secretKey.refreshToken}")
  private String REFRESH_TOKEN_KEY;

  @Value("${jwt.type.accessToken}")
  private String ACCESS_TOKEN_TYPE;

  public final Long ACCESS_TOKEN_DURATION =  30 * 60 * 1000L; // 30 minutes

  //public final Long REFRESH_TOKEN_DURATION = 7 * 24 * 60 * 60 * 1000L; // 7 days
  public final Long REFRESH_TOKEN_DURATION = 3 * 60 * 1000L; // 7 days


  public String issueToken(Long memberId, String subject, String name, Set<String> roles, String type) {
    String token = Jwts.builder()
        .subject(subject)
        .issuedAt(new Date())
        .expiration(new Date(new Date().getTime()+getDuration(type)))
        .claim("name", name)
        .claim("roles", roles)
        .claim("type", type)
        .claim("memberId", memberId)
        .signWith(getSecretKey(type))
        .compact();
    return token;
  }

  private long getDuration(String type) {
    return type.equals(ACCESS_TOKEN_TYPE) ? ACCESS_TOKEN_DURATION : REFRESH_TOKEN_DURATION;
  }


  public String getSubject(Claims claims) {
    return claims.getSubject();

  }
  public Claims getClaimsFromAccessToken(String token){
    return getClaims(token, ACCESS_TOKEN_TYPE);
  }

  public Claims getClaimsFromRefreshToken(String token){
    return getClaims(token, REFRESH_TOKEN_KEY);
  }


  private Claims getClaims(String token, String type) {
    return Jwts.parser().verifyWith(getSecretKey(type)).build().parseSignedClaims(token).getPayload();

  }


  private SecretKey getSecretKey(String type){
    String key = type.equals(ACCESS_TOKEN_TYPE) ? ACCESS_TOKEN_KEY : REFRESH_TOKEN_KEY;
    return Keys.hmacShaKeyFor(key.getBytes());
  }

  //토큰 유효성 검사
  public boolean isTokenAccessTokenValid(String token, String username) {
    String subject = getSubject(getClaimsFromAccessToken(token));
    return subject.equals(username) && isTokenNotExpired(token);
  }

  public boolean isTokenNotExpired(String token) {
    Date now = new Date();
    return getClaimsFromAccessToken(token).getExpiration().before(now);
  }

}
