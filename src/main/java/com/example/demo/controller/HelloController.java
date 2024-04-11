package com.example.demo.controller;

import com.example.demo.domain.UserApiKey;
import com.example.demo.service.AuthenticationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/hello")
@Slf4j
@AllArgsConstructor
public class HelloController {
    private final AuthenticationService authenticationService;

    @GetMapping
    public String init(Model model, @AuthenticationPrincipal User user) {
        List<UserApiKey> userApiKeys = authenticationService.listApiKeys(user.getUsername());
        model.addAttribute("apiKeys", userApiKeys);
        return "hello";
    }

    @PostMapping("/create-api-key")
    public String createToken(Model model, @AuthenticationPrincipal User user) {
        List<UserApiKey> userApiKeys = authenticationService.createApiKey(user.getUsername());
        model.addAttribute("apiKeys", userApiKeys);
        return "hello";
    }
}
