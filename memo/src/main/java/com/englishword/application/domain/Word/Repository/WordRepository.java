package com.englishword.application.domain.Word.Repository;

import com.englishword.application.domain.Word.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    boolean existsByWord(String word);

    // 1. 중복 없이 저장된 모든 날짜 목록 조회 (최신순)
    @Query("SELECT DISTINCT w.createdAt FROM Word w ORDER BY w.createdAt DESC")
    List<LocalDate> findDistinctCreatedAt();

    // 2. 특정 날짜에 해당하는 단어 목록 조회
    List<Word> findByCreatedAt(LocalDate studyDate);

    // 3. MySQL 자체에서 랜덤으로 80개 단어 뽑아오기 (성능 및 확장성 최고)
    @Query(value = "SELECT * FROM words ORDER BY RAND() LIMIT 80", nativeQuery = true)
    List<Word> findRandom80Words();

    @Query("SELECT w FROM Word w WHERE w.wrongCount >= 1 ORDER BY w.wrongCount DESC")
    List<Word> findWrongWords();
}
