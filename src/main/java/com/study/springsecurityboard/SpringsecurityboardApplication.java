package com.study.springsecurityboard;

import com.study.springsecurityboard.domain.Role;
import com.study.springsecurityboard.repository.RoleRepository;
import com.study.springsecurityboard.utils.member.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringsecurityboardApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringsecurityboardApplication.class, args);
  }
  @Bean
  public CommandLineRunner initRoles(RoleRepository roleRepository) {
    return args -> {
      if (roleRepository.count() == 0) { // role 테이블에 데이터가 없을 경우
        Role userRole = new Role();
        userRole.setRoleId(1L);
        userRole.setName(UserRole.ROLE_USER);

        Role adminRole = new Role();
        adminRole.setRoleId(2L);
        adminRole.setName(UserRole.ROLE_ADMIN);

        roleRepository.save(userRole);
        roleRepository.save(adminRole);
      }
    };
  }
}
