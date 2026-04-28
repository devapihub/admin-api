package com.hub.api.admin.repository;

import com.hub.api.admin.entity.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PermissionRepository extends MongoRepository<Permission, String> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);
}
