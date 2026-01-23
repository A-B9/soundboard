package com.soundboard.repository;

import com.soundboard.domain.models.SoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoundRepository extends JpaRepository<SoundEntity, Long> {

}
