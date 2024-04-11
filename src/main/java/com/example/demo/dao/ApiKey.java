package com.example.demo.dao;

import lombok.Data;

@Data
public class ApiKey {
    private String username;
    private boolean userEnabled;
    private String authority;
}
