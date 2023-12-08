package com.study.springsecurityboard.member.domain;

import com.study.springsecurityboard.member.utils.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="role")
@NoArgsConstructor
@Data
public class Role {
  @Id
  @Column(name="role_id")
  private Long roleId;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private UserRole name;
}