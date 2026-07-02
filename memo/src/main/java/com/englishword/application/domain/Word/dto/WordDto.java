package com.englishword.application.domain.Word.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordDto {
    private String word;
    private String meaning;

    public WordDto(String word, String meaning) {
        this.word = word;
        this.meaning = meaning;
    }

    @Override
    public String toString() {
        return "WordDto{word='" + word + "', meaning='" + meaning + "'}";
    }
}
