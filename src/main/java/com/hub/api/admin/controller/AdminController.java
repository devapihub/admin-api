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
    @GetMapping("")
    public Object hello() {
        log.info("Admin endpoint accessed");
        return Map.of("status", "OK");
    }
}
