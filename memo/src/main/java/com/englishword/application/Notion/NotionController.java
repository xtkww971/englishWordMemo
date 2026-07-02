package com.englishword.application.Notion;

import com.englishword.application.domain.Word.dto.WordDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/notion")
public class NotionController {

    private final NotionService notionService;

    @Value("${notion.client-id}")
    private String clientId;

    @Value("${notion.redirect-uri}")
    private String redirectUri;

    public NotionController(NotionService notionService) {
        this.notionService = notionService;
    }

    @GetMapping("/data-test")
    public List<WordDto> fetchDatabaseData() {
        notionService.saveAllWordsToDb();
        return notionService.fetchAllWordsFromPage();
    }
}