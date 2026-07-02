package com.englishword.application.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class NotionConfig {

    @Value("${notion.client-secret}")
    private String notionSecret;

    @Value("${notion.version}")
    private String notionVersion;

    @Bean
    public WebClient notionWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.notion.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + notionSecret)
                .defaultHeader("Notion-Version", notionVersion)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }
}