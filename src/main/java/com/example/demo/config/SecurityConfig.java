package com.example.demo.config;

import com.example.demo.config.filter.ApiKeyAuthenticationFilter;
import com.example.demo.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Autowired
    public void initialize(AuthenticationManagerBuilder builder, DataSource dataSource,
                           @Value("${app.security.default.user}") String user,
                           @Value("${app.security.default.password}") String password,
                           @Value("${app.security.default.roles}") String[] roles
    ) throws Exception {
        builder.jdbcAuthentication()
                .dataSource(dataSource)
                .withDefaultSchema()
                .withUser(user)
                .password(new BCryptPasswordEncoder().encode(password))
                .roles(roles);
        // Default user/password is defined in application.properties file.
    }

    @Bean
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        // The filter chain to secure any request that does not start with /api
        http.securityMatcher("/**")
                .securityMatchers(matcher -> matcher.requestMatchers(new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**"))))
                .authorizeHttpRequests(requests -> requests.requestMatchers("/",
                                "/home", "/static/**")
                        .permitAll()
                        .requestMatchers(PathRequest.toH2Console())
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form.loginPage("/login") // Set custom login page. If not set, default login page
                        // will be served.
                        .permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()))
                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }

    @Bean
    public SecurityFilterChain restFilterChain(HttpSecurity http, AuthenticationService authenticationService) throws Exception {
        // The filter chain to secure any request that start with /api
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(requests -> requests.anyRequest()
                        .hasRole("USER")) // Any request to /api/** requires to have role USER.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Using addFilter() method will make Spring complains that we need to assign order to
                // ApiKeyAuthenticationFilter.
                .addFilterAfter(new ApiKeyAuthenticationFilter(authenticationService), UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /*
    https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/password-encoder.html
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
