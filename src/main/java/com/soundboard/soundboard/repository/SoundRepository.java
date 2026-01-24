package com.soundboard.soundboard.repository;

import com.soundboard.soundboard.models.SoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoundRepository extends JpaRepository<SoundEntity, Long> {
  
  @Query("""
    SELECT s
    FROM SoundEntity s
    WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
  """)
  List<SoundEntity> search(String keyword);

}
