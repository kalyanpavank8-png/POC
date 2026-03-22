package com.dsar.service;

import com.dsar.dto.CreateRequestDto;
import com.dsar.dto.UpdateStatusDto;
import com.dsar.model.AuditLog;
import com.dsar.model.DsarRequest;
import com.dsar.model.RequestStatus;
import com.dsar.repository.InMemoryStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Business logic for DSAR lifecycle management and audit trail.
 */
@Service
public class DsarService {

    private final InMemoryStore store;

    public DsarService(InMemoryStore store) {
        this.store = store;
    }

    // ── Customer operations ───────────────────────────────────────────────────

    /**
     * A customer submits a new DSAR. Starts in PENDING status.
     */
    public DsarRequest submitRequest(CreateRequestDto dto, String customerId) {
        DsarRequest request = new DsarRequest(
                customerId,
                dto.customerEmail(),
                dto.requestType(),
                dto.description()
        );
        store.saveRequest(request);

        store.saveAuditLog(new AuditLog(
                request.getId(),
                "REQUEST_SUBMITTED",
                customerId,
                String.format("New %s request submitted by %s <%s>",
                        dto.requestType().getLabel(), customerId, dto.customerEmail())
        ));

        return request;
    }

    /**
     * Customers see only their own requests; admins see everything.
     */
    public List<DsarRequest> getRequests(String username, boolean isAdmin) {
        return isAdmin
                ? store.findAllRequests()
                : store.findRequestsByCustomer(username);
    }

    /**
     * Fetch a single request. Customers can only fetch their own.
     */
    public DsarRequest getRequest(String id, String username, boolean isAdmin) {
        DsarRequest request = store.findRequestById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "DSAR request not found: " + id));

        if (!isAdmin && !request.getCustomerId().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied to this request");
        }
        return request;
    }

    /**
     * Customer edits the description of their own PENDING request.
     * Not allowed once an admin has picked it up (IN_PROGRESS or beyond).
     */
    public DsarRequest updateDescription(String id, String newDescription, String customerId) {
        DsarRequest request = store.findRequestById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "DSAR request not found: " + id));

        if (!request.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied to this request");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only PENDING requests can be edited");
        }
        if (newDescription == null || newDescription.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Description cannot be blank");
        }

        request.setDescription(newDescription);
        store.saveRequest(request);

        store.saveAuditLog(new AuditLog(
                request.getId(),
                "DESCRIPTION_UPDATED",
                customerId,
                String.format("Request description updated by %s", customerId)
        ));

        return request;
    }

    // ── Admin operations ──────────────────────────────────────────────────────

    /**
     * Admin updates the status of a DSAR, optionally adding notes.
     */
    public DsarRequest updateStatus(String id, UpdateStatusDto dto, String adminUsername) {
        DsarRequest request = store.findRequestById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "DSAR request not found: " + id));

        RequestStatus previous = request.getStatus();

        // Guard: cannot move backward from terminal states
        if (previous == RequestStatus.COMPLETED || previous == RequestStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot update a request that is already " + previous);
        }

        request.setStatus(dto.status());
        if (dto.adminNotes() != null && !dto.adminNotes().isBlank()) {
            request.setAdminNotes(dto.adminNotes());
        }
        store.saveRequest(request);

        store.saveAuditLog(new AuditLog(
                request.getId(),
                "STATUS_UPDATED",
                adminUsername,
                String.format("Status changed from %s → %s by %s. Notes: %s",
                        previous, dto.status(), adminUsername,
                        dto.adminNotes() != null ? dto.adminNotes() : "—")
        ));

        return request;
    }

    // ── Audit Trail ───────────────────────────────────────────────────────────

    public List<AuditLog> getAllAuditLogs() {
        return store.findAllAuditLogs();
    }

    public List<AuditLog> getAuditLogsForRequest(String requestId) {
        return store.findAuditLogsByRequestId(requestId);
    }
}
