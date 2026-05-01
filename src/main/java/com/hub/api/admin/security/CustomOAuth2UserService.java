package com.hub.api.admin.security;

import com.hub.api.admin.entity.Role;
import com.hub.api.admin.entity.User;
import com.hub.api.admin.repository.RoleRepository;
import com.hub.api.admin.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");

        User user = userRepository.findByProviderAndProviderId("google", providerId)
                .orElseGet(() -> findOrCreateUser(providerId, email, attributes));

        return new CustomOAuth2UserDetails(user, attributes);
    }

    private User findOrCreateUser(String providerId, String email, Map<String, Object> attributes) {
        // Link tới existing local account nếu email trùng
        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setProvider("google");
                    existingUser.setProviderId(providerId);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> createNewUser(providerId, email));
    }

    private User createNewUser(String providerId, String email) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        User newUser = new User();
        newUser.setUsername(email);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setProvider("google");
        newUser.setProviderId(providerId);
        newUser.setRoles(Set.of(userRole));

        return userRepository.save(newUser);
    }
}
