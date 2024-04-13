package com.example.demo.config;

import com.example.demo.config.filter.ApiKeyAuthenticationFilter;
import com.example.demo.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.ldap.LdapPasswordComparisonAuthenticationManagerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    /*
        This is how to configure LDAP authentication before Spring Security 5.7.0-M2
        See also https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
     */
//    @Autowired
//    public void initialize(AuthenticationManagerBuilder builder,
//                           @Value("${spring.ldap.embedded.base-dn}") String ldapBaseDn,
//                           @Value("${spring.ldap.embedded.port}") String ldapPort
//    ) throws Exception {
//        builder.ldapAuthentication()
//                .userDetailsContextMapper(userDetailsContextMapper())
//                .userDnPatterns("uid={0},ou=people")
//                .groupSearchBase("ou=groups")
//                .contextSource()
//                .url("ldap://localhost:" + ldapPort + "/" + ldapBaseDn)
//                .and()
//                .passwordCompare()
//                .passwordEncoder(passwordEncoder())
//                .passwordAttribute("userPassword");
//        // Default user/password is defined in application.properties file.
//    }

    /*
        No need to explicitly declare this bean since we already define spring.ldap.embedded.* properties in
        application.properties file which automatically created and configured
        EmbeddedLdapServerContextSourceFactoryBean.
     */
//    @Bean
//    public EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean(
//            @Value("${spring.ldap.embedded.port:8389}") Integer ldapPort,
//            @Value("${spring.ldap.embedded.base-dn:dc=example,dc=com}") String ldapBaseDn) {
//        EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean =
//                EmbeddedLdapServerContextSourceFactoryBean.fromEmbeddedLdapServer();
//        contextSourceFactoryBean.setPort(ldapPort);
//        contextSourceFactoryBean.setRoot(ldapBaseDn);
//        return contextSourceFactoryBean;
//    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    @Bean
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator(BaseLdapPathContextSource contextSource) {
        // For looking up user roles and populate into GrantedAuthorities.
        // Is there a more graceful way to set base suffix?
        //    -> Yes! Set in application.properties: spring.ldap.base=<base suffix>
        // in this case - spring.ldap.base=dc=example,dc=com
        DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator = new DefaultLdapAuthoritiesPopulator(contextSource,
                "ou=groups");
        // Set group filter to match group member attribute name in LDAP server.
        ldapAuthoritiesPopulator.setGroupSearchFilter("uniqueMember={0}");
        return ldapAuthoritiesPopulator;
    }

    @Bean
    public AuthenticationManager ldapAuthenticationManager(BaseLdapPathContextSource contextSource,
                                                           LdapAuthoritiesPopulator ldapAuthoritiesPopulator) {
        LdapPasswordComparisonAuthenticationManagerFactory factory =
                new LdapPasswordComparisonAuthenticationManagerFactory(contextSource, passwordEncoder());
        // Is there a more graceful way to set base suffix?
        //    -> Yes! Set in application.properties: spring.ldap.base=<base suffix>
        // in this case - spring.ldap.base=dc=example,dc=com
        factory.setUserDnPatterns("uid={0},ou=people");
        factory.setUserDetailsContextMapper(new LdapUserDetailsMapper());
        factory.setPasswordAttribute("userPassword");
        factory.setLdapAuthoritiesPopulator(ldapAuthoritiesPopulator);
        return factory.createAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain webFilterChain(HttpSecurity http, AuthenticationManager ldapAuthenticationManager) throws Exception {
        // The filter chain to secure any request that does not start with /api
        http.securityMatcher("/**")
                .securityMatchers(matcher -> matcher.requestMatchers(new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**"))))
                .authenticationManager(ldapAuthenticationManager)
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
                        .hasRole("DEVELOPERS"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Using addFilter() method will make Spring complains that we need to assign order to
                // ApiKeyAuthenticationFilter.
                .addFilterAfter(new ApiKeyAuthenticationFilter(authenticationService),
                        UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
