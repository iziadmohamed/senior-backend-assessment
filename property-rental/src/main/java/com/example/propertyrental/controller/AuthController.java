package com.example.propertyrental.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @GetMapping("/whoami")
    public Map<String, Object> whoami(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "userId", jwt.getSubject(),
            "username", jwt.getClaimAsString("preferred_username"),
            "email", jwt.getClaimAsString("email"),
            "roles", jwt.getClaimAsMap("realm_access").get("roles")
        );
    }
}