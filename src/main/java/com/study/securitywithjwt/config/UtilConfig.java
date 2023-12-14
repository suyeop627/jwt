package com.study.securitywithjwt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.study.securitywithjwt.dao.RoleRepository;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.utils.member.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilConfig {

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
