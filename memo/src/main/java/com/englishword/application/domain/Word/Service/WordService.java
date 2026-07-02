package com.englishword.application.domain.Word.Service;

import com.englishword.application.domain.Word.Repository.WordRepository;
import com.englishword.application.domain.Word.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class WordService {
    @Autowired
    private WordRepository wordRepository;

    public List<Word> wordTest() {
        List<Word> allWords = wordRepository.findAll();
        Collections.shuffle(allWords);
        return allWords;
    }
}
