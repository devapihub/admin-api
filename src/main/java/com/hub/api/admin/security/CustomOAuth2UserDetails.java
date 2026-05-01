package com.hub.api.admin.security;

import com.hub.api.admin.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomOAuth2UserDetails implements OAuth2User {

    @Getter
    private final User user;
    private final Map<String, Object> attributes;

    public CustomOAuth2UserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .flatMap(role -> {
                    Stream<SimpleGrantedAuthority> roleAuthority =
                            Stream.of(new SimpleGrantedAuthority(role.getName()));
                    Stream<SimpleGrantedAuthority> permissionAuthorities =
                            role.getPermissions().stream()
                                    .map(p -> new SimpleGrantedAuthority(p.getName()));
                    return Stream.concat(roleAuthority, permissionAuthorities);
                })
                .collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return user.getProviderId();
    }

}
