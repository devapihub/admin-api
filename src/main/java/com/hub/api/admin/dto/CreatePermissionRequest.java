package com.hub.api.admin.dto;

import lombok.Data;

@Data
public class CreatePermissionRequest {
    private String name;
    private String description;
}
