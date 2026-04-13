package com.bookstore.shop.readme.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentRefundColumnsMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                ALTER TABLE payment
                ADD COLUMN IF NOT EXISTS refunded_amount INTEGER
                """);

        jdbcTemplate.execute("""
                ALTER TABLE payment
                ADD COLUMN IF NOT EXISTS return_fee INTEGER
                """);
    }
}
