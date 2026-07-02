package com.englishword.application.domain.Word.Controller;

import com.englishword.application.domain.Word.Service.WordService;
import com.englishword.application.domain.Word.Word;
import com.englishword.application.domain.Word.dto.WordTestResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/word")
public class WordController {
    @Autowired
    private WordService wordService;

    @GetMapping("/test")
    public List<Word> getWordTest() {
        return wordService.wordTest();
    }

    @PostMapping("/result")
    public ResponseEntity<String> saveWordResult(@RequestBody List<WordTestResultDto> results) {

        // 콘솔로 데이터가 누락 없이 잘 도달했는지 확인하는 로그
        System.out.println("====== 수신된 단어 시험 결과 ======");
        for (WordTestResultDto result : results) {
            System.out.println("단어ID: " + result.getWordId() +
                    ", 단어명: " + result.getWord() +
                    ", 정답여부: " + (result.getIsCorrect() ? "O" : "X"));
        }

        // 나중에 필요하시다면 서비스 레이어로 넘겨 DB 오답노트 테이블 등에 save 처리 하시면 됩니다.
        // wordService.saveResults(results);

        return ResponseEntity.ok("Success");
    }
}
