package com.tiokamp.repository;

import com.tiokamp.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    Optional<Score> findByUserId(Long userId);

    boolean existsByFinalRankIsNotNull();

    @Query("SELECT s FROM Score s JOIN FETCH s.user")
    List<Score> findAllWithUser();

    // Most recently updated score (for the leaderboard banner)
    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.lastUpdatedAt IS NOT NULL ORDER BY s.lastUpdatedAt DESC")
    List<Score> findMostRecentlyUpdated(org.springframework.data.domain.Pageable pageable);

    // Final standings after the one-time calculation (lowest total wins)
    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.finalRank IS NOT NULL ORDER BY s.finalRank ASC, s.user.username ASC")
    List<Score> findFinalStandings();
}
