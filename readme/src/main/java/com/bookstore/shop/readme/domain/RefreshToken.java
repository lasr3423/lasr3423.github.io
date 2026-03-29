package com.bookstore.shop.readme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken extends BaseEntity {

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static RefreshToken create(Long memberId, String token, long expirationMs) {
        RefreshToken rt = new RefreshToken();
        rt.memberId  = memberId;
        rt.token     = token;
        rt.expiresAt = LocalDateTime.now().plusNanos(expirationMs * 1_000_000L);
        return rt;
    }

    /* Rotation: 토큰 교체 */
    public void rotate(String newToken, long expirationMs) {
        this.token     = newToken;
        this.expiresAt = LocalDateTime.now().plusNanos(expirationMs * 1_000_000L);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
