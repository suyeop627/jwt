package com.study.springsecurityboard.member.repository;

import com.study.springsecurityboard.member.domain.Role;
import com.study.springsecurityboard.member.utils.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByName(UserRole name);

}
