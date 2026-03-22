package com.dsar.dto;

import com.dsar.model.RequestStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Payload sent by an admin to update the status of a DSAR.
 */
public record UpdateStatusDto(

    @NotNull(message = "Status is required")
    RequestStatus status,

    String adminNotes   // optional free-text notes
) {}
