package com.englishword.application.domain.Word;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "word")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Word {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wordId;
    private String word;
    private String meaning;

    public Word(String word, String meaning) {
        this.word =word;
        this.meaning = meaning;
    }
}
