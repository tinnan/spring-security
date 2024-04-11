package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"api_key", "username"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserApiKey {
    @Id
    private String apiKey;
    private String username;
    private boolean active;
}
