package com.englishword.application.domain.Word.Service;

import com.englishword.application.domain.Word.Repository.WordRepository;
import com.englishword.application.domain.Word.Word;
import com.englishword.application.domain.Word.dto.WordTestResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class WordService {
    @Autowired
    private WordRepository wordRepository;

    public List<Word> getRandom80Exam() {
        return wordRepository.findRandom80Words();
    }

    public void saveResults(List<WordTestResultDto> results) {
        for(WordTestResultDto e : results) {
            Optional<Word> a = wordRepository.findById(e.getWordId());
            if(a.isPresent()) {
                Word word = a.get();
                int wrongCount  = (e.getIsCorrect() ? word.getWrongCount() : word.getWrongCount()+1);
            }
        }
    }

    // 화면에 띄울 날짜 가지고 오기
    public List<LocalDate> getAvailableDates() {
        return wordRepository.findDistinctCreatedAt();
    }

    public List<Word> getWordsByDate(LocalDate date) {
        return wordRepository.findByCreatedAt(date);
    }

    // 날짜별 시험
    public List<Word> getDateExam(LocalDate date) {
        List<Word> words = wordRepository.findByCreatedAt(date);
        Collections.shuffle(words); // 무작위로 섞기
        return words;
    }

    public List<Word> getWrongWords() {
        return wordRepository.findWrongWords();
    }

    @Transactional
    public void reportWrongWords(List<Long> wrongWordIds) {
        for (Long id : wrongWordIds) {
            wordRepository.findById(id).ifPresent(Word::increaseWrongCount);
        }
    }
}
