package com.example.demo.controller;

import com.example.demo.domain.ApiKeyAuthentication;
import com.example.demo.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {
    @GetMapping
    public User getUser(Principal principal) {
        ApiKeyAuthentication auth = (ApiKeyAuthentication) principal;
        String roles = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        return new User(auth.getUsername(), auth.isUserEnabled(), roles);
    }
}
