package com.example.demo.dao;

import com.example.demo.domain.UserApiKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

// Using @SpringBootTest for auto-configuring security user details schema.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ApiKeyRepositoryTest {
    private static final String TEST_API_KEY = "1299675c-ed5a-426b-8d1e-1a67f865c35d";
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "test_password";

    @Autowired
    private ApiKeyRepository authenticationRepository;

    @Test
    public void test() {
        List<ApiKey> authentications = authenticationRepository.findByApiKey(TEST_API_KEY);
        authentications.forEach(auth -> {
            Assertions.assertEquals(TEST_USERNAME, auth.getUsername());
            Assertions.assertTrue(auth.isUserEnabled());
            Assertions.assertEquals("ROLE_USER", auth.getAuthority());
        });
    }

    @TestConfiguration
    public static class TestConfig implements CommandLineRunner {
        @Autowired
        private UserApiKeyRepository userApiKeyRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Override
        public void run(String... args) {
            UserApiKey apiKey = new UserApiKey(TEST_API_KEY, TEST_USERNAME, true);
            userApiKeyRepository.save(apiKey);

            jdbcTemplate.update("INSERT INTO users(username, password, enabled) VALUES(?, ?, ?)", TEST_USERNAME,
                    passwordEncoder.encode(TEST_PASSWORD), true);
            jdbcTemplate.update("INSERT INTO authorities(username, authority) VALUES(?, ?)", TEST_USERNAME,
                    "ROLE_USER");
        }
    }
}