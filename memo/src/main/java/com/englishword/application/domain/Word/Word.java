package com.englishword.application.domain.Word;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "word")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String word; // 영어 단어

    @Column(nullable = false)
    private String meaning; // 뜻

    @Column(nullable = false)
    private LocalDate createdAt; // 학습/등록 날짜 (YYYY-MM-DD)

    @Column(nullable = false)
    private int wrongCount = 0; // 틀린 횟수 (기본값 0)

    // 생성자
    public Word(String word, String meaning) {
        this.word = word;
        this.meaning = meaning;
        this.createdAt = LocalDate.now(); // 노션에서 DB로 긁어온 당일 날짜가 자동으로 지정됩니다.
    }

    // 틀린 횟수 증가 메서드 (Domain 로직)
    public void increaseWrongCount() {
        this.wrongCount += 1;
    }
}