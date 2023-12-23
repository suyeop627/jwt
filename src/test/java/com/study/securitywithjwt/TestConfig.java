package com.study.securitywithjwt;

import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RoleRepository;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

@TestConfiguration
public class TestConfig {
  @Autowired
  PasswordEncoder passwordEncoder;
  @Bean
  public CommandLineRunner initRolesForTest(RoleRepository roleRepository) {
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
            .phone("01009476322")
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
            .phone("01087446352")
            .roles(Set.of(managerRole))
            .build();

        Role userRole = new Role(3L, UserRole.ROLE_USER);
        Member user = Member.builder()
            .memberId(3L)
            .email("user@test.com")
            .name("user")
            .phone("01091826354")
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
