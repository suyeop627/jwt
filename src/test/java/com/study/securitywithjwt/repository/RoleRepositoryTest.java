package com.study.securitywithjwt.repository;

import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.utils.member.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

//@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)//@DataJpaTest 사용시, 내장 H2 db사용 - mysql적용 안돼서, mysql적용되도록 설정한 db를 사용하도록 replace X
class RoleRepositoryTest {
  @Autowired
  RoleRepository roleRepository;


  @BeforeEach
  void setUp() {
    //로드 시, role 3개 저장되도록 설정하여, 필요없어짐.
//    roleRepository.save(new Role(1L, UserRole.ROLE_ADMIN));
//    roleRepository.save(new Role(2L, UserRole.ROLE_MANAGER));
//    roleRepository.save(new Role(3L, UserRole.ROLE_USER));
  }
  @Test
  void findByName_existName_returnOptionalRole() {
    Optional<Role> optionalRole = roleRepository.findByName(UserRole.ROLE_USER);
    Assertions.assertThat(optionalRole.isPresent()).isTrue();
    Assertions.assertThat(optionalRole.get().getName()).isEqualTo(UserRole.ROLE_USER);
  }
}