package com.example.demo;

import com.example.demo.dao.UserApiKeyRepository;
import com.example.demo.domain.ApiKeyAuthentication;
import com.example.demo.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // -- START: spring.ldap.embedded --
        "spring.ldap.embedded.ldif=classpath:ldap-test-server.ldif",
        "spring.ldap.embedded.base-dn=dc=example,dc=com",
        "spring.ldap.embedded.port=18389",
        // -- END: spring.ldap.embedded --
        "spring.ldap.urls=ldap://localhost:18389/",
        "spring.ldap.base=dc=example,dc=com",
        // Disable H2 console.
        "spring.h2.console.enabled=false"
})
public class SecurityTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private UserApiKeyRepository userApiKeyRepository;
    @MockBean
    private H2ConsoleProperties h2ConsoleProperties;

    @Test
    @WithAnonymousUser
    public void givenUnauthenticatedUser_whenAccessSecuredEndpoint_thenGetRedirectedToLogin() throws Exception {
        mvc.perform(get("/hello"))
                .andExpect(status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    public void givenMockedUser_whenAccessSecuredEndpoint_thenGetOkStatus() throws Exception {
        mvc.perform(get("/hello"))
                .andExpect(status().isOk());
    }

    // Need to manually mock the service because this filter chain uses custom filter.
    @Test
    public void givenMockedUserWithoutRoles_whenAccessRoleSecuredEndpoint_thenGetAccessDeniedError() throws Exception {
        Mockito.when(authenticationService.getAuthentication(Mockito.any()))
                .thenReturn(new ApiKeyAuthentication("user", true, null));

        mvc.perform(get("/api/v1/user"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenMockedUserWithRoles_whenAccessRoleSecuredEndpoint_thenGetOkStatus() throws Exception {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_DEVELOPERS"));
        Mockito.when(authenticationService.getAuthentication(Mockito.any()))
                .thenReturn(new ApiKeyAuthentication("user", true, authorities));

        // The API requires user with role USER.
        mvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk());
    }
}
