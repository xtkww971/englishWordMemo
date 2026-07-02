package com.englishword.application.domain.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "word_test")
public class WordTest {
    @Id
    private Long wordTestId;
}
