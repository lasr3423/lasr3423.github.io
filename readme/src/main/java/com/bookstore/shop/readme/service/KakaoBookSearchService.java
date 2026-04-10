package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.dto.response.IsbnBookLookupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class KakaoBookSearchService {

    private final RestTemplate restTemplate;

    @Value("${kakao.book.base-url}")
    private String kakaoBookBaseUrl;

    @Value("${kakao.book.rest-api-key:}")
    private String restApiKey;

    public IsbnBookLookupResponse lookupByIsbn(String rawIsbn) {
        String isbn = normalizeIsbn(rawIsbn);
        if (!StringUtils.hasText(isbn)) {
            throw new IllegalArgumentException("ISBN을 입력해 주세요.");
        }

        Map<String, Object> document = searchSingleByIsbn(isbn);
        if (document == null) {
            throw new RuntimeException("해당 ISBN으로 도서를 찾지 못했습니다.");
        }

        return new IsbnBookLookupResponse(
                pickBestIsbn(document, isbn),
                text(document.get("title")),
                joinList(document.get("authors")),
                text(document.get("publisher")),
                text(document.get("thumbnail"))
        );
    }

    public KakaoBookSearchResult searchByTitle(String query, int page, int size) {
        if (!StringUtils.hasText(query)) {
            return new KakaoBookSearchResult(List.of(), true);
        }

        String requestUrl = UriComponentsBuilder.fromHttpUrl(kakaoBookBaseUrl)
                .queryParam("query", query.trim())
                .queryParam("target", "title")
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .encode()
                .toUriString();

        Map<String, Object> body = callKakaoApi(requestUrl);
        return new KakaoBookSearchResult(extractDocuments(body), isEnd(body));
    }

    private Map<String, Object> searchSingleByIsbn(String isbn) {
        String requestUrl = UriComponentsBuilder.fromHttpUrl(kakaoBookBaseUrl)
                .queryParam("query", isbn)
                .queryParam("target", "isbn")
                .queryParam("page", 1)
                .queryParam("size", 10)
                .build()
                .encode()
                .toUriString();

        List<Map<String, Object>> documents = extractDocuments(callKakaoApi(requestUrl));
        if (documents.isEmpty()) {
            return null;
        }

        for (Map<String, Object> document : documents) {
            String candidateIsbn = pickBestIsbn(document, "");
            if (isbn.equals(candidateIsbn)) {
                return document;
            }
        }
        return documents.get(0);
    }

    private Map<String, Object> callKakaoApi(String requestUrl) {
        if (!StringUtils.hasText(restApiKey)) {
            throw new IllegalStateException("카카오 도서 검색 REST API 키가 설정되지 않았습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + restApiKey);

        ResponseEntity<Map> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("카카오 도서 검색 응답이 비어 있습니다.");
        }
        return body;
    }

    private List<Map<String, Object>> extractDocuments(Map<String, Object> body) {
        Object documents = body.get("documents");
        if (documents instanceof List<?> rawList) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                }
            }
            return result;
        }
        return List.of();
    }

    private boolean isEnd(Map<String, Object> body) {
        Object meta = body.get("meta");
        if (meta instanceof Map<?, ?> metaMap) {
            Object isEnd = metaMap.get("is_end");
            if (isEnd instanceof Boolean value) {
                return value;
            }
        }
        return true;
    }

    private String pickBestIsbn(Map<String, Object> document, String fallback) {
        String raw = text(document.get("isbn"));
        if (!StringUtils.hasText(raw)) {
            return fallback;
        }

        String[] parts = raw.split("\\s+");
        String best = "";
        for (String part : parts) {
            String normalized = normalizeIsbn(part);
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            if (normalized.length() == 13) {
                return normalized;
            }
            if (!StringUtils.hasText(best)) {
                best = normalized;
            }
        }
        return StringUtils.hasText(best) ? best : fallback;
    }

    private String normalizeIsbn(String isbn) {
        if (!StringUtils.hasText(isbn)) {
            return "";
        }
        return isbn.replaceAll("[^0-9Xx]", "");
    }

    private String joinList(Object value) {
        if (value instanceof List<?> list && !list.isEmpty()) {
            return list.stream().map(String::valueOf).reduce((left, right) -> left + ", " + right).orElse(null);
        }
        return text(value);
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public record KakaoBookSearchResult(List<Map<String, Object>> documents, boolean isEnd) {
    }
}
