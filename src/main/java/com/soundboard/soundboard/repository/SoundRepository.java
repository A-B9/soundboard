package com.soundboard.soundboard.repository;

import com.soundboard.soundboard.domain.Sound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {

}
