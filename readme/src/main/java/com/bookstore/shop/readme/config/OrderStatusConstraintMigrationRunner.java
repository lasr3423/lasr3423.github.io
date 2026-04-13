package com.bookstore.shop.readme.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusConstraintMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                ALTER TABLE "order"
                DROP CONSTRAINT IF EXISTS order_orderstatus_check
                """);

        jdbcTemplate.execute("""
                ALTER TABLE "order"
                ADD CONSTRAINT order_orderstatus_check
                CHECK (
                    order_status IN (
                        'PAYMENT_PENDING',
                        'PENDING',
                        'PAYED',
                        'APPROVAL',
                        'DELIVERING',
                        'DELIVERED',
                        'CANCELED'
                    )
                )
                """);
    }
}
