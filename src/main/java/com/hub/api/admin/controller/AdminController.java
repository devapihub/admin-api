package com.hub.api.admin.controller;

import com.hub.api.admin.dto.UserDto;
import com.hub.api.admin.entity.Role;
import com.hub.api.admin.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
@Slf4j
public class AdminController {
    @GetMapping("/public/hello")
    public String publicHello() {
        return "Public: không cần token";
    }

    @GetMapping("/user/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserDto userProfile(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        var user = principal.getUser();
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        return new UserDto(
                user.getId(),
                user.getUsername(),
                roles
        );
    }

    @GetMapping("/admin/secret")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminSecret() {
        return "Admin: chỉ ROLE_ADMIN mới vào được";
    }

}
