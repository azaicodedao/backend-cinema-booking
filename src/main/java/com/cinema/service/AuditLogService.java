package com.cinema.service;

import com.cinema.entity.AuditLog;
import com.cinema.repository.AuditLogRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogService {

    AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(String action, Integer adminId, Integer targetUserId, String oldValue, String newValue, String description) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .targetType("USER")
                .targetId(String.valueOf(targetUserId))
                .description(description + " (Changed from " + oldValue + " to " + newValue + ")")
                .build();
        
        auditLogRepository.save(log);
    }
}
