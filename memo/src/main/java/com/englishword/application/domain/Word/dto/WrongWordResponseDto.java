package com.englishword.application.domain.Word.dto;

import com.englishword.application.domain.Word.Word;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WrongWordResponseDto {
    private String word;
    private String meaning;
    private int wrongCount;

    public static WrongWordResponseDto from(Word word) {
        return WrongWordResponseDto.builder()
                .word(word.getWord())
                .meaning(word.getMeaning())
                .wrongCount(word.getWrongCount())
                .build();
    }
}
