package org.hejinggang.elibrary.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hejinggang.elibrary.common.enums.PayChannel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支付平台账单记录实体
 * 存储从支付平台下载的对账文件解析后的数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_platform_bill_record")
public class PlatformBillRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_id", nullable = false, length = 64)
    private String uploadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_channel", nullable = false, length = 32)
    private PayChannel payChannel;

    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @Column(name = "platform_order_no", nullable = false, length = 128)
    private String platformOrderNo;

    @Column(name = "local_order_no", length = 64)
    private String localOrderNo;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "raw_data", columnDefinition = "JSON")
    private String rawData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}