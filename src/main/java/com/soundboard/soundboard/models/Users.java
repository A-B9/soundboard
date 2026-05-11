package com.soundboard.soundboard.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String username;

  private String password;

  private Instant createdAt;

  private boolean active = true;

  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "varchar(20) default 'USER'")
  @Builder.Default
  private Role role = Role.USER;

  public void setPassword(String password) {
    this.password = password;
  }
}
