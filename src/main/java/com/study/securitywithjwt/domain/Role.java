package com.study.securitywithjwt.domain;

import com.study.securitywithjwt.utils.member.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="role")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Role {
  @Id
  @Column(name="role_id")
  private Long roleId;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private UserRole name;
}