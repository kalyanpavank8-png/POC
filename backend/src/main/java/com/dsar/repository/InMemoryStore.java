package com.dsar.repository;

import com.dsar.model.AuditLog;
import com.dsar.model.DsarRequest;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Thread-safe, in-memory data store.
 * No external database required — fulfils the "in-memory datastore" constraint.
 */
@Repository
public class InMemoryStore {

    // ── DSAR Requests ────────────────────────────────────────────────────────

    private final Map<String, DsarRequest> requests = new ConcurrentHashMap<>();

    public DsarRequest saveRequest(DsarRequest request) {
        requests.put(request.getId(), request);
        return request;
    }

    public Optional<DsarRequest> findRequestById(String id) {
        return Optional.ofNullable(requests.get(id));
    }

    /** Returns all requests, newest first. */
    public List<DsarRequest> findAllRequests() {
        return requests.values().stream()
                .sorted(Comparator.comparing(DsarRequest::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /** Returns all requests belonging to a specific customer, newest first. */
    public List<DsarRequest> findRequestsByCustomer(String customerId) {
        return requests.values().stream()
                .filter(r -> r.getCustomerId().equals(customerId))
                .sorted(Comparator.comparing(DsarRequest::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    // ── Audit Logs ───────────────────────────────────────────────────────────

    private final List<AuditLog> auditLogs = new CopyOnWriteArrayList<>();

    public void saveAuditLog(AuditLog log) {
        auditLogs.add(log);
    }

    /** Returns all audit entries, newest first. */
    public List<AuditLog> findAllAuditLogs() {
        List<AuditLog> sorted = new ArrayList<>(auditLogs);
        sorted.sort(Comparator.comparing(AuditLog::getTimestamp).reversed());
        return sorted;
    }

    /** Returns audit entries for a specific DSAR request, newest first. */
    public List<AuditLog> findAuditLogsByRequestId(String requestId) {
        return auditLogs.stream()
                .filter(l -> l.getRequestId().equals(requestId))
                .sorted(Comparator.comparing(AuditLog::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
}
