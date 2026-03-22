package com.dsar.dto;

import com.dsar.model.RequestType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload sent by a customer to submit a new DSAR.
 */
public record CreateRequestDto(

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    String customerEmail,

    @NotNull(message = "Request type is required")
    RequestType requestType,

    @NotBlank(message = "Description is required")
    String description
) {}
