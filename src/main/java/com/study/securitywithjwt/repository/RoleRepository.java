package com.study.securitywithjwt.repository;

import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.utils.member.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByName(UserRole name);

}
