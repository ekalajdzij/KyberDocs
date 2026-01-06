package com.kyberdocs.docs.audit.dto;
import com.kyberdocs.docs.audit.AuditLog;

import java.sql.Timestamp;

public class AuditLogDto {
    private Long id;
    private String action;
    private String details;
    private Timestamp timestamp;
    private String username;
    private String email;

    public AuditLogDto(AuditLog log) {
        this.id = log.getId();
        this.action = log.getAction();
        this.details = log.getDetails();
        this.timestamp = log.getTimestamp();

        if (log.getUser() != null) {
            this.username = log.getUser().getUsername();
            this.email = log.getUser().getEmail();
        } else {
            this.username = "Unknown/Deleted";
            this.email = "-";
        }
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}