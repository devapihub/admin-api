package com.hub.api.admin.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public OAuth2LoginFailureHandler(@Value("${app.oauth2.frontend-redirect-uri}") String frontendRedirectUri) {
        super(frontendRedirectUri + "?error=oauth2_failed");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 login failed: {}", exception.getMessage(), exception);
        super.onAuthenticationFailure(request, response, exception);
    }
}
