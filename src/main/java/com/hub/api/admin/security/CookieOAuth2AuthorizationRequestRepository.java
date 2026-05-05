package com.hub.api.admin.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_MAX_AGE = 180; // 3 minutes

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        java.util.Optional<String> cookieValue = getCookieValue(request, COOKIE_NAME);
        if (cookieValue.isEmpty()) {
            String cookieNames = request.getCookies() == null ? "none" :
                    Arrays.stream(request.getCookies()).map(Cookie::getName).toList().toString();
            log.warn("[OAuth2] loadAuthorizationRequest: cookie '{}' NOT found. uri={}, presentCookies={}",
                    COOKIE_NAME, request.getRequestURI(), cookieNames);
            return null;
        }
        log.debug("[OAuth2] loadAuthorizationRequest: cookie found, valueLength={}", cookieValue.get().length());
        OAuth2AuthorizationRequest authRequest = deserialize(cookieValue.get());
        if (authRequest == null) {
            log.warn("[OAuth2] loadAuthorizationRequest: cookie present but deserialization returned null");
        }
        return authRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            log.warn("[OAuth2] saveAuthorizationRequest: null authorizationRequest, deleting cookie");
            deleteCookie(request, response);
            return;
        }
        String serialized = serialize(authorizationRequest);
        boolean secure = isSecureRequest(request);
        // SameSite=None requires Secure=true (HTTPS). For local HTTP dev, fall back to Lax.
        // OAuth2 callbacks come from Google (cross-site redirect), so Lax blocks them in modern browsers.
        String sameSite = secure ? "None" : "Lax";
        log.info("[OAuth2] saveAuthorizationRequest: setting cookie {}, valueLength={}, secure={}, sameSite={}, state={}",
                authorizationRequest.getRedirectUri(), serialized.length(), secure, sameSite, authorizationRequest.getState());
        Cookie cookie = new Cookie(COOKIE_NAME, serialized);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        deleteCookie(request, response);
        return authRequest;
    }

    private java.util.Optional<String> getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return java.util.Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst();
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        return request.isSecure()
                || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
        boolean secure = isSecureRequest(request);
        String sameSite = secure ? "None" : "Lax";
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
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
