package com.example.demo.dao;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@AllArgsConstructor
public class ApiKeyRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<ApiKey> findByApiKey(String apiKey) {
        return jdbcTemplate.query("""
                    SELECT a.api_key, a.username, u.enabled, auth.authority
                    FROM user_api_key a
                    INNER JOIN authorities auth ON auth.username = a.username
                    INNER JOIN users u ON u.username = a.username
                    WHERE a.active = true AND a.api_key = ?
                """, new ApiKeyAuthenticationRowMapper(), apiKey);
    }

    public static class ApiKeyAuthenticationRowMapper implements RowMapper<ApiKey> {

        @Override
        public ApiKey mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiKey data = new ApiKey();
            data.setUsername(rs.getString("username"));
            data.setUserEnabled(rs.getBoolean("enabled"));
            data.setAuthority(rs.getString("authority"));
            return data;
        }
    }
}
