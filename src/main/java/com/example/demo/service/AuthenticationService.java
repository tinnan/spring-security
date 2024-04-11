package com.example.demo.service;

import com.example.demo.dao.ApiKey;
import com.example.demo.dao.ApiKeyRepository;
import com.example.demo.dao.UserApiKeyRepository;
import com.example.demo.domain.ApiKeyAuthentication;
import com.example.demo.domain.UserApiKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {
    private static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";

    private final ApiKeyRepository authenticationRepository;
    private final UserApiKeyRepository userApiKeyRepository;

    @Transactional
    public List<UserApiKey> createApiKey(String username) {
        List<UserApiKey> apiKeys = userApiKeyRepository.findAllByUsername(username);
        for (UserApiKey apiKey : apiKeys) {
            if (apiKey.isActive()) {
                // There can be only 1 active API key per username.
                apiKey.setActive(false);
            }
        }
        String apiKey = UUID.randomUUID()
                .toString();
        UserApiKey newApiKey = new UserApiKey(apiKey, username, true);
        userApiKeyRepository.save(newApiKey);
        apiKeys.add(newApiKey);
        return apiKeys;
    }

    public List<UserApiKey> listApiKeys(String username) {
        return userApiKeyRepository.findAllByUsername(username);
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (apiKey == null) {
            log.info("Missing Api key header.");
            throw new BadCredentialsException("Invalid API Key");
        }
        List<ApiKey> authentications = authenticationRepository.findByApiKey(apiKey);
        if (authentications.isEmpty()) {
            log.info("Not found active API key.");
            throw new BadCredentialsException("Invalid API Key");
        }
        if (!authentications.get(0)
                .isUserEnabled()) {
            log.info("API key owner account status is invalid.");
            throw new BadCredentialsException("Invalid API Key");
        }

        String username = authentications.get(0)
                .getUsername();
        boolean userEnabled = authentications.get(0)
                .isUserEnabled();
        List<SimpleGrantedAuthority> grantedAuthorities =
                authentications.stream()
                        .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                        .toList();
        return new ApiKeyAuthentication(username, userEnabled, grantedAuthorities);
    }
}
