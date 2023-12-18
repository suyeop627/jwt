package com.study.securitywithjwt.config;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RoleRepository;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

@Configuration
public class UtilConfig {
  private final PasswordEncoder passwordEncoder;

  public UtilConfig(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Bean
  public CommandLineRunner initRoles(RoleRepository roleRepository) {
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
  public CommandLineRunner initTestUser(MemberRepository memberRepository) {
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
            .roles(Set.of(adminRole))
            .build();


        Role managerRole = new Role(2L, UserRole.ROLE_MANAGER);

        Member manager = Member.builder()
            .memberId(2L)
            .email("manager@test.com")
            .name("manager")
            .password(passwordEncoder.encode("00000000"))
            .regdate(LocalDateTime.now())
            .gender(Gender.MALE)
            .roles(Set.of(managerRole))
            .build();

        Role userRole = new Role(3L, UserRole.ROLE_USER);
        Member user = Member.builder()
            .memberId(3L)
            .email("user@test.com")
            .name("user")
            .password(passwordEncoder.encode("00000000"))
            .regdate(LocalDateTime.now())
            .gender(Gender.MALE)
            .roles(Set.of(userRole))
            .build();

        memberRepository.save(admin);
        memberRepository.save(manager);
        memberRepository.save(user);
      }
    };
  }


}
