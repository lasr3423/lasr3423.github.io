package com.bookstore.shop.readme.dto.request;

public record UpdateMemberRequest(
        String name,
        String phone,
        String address
) {}
