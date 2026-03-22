package com.dsar.controller;

import com.dsar.dto.CreateRequestDto;
import com.dsar.dto.UpdateStatusDto;
import com.dsar.model.AuditLog;
import com.dsar.model.DsarRequest;
import com.dsar.service.DsarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for DSAR submission, retrieval, processing, and audit trail.
 *
 * Endpoints:
 *   POST   /api/requests              — Customer submits a request
 *   GET    /api/requests              — List requests (own for CUSTOMER, all for ADMIN)
 *   GET    /api/requests/{id}         — Get a single request
 *   PUT    /api/requests/{id}/status      — Admin processes a request
 *   PATCH  /api/requests/{id}/description — Customer edits a PENDING request
 *   GET    /api/audit                     — Admin views full audit trail
 *   GET    /api/audit/{requestId}         — Admin views audit for one request
 */
@RestController
@RequestMapping("/api")
public class DsarController {

    private final DsarService dsarService;

    public DsarController(DsarService dsarService) {
        this.dsarService = dsarService;
    }

    // ── Submit request ────────────────────────────────────────────────────────

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public DsarRequest submitRequest(@Valid @RequestBody CreateRequestDto dto,
                                     Authentication auth) {
        return dsarService.submitRequest(dto, auth.getName());
    }

    // ── List / get requests ───────────────────────────────────────────────────

    @GetMapping("/requests")
    public List<DsarRequest> listRequests(Authentication auth) {
        return dsarService.getRequests(auth.getName(), isAdmin(auth));
    }

    @GetMapping("/requests/{id}")
    public DsarRequest getRequest(@PathVariable String id, Authentication auth) {
        return dsarService.getRequest(id, auth.getName(), isAdmin(auth));
    }

    // ── Customer: edit description of a PENDING request ──────────────────────

    @PatchMapping("/requests/{id}/description")
    public DsarRequest updateDescription(@PathVariable String id,
                                         @RequestBody java.util.Map<String, String> body,
                                         Authentication auth) {
        return dsarService.updateDescription(id, body.get("description"), auth.getName());
    }

    // ── Admin: update status ──────────────────────────────────────────────────

    @PutMapping("/requests/{id}/status")
    public DsarRequest updateStatus(@PathVariable String id,
                                    @Valid @RequestBody UpdateStatusDto dto,
                                    Authentication auth) {
        return dsarService.updateStatus(id, dto, auth.getName());
    }

    // ── Admin: audit trail ────────────────────────────────────────────────────

    @GetMapping("/audit")
    public List<AuditLog> getAuditLog() {
        return dsarService.getAllAuditLogs();
    }

    @GetMapping("/audit/{requestId}")
    public List<AuditLog> getAuditLogForRequest(@PathVariable String requestId) {
        return dsarService.getAuditLogsForRequest(requestId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMIN"));
    }
}
