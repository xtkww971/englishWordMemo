package com.englishword.application.domain.Word.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WordTestResultDto {
    private Long wordId;
    private String word;
    private Boolean isCorrect; // 맞았으면 true, 틀렸으면 false
}
