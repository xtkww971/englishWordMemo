package com.englishword.application.domain.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sentence_test")
public class SentenceTest {
    @Id
    private Long sentenceTestId;
}
