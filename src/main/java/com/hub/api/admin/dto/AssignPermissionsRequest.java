package com.hub.api.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionsRequest {
    private List<String> permissionIds;
}
