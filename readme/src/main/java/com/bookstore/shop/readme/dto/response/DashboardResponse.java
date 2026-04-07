package com.bookstore.shop.readme.dto.response;

public record DashboardResponse(
        long totalMembers,
        long activeMembers,
        long totalProducts,
        long totalOrders,
        long pendingOrders
) {}
