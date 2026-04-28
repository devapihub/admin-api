package com.hub.api.admin.controller;

import com.hub.api.admin.dto.AssignPermissionsRequest;
import com.hub.api.admin.dto.CreateRoleRequest;
import com.hub.api.admin.dto.PermissionDto;
import com.hub.api.admin.dto.RoleDto;
import com.hub.api.admin.entity.Permission;
import com.hub.api.admin.entity.Role;
import com.hub.api.admin.repository.PermissionRepository;
import com.hub.api.admin.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleController(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    public List<RoleDto> listRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody CreateRoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            return ResponseEntity.badRequest().body("Role already exists: " + request.getName());
        }
        Role saved = roleRepository.save(new Role(request.getName()));
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable String id) {
        if (!roleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        roleRepository.deleteById(id);
        return ResponseEntity.ok("Role deleted");
    }

    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<?> assignPermissions(
            @PathVariable String roleId,
            @RequestBody AssignPermissionsRequest request) {

        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
        if (permissions.size() != request.getPermissionIds().size()) {
            return ResponseEntity.badRequest().body("One or more permission IDs not found");
        }

        Role role = roleOpt.get();
        role.getPermissions().addAll(permissions);
        Role saved = roleRepository.save(role);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<?> removePermission(
            @PathVariable String roleId,
            @PathVariable String permissionId) {

        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Role role = roleOpt.get();
        boolean removed = role.getPermissions().removeIf(p -> p.getId().equals(permissionId));
        if (!removed) {
            return ResponseEntity.badRequest().body("Permission not assigned to this role");
        }

        roleRepository.save(role);
        return ResponseEntity.ok("Permission removed from role");
    }

    private RoleDto toDto(Role role) {
        List<PermissionDto> permDtos = role.getPermissions().stream()
                .map(p -> new PermissionDto(p.getId(), p.getName(), p.getDescription()))
                .toList();
        return new RoleDto(role.getId(), role.getName(), permDtos);
    }
}
