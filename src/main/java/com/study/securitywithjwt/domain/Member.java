package com.study.securitywithjwt.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.study.securitywithjwt.utils.member.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="member")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
  @Id
  @Column(name = "member_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long memberId;

  @Column(length = 255, unique = true)
  private String email;

  @Column(length = 50)
  private String name;

  @JsonIgnore
  @Column(length = 500)
  private String password;

  @CreationTimestamp
  private LocalDateTime regdate;

  @Column(length = 10, nullable = false)
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @ManyToMany(fetch = FetchType.EAGER)//LazyInitializationException 방지
  @JoinTable(name = "user_role",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  @Builder.Default
  //@Builder를 class에 붙였을 때, build()과정에 포함되지 않은 필드들은 모두 null이나 0이 됨.
  //@Builder.Default로 초기값 설정 가능함
  private Set<Role> roles = new HashSet<>();

  public void addRole(Role role){
    roles.add(role);
  }
}
