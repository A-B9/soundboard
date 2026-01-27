package com.soundboard.soundboard.repository;

import com.soundboard.soundboard.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyUserRepo extends JpaRepository<Users,Long> {

  Users findByUsername(String username);
}
