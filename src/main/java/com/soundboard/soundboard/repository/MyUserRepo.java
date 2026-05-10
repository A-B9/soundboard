package com.soundboard.soundboard.repository;

import com.soundboard.soundboard.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MyUserRepo extends JpaRepository<Users, UUID> {

  Users findByUsername(String username);
  
  Boolean existsByUsername(String username);
}
