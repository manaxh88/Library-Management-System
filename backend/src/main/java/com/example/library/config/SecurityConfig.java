package com.example.library.config;

import com.example.library.repository.SysUserRepository;
import com.example.library.util.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 安全配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SysUserRepository sysUserRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(SysUserRepository sysUserRepository, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.sysUserRepository = sysUserRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置密码编码器 Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置自定义的 UserDetailsService
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> sysUserRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_" + getRoleString(user.getRole()))
                        .accountLocked(user.getStatus() != 1)
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * 配置 AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService userDetailsService,
                                                      PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    /**
     * 配置 CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "https://localhost:*",
            "http://127.0.0.1:*",
            "https://127.0.0.1:*",
            "http://192.168.*:*",
            "https://192.168.*:*",
            "http://10.*:*",
            "https://10.*:*",
            "http://172.16.*:*",
            "https://172.16.*:*",
            "http://172.17.*:*",
            "https://172.17.*:*",
            "http://172.18.*:*",
            "https://172.18.*:*",
            "http://172.19.*:*",
            "https://172.19.*:*",
            "http://172.20.*:*",
            "https://172.20.*:*",
            "http://172.21.*:*",
            "https://172.21.*:*",
            "http://172.22.*:*",
            "https://172.22.*:*",
            "http://172.23.*:*",
            "https://172.23.*:*",
            "http://172.24.*:*",
            "https://172.24.*:*",
            "http://172.25.*:*",
            "https://172.25.*:*",
            "http://172.26.*:*",
            "https://172.26.*:*",
            "http://172.27.*:*",
            "https://172.27.*:*",
            "http://172.28.*:*",
            "https://172.28.*:*",
            "http://172.29.*:*",
            "https://172.29.*:*",
            "http://172.30.*:*",
            "https://172.30.*:*",
            "http://172.31.*:*",
            "https://172.31.*:*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*", "Content-Type", "Authorization"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login").permitAll()
                .requestMatchers("/users").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 角色转换方法
     */
    private String getRoleString(Integer role) {
        if (role == null) {
            return "STUDENT";
        }
        return switch (role) {
            case 0 -> "ADMIN";
            case 1 -> "TEACHER";
            case 2 -> "STUDENT";
            default -> "STUDENT";
        };
    }
}


