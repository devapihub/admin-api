package com.hub.api.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public String userProfile() {
        return "User: cần ROLE_USER hoặc ROLE_ADMIN";
    }

    @GetMapping("/admin/secret")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminSecret() {
        return "Admin: chỉ ROLE_ADMIN mới vào được";
    }

}
