package com.kyberdocs.docs.audit;

import com.kyberdocs.docs.audit.dto.AuditLogDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogDto>> getAllAuditLogs() {
        List<AuditLog> logs = auditLogRepository.findAll();

        List<AuditLogDto> response = logs.stream()
                .map(AuditLogDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}