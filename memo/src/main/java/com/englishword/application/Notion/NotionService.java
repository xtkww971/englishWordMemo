package com.englishword.application.Notion;

import com.englishword.application.domain.Word.Repository.WordRepository;
import com.englishword.application.domain.Word.Word;
import com.englishword.application.domain.Word.dto.WordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NotionService {

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${notion.access-token}")
    private String accessToken;

    @Value("${notion.database-id}") // 여기서는 부모 페이지 ID 역할입니다.
    private String pageId;

    @Value("${notion.version}")
    private String notionVersion;

    @Autowired
    private WordRepository wordRepository;

    public List<WordDto> fetchAllWordsFromPage() {
        List<WordDto> allWords = new ArrayList<>();

        try {
            // 1단계: 부모 페이지의 1단계 자식들(토글 블록들)을 가져옵니다.
            List<String> toggleBlockIds = fetchChildBlockIdsByType(pageId, "heading_1", "heading_2", "heading_3", "toggle");
            System.out.println("🔍 발견된 날짜 토글 블록 개수: " + toggleBlockIds.size() + "개");

            // 2단계: 각 토글 블록 안으로 파고들어 단순 'table' 블록의 ID를 찾습니다.
            for (String toggleId : toggleBlockIds) {
                List<String> tableIds = fetchChildBlockIdsByType(toggleId, "table");

                // 3단계: 발견된 표(Table)의 행(Row)들을 조회해서 단어/뜻을 파싱합니다.
                for (String tableId : tableIds) {
                    List<WordDto> tableWords = parseSimpleTable(tableId);
                    allWords.addAll(tableWords);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 전체 페이지 탐색 중 실패: " + e.getMessage());
        }

        return allWords;
    }

    @Transactional // 트랜잭션 보장
    public String saveAllWordsToDb() {
        // 기존에 만들어둔 방식으로 노션 데이터 수집
        List<WordDto> dtoList = fetchAllWordsFromPage();

        if (dtoList.isEmpty()) {
            return "노션 페이지에서 가져올 단어가 데이터가 없습니다.";
        }

        int savedCount = 0;
        List<Word> saveList = new ArrayList<>();

        for (WordDto dto : dtoList) {
            // 선택 사항: 이미 DB에 존재하는 단어라면 스킵 (중복 방지)
            if (wordRepository.existsByWord(dto.getWord())) {
                continue;
            }

            // DTO를 엔티티 객체로 변환하여 리스트에 등록
            saveList.add(new Word(dto.getWord(), dto.getMeaning()));
            savedCount++;
        }

        // JPA의 saveAll을 이용하여 리스트 한 번에 Batch Insert 처리
        if (!saveList.isEmpty()) {
            wordRepository.saveAll(saveList);
        }

        return "성공적으로 DB에 저장 완료! 총 " + savedCount + "개의 새로운 단어가 등록되었습니다.";
    }

    private List<String> fetchChildBlockIdsByType(String parentId, String... targetTypes) {
        List<String> childIds = new ArrayList<>();
        try {
            String responseJson = webClient.get()
                    .uri("https://api.notion.com/v1/blocks/{block_id}/children", parentId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header("Notion-Version", notionVersion)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode results = rootNode.get("results");

            if (results != null && results.isArray()) {
                for (JsonNode block : results) {
                    String blockType = block.path("type").asText();
                    for (String targetType : targetTypes) {
                        if (targetType.equals(blockType)) {
                            childIds.add(block.path("id").asText());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 자식 블록 조회 실패 (부모ID: " + parentId + "): " + e.getMessage());
        }
        return childIds;
    }

    private List<WordDto> parseSimpleTable(String tableId) {
        List<WordDto> words = new ArrayList<>();
        try {
            // 표의 자식인 table_row 블록들을 가져옵니다.
            String responseJson = webClient.get()
                    .uri("https://api.notion.com/v1/blocks/{block_id}/children", tableId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header("Notion-Version", notionVersion)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode results = rootNode.get("results");

            if (results != null && results.isArray()) {
                boolean isHeaderRow = true;

                for (JsonNode rowBlock : results) {
                    if (!"table_row".equals(rowBlock.path("type").asText())) continue;

                    // cells: [ [word텍스트], [meaning텍스트], [추가암호], [추가정보] ] 구조
                    JsonNode cells = rowBlock.path("table_row").path("cells");
                    if (cells.isArray() && cells.size() >= 2) {

                        // 첫 번째 행은 헤더(word, meaning 제목줄)이므로 스킵합니다.
                        if (isHeaderRow) {
                            isHeaderRow = false;
                            continue;
                        }

                        // 첫 번째 셀(단어) 추출
                        String word = getPlainTextFromCell(cells.get(0));
                        // 두 번째 셀(뜻) 추출
                        String meaning = getPlainTextFromCell(cells.get(1));

                        if (!word.isEmpty()) {
                            words.add(new WordDto(word, meaning));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 단순 표 파싱 실패 (표ID: " + tableId + "): " + e.getMessage());
        }
        return words;
    }

    /**
     * 셀 배열 안에서 순수 텍스트만 추출하는 메서드
     */
    private String getPlainTextFromCell(JsonNode cellArray) {
        StringBuilder sb = new StringBuilder();
        if (cellArray.isArray()) {
            for (JsonNode textObj : cellArray) {
                sb.append(textObj.path("plain_text").asText());
            }
        }
        return sb.toString().trim();
    }
}