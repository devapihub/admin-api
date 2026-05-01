package com.hub.api.admin.security;

import com.hub.api.admin.entity.Permission;
import com.hub.api.admin.entity.Role;
import com.hub.api.admin.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CookieOAuth2AuthorizationRequestRepository cookieRepo;
    private final String frontendRedirectUri;

    public OAuth2LoginSuccessHandler(
            JwtService jwtService,
            CookieOAuth2AuthorizationRequestRepository cookieRepo,
            @Value("${app.oauth2.frontend-redirect-uri}") String frontendRedirectUri) {
        this.jwtService = jwtService;
        this.cookieRepo = cookieRepo;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2UserDetails oAuth2UserDetails =
                (CustomOAuth2UserDetails) authentication.getPrincipal();
        User user = oAuth2UserDetails.getUser();

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Object> extraClaims = Map.of("roles", roles, "permissions", permissions);
        String jwt = jwtService.generateToken(new CustomUserDetails(user), extraClaims);

        cookieRepo.removeAuthorizationRequest(request, response);

        response.sendRedirect(frontendRedirectUri + "?token=" + jwt);
    }
}
