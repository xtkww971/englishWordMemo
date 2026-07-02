package com.englishword.application.domain.Word;

import jakarta.persistence.*;

@Entity
@Table(name = "word_info")
public class WordInfo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wordId;
}
