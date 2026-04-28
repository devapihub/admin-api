package com.hub.api.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "permissions")
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    public Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
