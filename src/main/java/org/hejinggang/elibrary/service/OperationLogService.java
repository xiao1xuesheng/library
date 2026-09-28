package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.config.AuthContext;
import org.hejinggang.elibrary.model.entity.OperationLog;
import org.hejinggang.elibrary.repository.OperationLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    public OperationLogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    public void logUserOperation(Long userId, String operationType, String operationContent) {
        logUserOperation(userId, operationType, operationContent, null, null);
    }

    public void logUserOperation(Long userId, String operationType, String operationContent, 
                                  Long targetId, String targetType) {
        OperationLog log = new OperationLog();
        log.setOperatorId(userId);
        log.setOperatorType("USER");
        log.setOperationType(operationType);
        log.setOperationContent(operationContent);
        log.setTargetId(targetId);
        log.setTargetType(targetType);
        log.setTraceId(AuthContext.getTraceId());
        operationLogRepository.save(log);
    }

    public void logAdminOperation(Long adminId, String operationType, String operationContent) {
        logAdminOperation(adminId, operationType, operationContent, null, null);
    }

    public void logAdminOperation(Long adminId, String operationType, String operationContent,
                                   Long targetId, String targetType) {
        OperationLog log = new OperationLog();
        log.setOperatorId(adminId);
        log.setOperatorType("ADMIN");
        log.setOperationType(operationType);
        log.setOperationContent(operationContent);
        log.setTargetId(targetId);
        log.setTargetType(targetType);
        log.setTraceId(AuthContext.getTraceId());
        operationLogRepository.save(log);
    }

    public Page<OperationLog> getAdminLogs(Long adminId, Pageable pageable) {
        return operationLogRepository.findByOperatorId(adminId, pageable);
    }

    public Page<OperationLog> getAllLogs(Pageable pageable) {
        return operationLogRepository.findAll(pageable);
    }
}