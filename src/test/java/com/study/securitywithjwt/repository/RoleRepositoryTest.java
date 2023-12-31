package com.study.securitywithjwt.repository;

import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.utils.member.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

//@ActiveProfiles("test")
@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)//@DataJpaTest 사용시, 내장 H2 db사용 - mysql적용 안돼서, mysql적용되도록 설정한 db를 사용하도록 replace X
class RoleRepositoryTest {
  @Autowired
  RoleRepository roleRepository;


  @BeforeEach
  void setUp() {
    roleRepository.save(new Role(1L, UserRole.ROLE_ADMIN));
    roleRepository.save(new Role(2L, UserRole.ROLE_MANAGER));
    roleRepository.save(new Role(3L, UserRole.ROLE_USER));
    System.out.println("======================roleRepository = " + roleRepository.findAll());
  }
  @Test
  void findByName_existName_returnOptionalRole() {
    Optional<Role> optionalRole = roleRepository.findByName(UserRole.ROLE_USER);
    Assertions.assertThat(optionalRole.isPresent()).isTrue();
    Assertions.assertThat(optionalRole.get().getName()).isEqualTo(UserRole.ROLE_USER);
  }

}