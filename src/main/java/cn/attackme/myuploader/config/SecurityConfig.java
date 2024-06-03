package cn.attackme.myuploader.config;

import cn.attackme.myuploader.TokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private TokenFilter tokenFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .requiresChannel().anyRequest().requiresSecure() // 要求所有请求使用HTTPS
                .and()
                .authorizeRequests()
                .antMatchers("/auth", "/Properties/scan", "/PropertiesDownload/download", "/swagger-ui.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs", "/swagger-ui.html","/Ex").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
