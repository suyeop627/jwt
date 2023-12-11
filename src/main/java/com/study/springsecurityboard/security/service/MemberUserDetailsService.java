package com.study.springsecurityboard.security.service;

import com.study.springsecurityboard.domain.Member;
import com.study.springsecurityboard.repository.MemberRepository;
import com.study.springsecurityboard.security.user.MemberUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {
  private final MemberRepository memberRepository;
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<Member> member = memberRepository.findByEmail(username);
    return new MemberUserDetails(member.orElseThrow(()->
        new UsernameNotFoundException(username + "은 존재하지 않는 email입니다.")));
  }
}
