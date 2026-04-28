package com.hub.api.admin.controller;

import com.hub.api.admin.dto.AssignRolesRequest;
import com.hub.api.admin.dto.UserDto;
import com.hub.api.admin.entity.Permission;
import com.hub.api.admin.entity.Role;
import com.hub.api.admin.entity.User;
import com.hub.api.admin.repository.RoleRepository;
import com.hub.api.admin.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public List<UserDto> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<?> assignRoles(
            @PathVariable String userId,
            @RequestBody AssignRolesRequest request) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Role> roles = roleRepository.findAllById(request.getRoleIds());
        if (roles.size() != request.getRoleIds().size()) {
            return ResponseEntity.badRequest().body("One or more role IDs not found");
        }

        User user = userOpt.get();
        user.getRoles().addAll(roles);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<?> removeRole(
            @PathVariable String userId,
            @PathVariable String roleId) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        boolean removed = user.getRoles().removeIf(r -> r.getId().equals(roleId));
        if (!removed) {
            return ResponseEntity.badRequest().body("Role not assigned to this user");
        }

        userRepository.save(user);
        return ResponseEntity.ok("Role removed from user");
    }

    private UserDto toDto(User user) {
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
}
