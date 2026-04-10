package com.bookstore.shop.readme.dto.response;

public record FileUploadResponse(
        String storedPath,
        String accessUrl,
        String originalFilename,
        long size
) {
}
