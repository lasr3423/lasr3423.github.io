package com.bookstore.shop.readme.dto.response;

public record IsbnBookLookupResponse(
        String isbn,
        String title,
        String author,
        String publisher,
        String thumbnail
) {
}
