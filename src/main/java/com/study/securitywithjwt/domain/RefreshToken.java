package com.study.securitywithjwt.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//리프레시 토큰 엔티티
@Entity
@Table(name="refresh_token")
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long memberId;

  @Column(length = 512)
  private String token;
}
