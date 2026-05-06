package com.hub.api.admin.security;

import com.hub.api.admin.entity.OAuth2StateDocument;
import com.hub.api.admin.repository.OAuth2StateRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

/**
 * Stores OAuth2 authorization requests in MongoDB keyed by the `state` parameter.
 * <p>
 * This avoids large (2 KB+) cookies that are unreliable behind nginx ingress in
 * Kubernetes environments. The OAuth2 `state` value is already included in Google's
 * callback URL, so it can be used directly to look up the saved request — no cookie needed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MongoOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final OAuth2StateRepository stateRepository;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null) {
            return null;
        }
        return stateRepository.findById(state)
                .map(doc -> {
                    log.debug("[OAuth2] loadAuthorizationRequest: found state={}", state);
                    return deserialize(doc.getSerializedRequest());
                })
                .orElseGet(() -> {
                    log.warn("[OAuth2] loadAuthorizationRequest: state not found in MongoDB: {}", state);
                    return null;
                });
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            String state = request.getParameter("state");
            if (state != null) {
                stateRepository.deleteById(state);
                log.debug("[OAuth2] saveAuthorizationRequest: deleted state={}", state);
            }
            return;
        }
        String state = authorizationRequest.getState();
        String serialized = serialize(authorizationRequest);
        stateRepository.save(new OAuth2StateDocument(state, serialized));
        log.info("[OAuth2] saveAuthorizationRequest: saved state={}, serializedLength={}", state, serialized.length());
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        String state = request.getParameter("state");
        if (state == null) {
            return null;
        }
        return stateRepository.findById(state)
                .map(doc -> {
                    stateRepository.deleteById(state);
                    log.debug("[OAuth2] removeAuthorizationRequest: removed state={}", state);
                    return deserialize(doc.getSerializedRequest());
                })
                .orElse(null);
    }

    private String serialize(OAuth2AuthorizationRequest request) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(request));
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
                    Base64.getUrlDecoder().decode(value));
        } catch (Exception e) {
            log.error("[OAuth2] deserialize failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }
}
