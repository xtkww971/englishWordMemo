package com.englishword.application.domain.Word.Controller;

import com.englishword.application.domain.Word.Service.WordService;
import com.englishword.application.domain.Word.Word;
import com.englishword.application.domain.Word.dto.WrongWordResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/words")
public class WordController {
    @Autowired
    private WordService wordService;

    @GetMapping("/dates")
    public ResponseEntity<List<LocalDate>> getDates() {
        return ResponseEntity.ok(wordService.getAvailableDates());
    }

    // 2. 날짜 클릭 시: 해당 날짜의 단어 목록 반환 (학습 화면)
    @GetMapping("/date/{date}")
    public ResponseEntity<List<Word>> getWordsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(wordService.getWordsByDate(date));
    }

    // 3. 단어장 안에서 [시험보기]: 해당 날짜의 단어를 무작위로 섞어서 반환
    @GetMapping("/exam/date/{date}")
    public ResponseEntity<List<Word>> getDateExam(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(wordService.getDateExam(date));
    }

    // 4. 메인 화면 [전체 중 80개 시험]: MySQL에서 바로 랜덤 80개 반환
    @GetMapping("/exam/random80")
    public ResponseEntity<List<Word>> getRandom80Exam() {
        return ResponseEntity.ok(wordService.getRandom80Exam());
    }

    // 5. 시험 종료 후: 틀린 단어 고유 ID(MySQL PK) 리스트를 받아 틀린 횟수 누적 업데이트
    @PostMapping("/exam/wrong")
    public ResponseEntity<String> updateWrongCounts(@RequestBody List<Long> wrongWordIds) {
        wordService.reportWrongWords(wrongWordIds);
        return ResponseEntity.ok("오답 카운트가 MySQL에 성공적으로 반영되었습니다.");
    }

    @GetMapping("/wrongWords")
    public ResponseEntity<List<WrongWordResponseDto>> getWrongWords() {
        List<Word> words = wordService.getWrongWords();
        return ResponseEntity.ok(words.stream().map(WrongWordResponseDto::from).toList());
    }
}
