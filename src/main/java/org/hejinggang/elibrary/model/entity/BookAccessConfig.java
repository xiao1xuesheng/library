package org.hejinggang.elibrary.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_book_access_config")
public class BookAccessConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false, unique = true)
    private Long bookId;

    @Column(name = "access_type", nullable = false, length = 32)
    private String accessType = "PUBLIC_FREE";

    @Column(name = "daily_price", nullable = false)
    private BigDecimal dailyPrice = BigDecimal.ZERO;

    @Column(name = "vip_discount", nullable = false)
    private BigDecimal vipDiscount = new BigDecimal("1.00");

    @Column(name = "max_renew_times", nullable = false)
    private Integer maxRenewTimes = 2;

    @Column(name = "renew_days_free", nullable = false)
    private Integer renewDaysFree = 7;

    @Column(name = "renew_days_vip", nullable = false)
    private Integer renewDaysVip = 15;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}