package com.dsar.dto;

import java.util.List;

/**
 * Returned by /api/auth/me so the frontend can discover the current user's role.
 */
public record UserInfoDto(
    String username,
    List<String> roles
) {}
