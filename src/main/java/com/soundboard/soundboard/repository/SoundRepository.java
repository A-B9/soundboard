package com.soundboard.soundboard.repository;

import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.util.SoundCategoryEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SoundRepository extends JpaRepository<SoundEntity, UUID> {

  @Query("""
    SELECT s
    FROM SoundEntity s
    WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
  """)
  List<SoundEntity> search(String keyword);

  @Query("""
    SELECT s
    FROM SoundEntity s
    WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    AND s.ownedBy = :username
  """)
  List<SoundEntity> searchByOwner(@Param("keyword") String keyword, @Param("username") String username);

  Page<SoundEntity> findAllByOwnedBy(Pageable pageable, String ownedBy);

  List<SoundEntity> findAllByOwnedBy(String ownedBy);

  Optional<SoundEntity> findByIdAndOwnedBy(UUID id, String ownedBy);

  @Query(value = "SELECT s FROM SoundEntity s WHERE s.ownedBy = :username AND s.category = :category",
         countQuery = "SELECT COUNT(s) FROM SoundEntity s WHERE s.ownedBy = :username AND s.category = :category")
  Page<SoundEntity> findAllByOwnedByAndCategory(Pageable pageable,
                                                 @Param("username") String username,
                                                 @Param("category") SoundCategoryEnum category);

  @Query(value = """
    SELECT s FROM SoundEntity s
    WHERE s.ownedBy = :username
    AND EXISTS (SELECT t FROM s.tags t WHERE UPPER(t) = UPPER(:tag))
    """,
    countQuery = """
    SELECT COUNT(s) FROM SoundEntity s
    WHERE s.ownedBy = :username
    AND EXISTS (SELECT t FROM s.tags t WHERE UPPER(t) = UPPER(:tag))
    """)
  Page<SoundEntity> findAllByOwnedByAndTag(Pageable pageable,
                                            @Param("username") String username,
                                            @Param("tag") String tag);

  @Query(value = """
    SELECT s FROM SoundEntity s
    WHERE s.ownedBy = :username AND s.category = :category
    AND EXISTS (SELECT t FROM s.tags t WHERE UPPER(t) = UPPER(:tag))
    """,
    countQuery = """
    SELECT COUNT(s) FROM SoundEntity s
    WHERE s.ownedBy = :username AND s.category = :category
    AND EXISTS (SELECT t FROM s.tags t WHERE UPPER(t) = UPPER(:tag))
    """)
  Page<SoundEntity> findAllByOwnedByAndCategoryAndTag(Pageable pageable,
                                                       @Param("username") String username,
                                                       @Param("category") SoundCategoryEnum category,
                                                       @Param("tag") String tag);

}
