package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.dto.response.IsbnBookLookupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class IsbnOpenApiService {

    private final RestTemplate restTemplate;

    @Value("${isbn.api.base-url}")
    private String isbnApiBaseUrl;

    @Value("${isbn.api.cert-key:}")
    private String certKey;

    public IsbnBookLookupResponse lookupByIsbn(String rawIsbn) {
        String isbn = normalizeIsbn(rawIsbn);
        if (!StringUtils.hasText(isbn)) {
            throw new IllegalArgumentException("ISBN을 입력해 주세요.");
        }
        if (!StringUtils.hasText(certKey)) {
            throw new IllegalStateException("ISBN Open API 인증키가 설정되지 않았습니다.");
        }

        String requestUrl = UriComponentsBuilder.fromHttpUrl(isbnApiBaseUrl)
                .queryParam("cert_key", certKey)
                .queryParam("result_style", "json")
                .queryParam("page_no", 1)
                .queryParam("page_size", 1)
                .queryParam("isbn", isbn)
                .build(true)
                .toUriString();

        ResponseEntity<Map> response = restTemplate.getForEntity(requestUrl, Map.class);
        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("ISBN Open API 응답이 비어 있습니다.");
        }

        List<Map<String, Object>> docs = extractDocs(body);
        if (docs.isEmpty()) {
            throw new RuntimeException("해당 ISBN의 도서 정보를 찾지 못했습니다.");
        }

        Map<String, Object> book = docs.get(0);
        return new IsbnBookLookupResponse(
                textOrDefault(book.get("EA_ISBN"), isbn),
                text(book.get("TITLE")),
                text(book.get("AUTHOR")),
                text(book.get("PUBLISHER")),
                text(book.get("TITLE_URL"))
        );
    }

    private List<Map<String, Object>> extractDocs(Map<String, Object> body) {
        Object result = body.get("result");
        if (result instanceof List<?> resultList && !resultList.isEmpty()) {
            Object first = resultList.get(0);
            if (first instanceof Map<?, ?> firstMap) {
                Object docs = firstMap.get("docs");
                if (docs instanceof List<?>) {
                    return (List<Map<String, Object>>) docs;
                }
            }
        }

        Object docs = body.get("docs");
        if (docs instanceof List<?>) {
            return (List<Map<String, Object>>) docs;
        }

        return List.of();
    }

    private String normalizeIsbn(String isbn) {
        if (!StringUtils.hasText(isbn)) {
            return "";
        }
        return isbn.replaceAll("[^0-9Xx]", "");
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String textOrDefault(Object value, String fallback) {
        String text = text(value);
        return StringUtils.hasText(text) ? text : fallback;
    }
}
