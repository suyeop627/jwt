package com.study.springsecurityboard.member.repository;

import com.study.springsecurityboard.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
