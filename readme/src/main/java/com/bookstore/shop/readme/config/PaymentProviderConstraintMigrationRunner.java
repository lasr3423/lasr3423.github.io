package com.bookstore.shop.readme.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProviderConstraintMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                ALTER TABLE payment
                DROP CONSTRAINT IF EXISTS payment_payment_provider_check
                """);

        jdbcTemplate.execute("""
                ALTER TABLE payment
                ADD CONSTRAINT payment_payment_provider_check
                CHECK (
                    payment_provider IN (
                        'BANK_TRANSFER',
                        'TOSS',
                        'KAKAO',
                        'NAVER'
                    )
                )
                """);
    }
}
