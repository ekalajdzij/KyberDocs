package com.kyberdocs.docs.audit;

import com.kyberdocs.docs.users.User;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logEvent(User user, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(new Timestamp(System.currentTimeMillis()));
        auditLogRepository.save(log);
    }
}
