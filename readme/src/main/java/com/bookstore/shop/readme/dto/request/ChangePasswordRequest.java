package com.bookstore.shop.readme.dto.request;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {}
