package com.hub.api.admin.controller;

import com.hub.api.admin.dto.CreatePermissionRequest;
import com.hub.api.admin.dto.PermissionDto;
import com.hub.api.admin.entity.Permission;
import com.hub.api.admin.repository.PermissionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/permissions")
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionRepository permissionRepository;

    public PermissionController(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionDto(p.getId(), p.getName(), p.getDescription()))
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> createPermission(@RequestBody CreatePermissionRequest request) {
        if (permissionRepository.existsByName(request.getName())) {
            return ResponseEntity.badRequest().body("Permission already exists: " + request.getName());
        }
        Permission saved = permissionRepository.save(
                new Permission(request.getName(), request.getDescription())
        );
        return ResponseEntity.ok(new PermissionDto(saved.getId(), saved.getName(), saved.getDescription()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePermission(@PathVariable String id) {
        if (!permissionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        permissionRepository.deleteById(id);
        return ResponseEntity.ok("Permission deleted");
    }
}
