package com.example.demo.domain;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class ApiKeyAuthentication extends AbstractAuthenticationToken {
    private final String username;
    private final boolean userEnabled;

    public ApiKeyAuthentication(String username, boolean userEnabled,
                                Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.username = username;
        this.userEnabled = userEnabled;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }
}
