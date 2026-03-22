package com.dsar.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core domain object representing a Data Subject Access Request.
 * Stored entirely in-memory via InMemoryStore.
 */
public class DsarRequest {

    private final String id;
    private final String customerId;       // username of the submitting customer
    private final String customerEmail;    // provided at submission time
    private final RequestType requestType;
    private String description;
    private RequestStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String adminNotes;

    public DsarRequest(String customerId, String customerEmail,
                       RequestType requestType, String description) {
        this.id           = UUID.randomUUID().toString();
        this.customerId   = customerId;
        this.customerEmail = customerEmail;
        this.requestType  = requestType;
        this.description  = description;
        this.status       = RequestStatus.PENDING;
        this.createdAt    = LocalDateTime.now();
        this.updatedAt    = LocalDateTime.now();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()            { return id; }
    public String getCustomerId()    { return customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public RequestType getRequestType() { return requestType; }
    public String getDescription()   { return description; }
    public RequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getAdminNotes()    { return adminNotes; }

    // ── Setters (mutable fields only) ────────────────────────────────────────

    public void setStatus(RequestStatus status) {
        this.status    = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
        this.updatedAt  = LocalDateTime.now();
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt   = LocalDateTime.now();
    }
}
