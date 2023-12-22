package com.study.securitywithjwt.service;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.RefreshToken;
import com.study.securitywithjwt.dto.LoginRequestDto;
import com.study.securitywithjwt.dto.LoginResponseDto;
import com.study.securitywithjwt.exception.JwtAuthenticationException;
import com.study.securitywithjwt.exception.JwtExceptionType;
import com.study.securitywithjwt.jwt.JwtUtils;
import com.study.securitywithjwt.security.user.MemberUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final RefreshTokenService refreshTokenService;
  private final String TYPE_ACCESS = "ACCESS";
  private final String TYPE_REFRESH = "REFRESH";


  public LoginResponseDto login(LoginRequestDto loginRequestDto) {
    //첫 로그인 시, username과 password로 해당유저가 존재하는지 확인하므로, security가 기본으로 제공하는 UsernamePasswordAuthenticationToken 사용함
    //토큰이 실려 오는경우엔 filter로만 처리. - principal -> Member(db에서 가져온 걸 그대로 저장함)
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequestDto.getEmail(), loginRequestDto.getPassword())
    );

    deleteRefreshTokenIfExists(loginRequestDto);

    //principal of Authentication = MemberUserDetails member
    Member member = ((MemberUserDetails) authentication.getPrincipal()).getMember();
    Set<String> roles = member.getRoleNameSet();

    String accessToken = jwtUtils.issueToken(member.getMemberId(), member.getEmail(), member.getName(), roles, TYPE_ACCESS);
    String refreshToken = jwtUtils.issueToken(member.getMemberId(), member.getEmail(), member.getName(), roles, TYPE_REFRESH);
    log.info("Created access token : {} ", accessToken);
    log.info("Created refresh token : {}", refreshToken);

    saveRefreshTokenOfLoginMember(member.getMemberId(), refreshToken);

    return LoginResponseDto.builder()
        .name(member.getName())
        .email(member.getEmail())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  private void deleteRefreshTokenIfExists(LoginRequestDto loginRequestDto) {
    Optional<RefreshToken> refreshTokenSavedInDB = refreshTokenService.selectRefreshTokenByMemberEmail(loginRequestDto.getEmail());
    refreshTokenSavedInDB.ifPresent(refreshToken -> refreshTokenService.deleteRefreshTokenById(refreshToken.getId()));
  }


  private void saveRefreshTokenOfLoginMember(Long memberId, String refreshToken) {
    RefreshToken refreshTokenOfLoginMember = RefreshToken.builder().token(refreshToken).memberId(memberId).build();
    refreshTokenService.insertRefreshToken(refreshTokenOfLoginMember);
  }


  public LoginResponseDto authenticateWithRefreshToken(String refreshToken) {

    Claims claimsFromRefreshToken = getClaimsRefreshTokenOrThrowException(refreshToken);

    String name = claimsFromRefreshToken.get("name", String.class);
    String subject = claimsFromRefreshToken.getSubject();
    Long memberId = claimsFromRefreshToken.get("memberId", Long.class);
    List<String> roleFromClaims = (List<String>) claimsFromRefreshToken.get("roles");

    String accessToken = jwtUtils.issueToken(memberId, subject, name, new HashSet<>(roleFromClaims), TYPE_ACCESS);

    return LoginResponseDto.builder()
        .name(name)
        .email(subject)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  private Claims getClaimsRefreshTokenOrThrowException(String refreshToken) {
    Claims claimsFromRefreshToken;
    try {
      claimsFromRefreshToken = jwtUtils.getClaimsFromRefreshToken(refreshToken);
    } catch (ExpiredJwtException e) {
      //전달받은 토큰의 유효기간이 지난 경우, 기존 토큰 삭제 및 재인증 요청 -> exception handler
      refreshTokenService.deleteRefreshToken(refreshToken);
      throw new JwtAuthenticationException(JwtExceptionType.EXPIRED_REFRESH_TOKEN.getMessage(), JwtExceptionType.EXPIRED_REFRESH_TOKEN);
    } catch (Exception e) {
      //전달받은 토큰을 parsing 할때 기타 예외가 발생한 경우 기존 토큰 삭제 및 예외 처리
      refreshTokenService.deleteRefreshToken(refreshToken);
      throw new JwtAuthenticationException(JwtExceptionType.UNKNOWN_ERROR.getMessage(), JwtExceptionType.UNKNOWN_ERROR);
    }
    return claimsFromRefreshToken;
  }
}
