package com.study.securitywithjwt;

import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dao.RoleRepository;
import com.study.securitywithjwt.utils.member.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringsecurityboardApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringsecurityboardApplication.class, args);
  }

}
