package com.hub.api.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
@Slf4j
public class AdminController {
    @GetMapping
    public Object hello() {
        return Map.of("status", "OK", "message", "Welcome Admin Service V2 Test 2 times");
    }

    @GetMapping("user")
    public Object getUser() {
        return Map.of("status", "OK",
                "data", Map.of("id", 1, "username", "Hugh")
        );
    }

}
