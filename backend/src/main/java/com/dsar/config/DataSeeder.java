package com.dsar.config;

import com.dsar.model.AuditLog;
import com.dsar.model.DsarRequest;
import com.dsar.model.RequestStatus;
import com.dsar.model.RequestType;
import com.dsar.repository.InMemoryStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds realistic demo DSAR requests on startup so the portal looks
 * populated immediately — no manual data entry needed for demos.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final InMemoryStore store;

    public DataSeeder(InMemoryStore store) {
        this.store = store;
    }

    @Override
    public void run(String... args) {

        // ── Pavan's requests ──────────────────────────────────────────────

        // 1. Completed ACCESS request
        DsarRequest r1 = new DsarRequest(
                "Pavan", "pavan@example.com",
                RequestType.ACCESS,
                "I would like a full copy of all personal data you hold on me, " +
                "including purchase history, browsing data, and any third-party sharing."
        );
        r1.setStatus(RequestStatus.COMPLETED);
        r1.setAdminNotes("Data export emailed to pavan@example.com on 2026-03-10. " +
                         "Includes account info, order history, and marketing preferences.");
        store.saveRequest(r1);
        store.saveAuditLog(new AuditLog(r1.getId(), "REQUEST_SUBMITTED", "Pavan",
                "ACCESS request submitted by Pavan <pavan@example.com>"));
        store.saveAuditLog(new AuditLog(r1.getId(), "STATUS_UPDATED", "Bhargava",
                "Status changed from PENDING → IN_PROGRESS by Bhargava. Notes: —"));
        store.saveAuditLog(new AuditLog(r1.getId(), "STATUS_UPDATED", "Bhargava",
                "Status changed from IN_PROGRESS → COMPLETED by Bhargava. " +
                "Notes: Data export emailed to pavan@example.com on 2026-03-10."));

        // 2. Pending CORRECT request
        DsarRequest r2 = new DsarRequest(
                "Pavan", "pavan@example.com",
                RequestType.CORRECT,
                "My date of birth in your system is incorrect — it shows 1990-05-15 " +
                "but the correct date is 1992-05-15. Please update your records."
        );
        store.saveRequest(r2);
        store.saveAuditLog(new AuditLog(r2.getId(), "REQUEST_SUBMITTED", "Pavan",
                "CORRECT request submitted by Pavan <pavan@example.com>"));

        // 3. In-progress DELETE request
        DsarRequest r3 = new DsarRequest(
                "Pavan", "pavan@example.com",
                RequestType.DELETE,
                "Please permanently delete all personal data associated with my account, " +
                "including backups, analytics data, and any data shared with partners."
        );
        r3.setStatus(RequestStatus.IN_PROGRESS);
        r3.setAdminNotes("Deletion in progress. Partner data removal being coordinated.");
        store.saveRequest(r3);
        store.saveAuditLog(new AuditLog(r3.getId(), "REQUEST_SUBMITTED", "Pavan",
                "DELETE request submitted by Pavan <pavan@example.com>"));
        store.saveAuditLog(new AuditLog(r3.getId(), "STATUS_UPDATED", "Bhargava",
                "Status changed from PENDING → IN_PROGRESS by Bhargava. " +
                "Notes: Deletion in progress. Partner data removal being coordinated."));

        // ── Nagaraju's requests ───────────────────────────────────────────

        // 4. Rejected request
        DsarRequest r4 = new DsarRequest(
                "Nagaraju", "nagaraju@example.com",
                RequestType.DELETE,
                "Delete my account and all associated data immediately."
        );
        r4.setStatus(RequestStatus.REJECTED);
        r4.setAdminNotes("Request rejected: active subscription detected. " +
                         "Please cancel your subscription first, then resubmit.");
        store.saveRequest(r4);
        store.saveAuditLog(new AuditLog(r4.getId(), "REQUEST_SUBMITTED", "Nagaraju",
                "DELETE request submitted by Nagaraju <nagaraju@example.com>"));
        store.saveAuditLog(new AuditLog(r4.getId(), "STATUS_UPDATED", "Bhargava",
                "Status changed from PENDING → REJECTED by Bhargava. " +
                "Notes: Active subscription detected."));

        // 5. Pending ACCESS request from Nagaraju
        DsarRequest r5 = new DsarRequest(
                "Nagaraju", "nagaraju@example.com",
                RequestType.ACCESS,
                "Please provide all data you hold about me including what is " +
                "shared with advertising networks."
        );
        store.saveRequest(r5);
        store.saveAuditLog(new AuditLog(r5.getId(), "REQUEST_SUBMITTED", "Nagaraju",
                "ACCESS request submitted by Nagaraju <nagaraju@example.com>"));

        // ── Krishna's requests ────────────────────────────────────────────

        DsarRequest r6 = new DsarRequest(
                "Krishna", "krishna@example.com",
                RequestType.CORRECT,
                "My email address on file is outdated. Please update it from " +
                "krishna.old@example.com to krishna@example.com."
        );
        store.saveRequest(r6);
        store.saveAuditLog(new AuditLog(r6.getId(), "REQUEST_SUBMITTED", "Krishna",
                "CORRECT request submitted by Krishna <krishna@example.com>"));
    }
}
