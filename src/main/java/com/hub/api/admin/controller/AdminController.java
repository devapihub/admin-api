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

    @GetMapping("/public/hello")
    public String publicHello() {
        return "Hello fen !!!";
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
