package com.bookstore.shop.readme.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;

public record AdminQnAAnswerRequest(
        String title,
        @JsonAlias("answer") String content
) {}
