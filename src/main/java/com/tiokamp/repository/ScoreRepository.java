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

    // Most recently updated score (for the leaderboard banner)
    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.lastUpdatedAt IS NOT NULL ORDER BY s.lastUpdatedAt DESC")
    List<Score> findMostRecentlyUpdated(org.springframework.data.domain.Pageable pageable);

    // Top 3 per event — called once per event in ScoreService
    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.event1 IS NOT NULL ORDER BY s.event1 DESC, s.user.username ASC")
    List<Score> findTop3ByEvent1(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.event2 IS NOT NULL ORDER BY s.event2 DESC, s.user.username ASC")
    List<Score> findTop3ByEvent2(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.event3 IS NOT NULL ORDER BY s.event3 DESC, s.user.username ASC")
    List<Score> findTop3ByEvent3(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.event4 IS NOT NULL ORDER BY s.event4 DESC, s.user.username ASC")
    List<Score> findTop3ByEvent4(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.event5 IS NOT NULL ORDER BY s.event5 DESC, s.user.username ASC")
    List<Score> findTop3ByEvent5(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.event6 IS NOT NULL ORDER BY s.event6 DESC, s.user.username ASC")
    List<Score> findTop3ByEvent6(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.event7 IS NOT NULL ORDER BY s.event7 DESC, s.user.username ASC")
    List<Score> findTop3ByEvent7(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.event8 IS NOT NULL ORDER BY s.event8 DESC, s.user.username ASC")
    List<Score> findTop3ByEvent8(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.event9 IS NOT NULL ORDER BY s.event9 DESC, s.user.username ASC")
    List<Score> findTop3ByEvent9(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s FROM Score s JOIN FETCH s.user WHERE s.event10 IS NOT NULL ORDER BY s.event10 DESC, s.user.username ASC")
    List<Score> findTop3ByEvent10(org.springframework.data.domain.Pageable pageable);
}
