package com.example.demo;

import com.example.demo.dao.UserApiKeyRepository;
import com.example.demo.domain.UserApiKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(UserApiKeyRepository userApiKeyRepository,
                                 @Value("${app.security.default.user}") String user,
                                 @Value("${app.security.default.apikey}") String apiKey) {
        return args -> {
            userApiKeyRepository.save(new UserApiKey(apiKey, user, true));
        };
    }

}
