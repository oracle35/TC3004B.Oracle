package com.springboot.MyTodoList.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * This is the WebSecurityConfiguration class that extends WebSecurityConfigurerAdapter.
 * It is used to configure the security settings for the application.
 * It currently accepts everything since login is allowed on the front.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf(csrf -> csrf.disable())
        .authorizeRequests(
            requests -> requests.antMatchers("/**").permitAll()); // This allows all requests
    // without authentication
  }
}
