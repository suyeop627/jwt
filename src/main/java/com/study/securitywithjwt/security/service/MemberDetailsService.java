package com.study.securitywithjwt.security.service;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.security.user.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
//DaoAuthenticationProvider 에 의해서 UsernamePasswordToken 을 인증할 때 사용됨
//입력받은 username으로,  db에 저장된 member를 조회함.
@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {
  private final MemberRepository memberRepository;
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<Member> member = memberRepository.findByEmail(username);
    return new MemberDetails(member.orElseThrow(()->
        new UsernameNotFoundException(String.format("username : %s is nonexistent", username))));
  }
}
