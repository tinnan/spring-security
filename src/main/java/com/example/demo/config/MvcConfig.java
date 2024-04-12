package com.example.demo.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.sql.SQLException;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/home")
                .setViewName("home");
        registry.addViewController("/")
                .setViewName("home");
        registry.addViewController("/login")
                .setViewName("login");
        registry.addViewController("/request")
                .setViewName("request");
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile("!test")
    public Server inMemoryH2DatabaseaServer() throws SQLException {
        // org.h2.tools.Server will be available in classpath when dependency scope is set to "compile".
        // To connect to the H2 database, Set "jdbc:h2:tcp://localhost:9090/mem:mydb" in database URL.
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9090");
    }
}
