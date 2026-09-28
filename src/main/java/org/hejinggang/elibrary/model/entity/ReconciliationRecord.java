package org.hejinggang.elibrary.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hejinggang.elibrary.common.enums.MismatchType;
import org.hejinggang.elibrary.common.enums.HandleStatus;
import org.hejinggang.elibrary.common.enums.PayChannel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账记录实体
 * 用于存储每笔订单的对账明细
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_reconciliation_record")
public class ReconciliationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "local_order_no", length = 64)
    private String localOrderNo;

    @Column(name = "platform_order_no", length = 128)
    private String platformOrderNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_channel", nullable = false, length = 32)
    private PayChannel payChannel;

    @Column(name = "local_amount", precision = 10, scale = 2)
    private BigDecimal localAmount;

    @Column(name = "platform_amount", precision = 10, scale = 2)
    private BigDecimal platformAmount;

    @Column(name = "local_status", length = 32)
    private String localStatus;

    @Column(name = "platform_status", length = 32)
    private String platformStatus;

    @Column(name = "local_paid_at")
    private LocalDateTime localPaidAt;

    @Column(name = "platform_paid_at")
    private LocalDateTime platformPaidAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "mismatch_type", length = 32)
    private MismatchType mismatchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "handle_status", nullable = false, length = 32)
    private HandleStatus handleStatus = HandleStatus.PENDING;

    @Column(name = "handle_remark", length = 512)
    private String handleRemark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}