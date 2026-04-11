package com.bookstore.shop.readme.dto.request;

import java.util.List;

public record AdminOrderBulkStatusRequest(
        List<Long> orderIds,
        String status
) {
}
