package com.example.demo.config.filter;

import com.example.demo.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.io.PrintWriter;

/*
Do not make filter into a @Bean. Otherwise, it will be auto registered into filter chains and might mess up your
configuration.
To exclude a bean from registration, see more here
https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.webserver.add-servlet-filter-listener.spring-bean.disable
or from example code below:

@Configuration(proxyBeanMethods = false)
public class MyFilterConfiguration {

    @Bean
    public FilterRegistrationBean<MyFilter> registration(MyFilter filter) {
        FilterRegistrationBean<MyFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

}
 */
@Slf4j
@AllArgsConstructor
public class ApiKeyAuthenticationFilter extends GenericFilterBean {
    private final AuthenticationService authenticationService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            Authentication authentication = authenticationService.getAuthentication((HttpServletRequest) servletRequest);
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        } catch (Exception ex) {
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            PrintWriter writer = httpResponse.getWriter();
            writer.print("""
                    {
                        "error": "%s"
                    }
                    """.formatted(ex.getMessage()));
            writer.flush();
            writer.close();
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
