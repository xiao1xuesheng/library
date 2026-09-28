package org.hejinggang.elibrary.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 对账报告实体
 * 用于存储每日对账汇总信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_reconciliation_report")
public class ReconciliationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    @Column(name = "local_order_count", nullable = false)
    private Integer localOrderCount = 0;

    @Column(name = "platform_order_count", nullable = false)
    private Integer platformOrderCount = 0;

    @Column(name = "matched_count", nullable = false)
    private Integer matchedCount = 0;

    @Column(name = "mismatch_count", nullable = false)
    private Integer mismatchCount = 0;

    @Column(name = "mismatch_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal mismatchAmount = BigDecimal.ZERO;

    @Column(name = "mismatch_types", length = 256)
    private String mismatchTypes;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "PENDING";

    @Column(name = "report_data", columnDefinition = "JSON")
    private String reportData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}