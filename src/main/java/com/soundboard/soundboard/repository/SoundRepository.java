package com.soundboard.soundboard.repository;

import com.soundboard.soundboard.models.SoundEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoundRepository extends JpaRepository<SoundEntity, Long> {
  
  @Query("""
    SELECT s
    FROM SoundEntity s
    WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
  """)
  List<SoundEntity> search(String keyword);
  
  Page<SoundEntity> findAllByOwnedTo(Pageable pageable, String ownedTo);
  
  Optional<SoundEntity> findByIdAndOwnedTo(Long id, String ownedTo);

}
