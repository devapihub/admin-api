package com.hub.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private String id;
    private String name;
    private List<PermissionDto> permissions;
}
