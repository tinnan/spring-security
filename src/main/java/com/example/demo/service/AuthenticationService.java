package com.example.demo.service;

import com.example.demo.dao.UserApiKeyRepository;
import com.example.demo.domain.ApiKeyAuthentication;
import com.example.demo.domain.UserApiKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {
    private static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";
    private final UserApiKeyRepository userApiKeyRepository;
    private final LdapTemplate ldapTemplate;

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
        if (log.isDebugEnabled()) {
            log.debug("Authenticating API key.");
        }
        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (apiKey == null) {
            log.error("Missing Api key header.");
            throw new BadCredentialsException("Invalid API Key");
        }
        Optional<UserApiKey> userApiKey = userApiKeyRepository.findById(apiKey);
        if (userApiKey.isEmpty() || !userApiKey.get()
                .isActive()) {
            log.error("Not found active API key.");
            throw new BadCredentialsException("Invalid API Key");
        }
        String username = userApiKey.get()
                .getUsername();
        if (log.isDebugEnabled()) {
            log.debug("Searching LDAP user authorities.");
        }
        // Is there a more graceful way to set base suffix?
        //    -> Yes! Set in application.properties: spring.ldap.base=<base suffix>
        // in this case - spring.ldap.base=dc=example,dc=com
        List<SimpleGrantedAuthority> grantedAuthorities = ldapTemplate.search("ou=groups", "(objectclass=groupOfUniqueNames)",
                new GrantedAuthorityAttributesMapper());
        if (log.isDebugEnabled()) {
            log.debug("Granted Authorities: {}", grantedAuthorities);
        }
        return new ApiKeyAuthentication(username, true, grantedAuthorities);
    }

    private static class GrantedAuthorityAttributesMapper implements AttributesMapper<SimpleGrantedAuthority> {
        @Override
        public SimpleGrantedAuthority mapFromAttributes(Attributes attributes) throws NamingException {
            String authority = (String) attributes.get("cn")
                    .get();
            return new SimpleGrantedAuthority("ROLE_" + authority.toUpperCase());
        }
    }
}
