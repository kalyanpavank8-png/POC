package com.dsar.controller;

import com.dsar.dto.UserInfoDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Allows the frontend to verify credentials and discover the user's roles
 * without needing any additional user-info endpoint.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * GET /api/auth/me
     * Returns the authenticated user's username and roles.
     * Used by the Vue app after login to determine which view to render.
     */
    @GetMapping("/me")
    public UserInfoDto me(Authentication auth) {
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new UserInfoDto(auth.getName(), roles);
    }
}
