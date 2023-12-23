package com.study.securitywithjwt.config;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RoleRepository;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

//  애플리케이션 실행 시, 기본적인 데이터를 DB에 저장하기 위한 클래스
//  ROLE 3가지(USER, MANAGER, ADMIN)은 항상 DB에 존재해야 함.
//  member는  테스트 용 사용자 생성 목적.
@Configuration
public class UtilConfig {
  private final PasswordEncoder passwordEncoder;

  public UtilConfig(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Bean
  public CommandLineRunner insertRoles(RoleRepository roleRepository) {
    return args -> {
      if (roleRepository.count() == 0) {

        Role adminRole = new Role(1L, UserRole.ROLE_ADMIN);

        Role managerRole = new Role(2L, UserRole.ROLE_MANAGER);

        Role userRole = new Role(3L, UserRole.ROLE_USER);

        roleRepository.save(userRole);
        roleRepository.save(managerRole);
        roleRepository.save(adminRole);
      }
    };
  }

  @Bean
  public CommandLineRunner insertAdminUser(MemberRepository memberRepository) {
    return args -> {
      if (memberRepository.count() == 0) {

        Role adminRole = new Role(1L, UserRole.ROLE_ADMIN);

        Member admin = Member.builder()
            .memberId(1L)
            .email("admin@test.com")
            .name("administrator")
            .password(passwordEncoder.encode("00000000"))
            .regdate(LocalDateTime.now())
            .gender(Gender.MALE)
            .phone("01011111111")
            .roles(Set.of(adminRole))
            .build();

        memberRepository.save(admin);
      }
    };
  }
}
