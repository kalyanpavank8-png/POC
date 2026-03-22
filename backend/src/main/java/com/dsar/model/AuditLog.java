package com.dsar.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit trail entry.
 * Every state transition and request submission creates one of these.
 */
public class AuditLog {

    private final String id;
    private final String requestId;
    private final String action;        // e.g. "REQUEST_SUBMITTED", "STATUS_UPDATED"
    private final String performedBy;   // username
    private final String details;       // human-readable summary
    private final LocalDateTime timestamp;

    public AuditLog(String requestId, String action, String performedBy, String details) {
        this.id          = UUID.randomUUID().toString();
        this.requestId   = requestId;
        this.action      = action;
        this.performedBy = performedBy;
        this.details     = details;
        this.timestamp   = LocalDateTime.now();
    }

    public String getId()          { return id; }
    public String getRequestId()   { return requestId; }
    public String getAction()      { return action; }
    public String getPerformedBy() { return performedBy; }
    public String getDetails()     { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
