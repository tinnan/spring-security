package com.example.demo;

import com.example.demo.config.SecurityConfig;
import com.example.demo.dao.UserApiKeyRepository;
import com.example.demo.domain.ApiKeyAuthentication;
import com.example.demo.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {SecurityConfig.class}))
@AutoConfigureTestDatabase
@ActiveProfiles("test")
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
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        Mockito.when(authenticationService.getAuthentication(Mockito.any()))
                .thenReturn(new ApiKeyAuthentication("user", true, authorities));

        // The API requires user with role USER.
        mvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk());
    }
}
