package com.hub.api.admin.security;

import com.hub.api.admin.entity.Permission;
import com.hub.api.admin.entity.Role;
import com.hub.api.admin.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final String frontendRedirectUri;

    public OAuth2LoginSuccessHandler(
            JwtService jwtService,
            CustomOAuth2UserService customOAuth2UserService,
            @Value("${app.oauth2.frontend-redirect-uri}") String frontendRedirectUri) {
        this.jwtService = jwtService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("onAuthenticationSuccess called: principal={}, class={}, authorities={}",
                authentication.getPrincipal(),
                authentication.getPrincipal().getClass().getName(),
                authentication.getAuthorities());
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();

        User user;
        if (principal instanceof CustomOAuth2UserDetails details) {
            user = details.getUser();
        } else {
            Map<String, Object> attributes = principal.getAttributes();
            String providerId = (String) attributes.get("sub");
            String email = (String) attributes.get("email");
            user = customOAuth2UserService.findOrCreateUser(providerId, email, attributes);
        }

        log.info("OAuth2 login successful for user: {}", user.getEmail());

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .toList();

        Map<String, Object> extraClaims = Map.of("roles", roles, "permissions", permissions);
        String jwt = jwtService.generateToken(new CustomUserDetails(user), extraClaims);

        response.sendRedirect(frontendRedirectUri + "?token=" + jwt + "&username=" + user.getUsername());
    }
}
