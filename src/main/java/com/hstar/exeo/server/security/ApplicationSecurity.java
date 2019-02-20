package com.hstar.exeo.server.security;

import com.hstar.exeo.server.security.jwt.JWTAuthenticationProvider;
import com.hstar.exeo.server.security.jwt.JWTFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *
 * Created by Saswat on 7/17/2015.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    @Autowired private JWTAuthenticationProvider jwtAuthenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()//no default csrf
            /*.authorizeRequests()//access control, rest api is mostly locked down
                .antMatchers("/css/**").permitAll()
                .antMatchers("/font/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/lib/**").permitAll()
                .antMatchers("/images/**").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/register").permitAll()
                .antMatchers("/app").permitAll()
                .antMatchers("/pair").permitAll()
                .antMatchers("/error").permitAll()
                .antMatchers("/partials/*").permitAll()
                .antMatchers("/wsignaler/**").permitAll()
                .anyRequest().authenticated()
                .and()*/
            .requiresChannel()//force https
                .anyRequest().requiresSecure()
                .and()
            .addFilterBefore(new JWTFilter(jwtAuthenticationProvider), UsernamePasswordAuthenticationFilter.class)//jwt token filter
            .headers()//hsts
                .httpStrictTransportSecurity()
                .and()
                .and()
            .sessionManagement()//stateless!
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(jwtAuthenticationProvider);
    }

}
