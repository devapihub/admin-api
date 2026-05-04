package com.hub.api.admin.controller;

import com.hub.api.admin.dto.UserDto;
import com.hub.api.admin.entity.Permission;
import com.hub.api.admin.entity.Role;
import com.hub.api.admin.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@RestController
@RequestMapping("/")
@Slf4j
public class AdminController {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "JWT_SECRET", "MONGODB_URI", "GOOGLE_CLIENT_SECRET",
            "PASSWORD", "SECRET", "TOKEN", "KEY", "CREDENTIAL"
    );

    @GetMapping("/public/hello")
    public String publicHello() {
        return "Hello fen !!!";
    }

    @GetMapping("/public/env")
    public Map<String, String> env() {
        Map<String, String> result = new TreeMap<>();
        System.getenv().forEach((key, value) -> {
            boolean sensitive = SENSITIVE_KEYS.stream()
                    .anyMatch(s -> key.toUpperCase().contains(s));
            result.put(key, sensitive ? mask(value) : value);
        });
        return result;
    }

    private String mask(String value) {
        if (value == null || value.length() <= 4) return "****";
        return value.substring(0, 4) + "*".repeat(value.length() - 4);
    }

    @GetMapping("/user/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserDto userProfile(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        var user = principal.getUser();
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .toList();

        return new UserDto(user.getId(), user.getUsername(), roles, permissions);
    }

    @GetMapping("/admin/secret")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminSecret() {
        return "Admin: chỉ ROLE_ADMIN mới vào được";
    }

}
