package com.hub.api.admin.repository;

import com.hub.api.admin.entity.OAuth2StateDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OAuth2StateRepository extends MongoRepository<OAuth2StateDocument, String> {
}
