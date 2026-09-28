package org.hejinggang.elibrary.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_type", nullable = false, length = 32)
    private String operatorType;

    @Column(name = "operation_type", nullable = false, length = 64)
    private String operationType;

    @Column(name = "operation_content", length = 1024)
    private String operationContent;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "target_type", length = 64)
    private String targetType;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "remark", length = 512)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}