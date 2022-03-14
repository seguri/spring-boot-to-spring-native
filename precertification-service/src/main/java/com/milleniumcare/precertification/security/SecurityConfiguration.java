package com.milleniumcare.precertification.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests -> authorizeRequests
                        .antMatchers("/precertificationrequests/v3/api-docs/**", "/precertificationrequests/swagger-ui/**", "/precertificationrequests/swagger-ui.html").permitAll()
                        .requestMatchers(EndpointRequest.to("health", "prometheus")).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer().jwt();
    }
}
