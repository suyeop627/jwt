package com.study.springsecurityboard.repository;

import com.study.springsecurityboard.domain.Role;
import com.study.springsecurityboard.utils.member.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByName(UserRole name);

}
