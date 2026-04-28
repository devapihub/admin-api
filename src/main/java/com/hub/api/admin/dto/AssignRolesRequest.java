package com.hub.api.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignRolesRequest {
    private List<String> roleIds;
}
