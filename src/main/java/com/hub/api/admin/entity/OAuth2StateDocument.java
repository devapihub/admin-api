package com.hub.api.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "oauth2_states")
public class OAuth2StateDocument {

    @Id
    private String state;

    private String serializedRequest;

    @Indexed(expireAfterSeconds = 300) // 5 minutes TTL
    private Date createdAt = new Date();

    public OAuth2StateDocument(String state, String serializedRequest) {
        this.state = state;
        this.serializedRequest = serializedRequest;
        this.createdAt = new Date();
    }
}
