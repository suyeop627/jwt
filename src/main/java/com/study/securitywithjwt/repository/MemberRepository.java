package com.study.securitywithjwt.repository;

import com.study.securitywithjwt.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByEmail(String username);

  boolean existsByEmail(String userEmail);

  boolean existsByPhone(String phone);

}
