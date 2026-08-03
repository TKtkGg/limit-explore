package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                // ゲストでも遊べる（ゲーム進行・閲覧）
                .requestMatchers(
                    "/auth/**",
                    "/start",
                    "/move", "/move/**",
                    "/battle", "/battle/**",
                    "/treasure", "/treasure/**",
                    "/shop", "/shop/**",
                    "/status",
                    "/equipment", "/equipment/**",
                    "/card", "/card/**", "/cards",
                    "/items",
                    "/gameover",
                    "/progress"
                ).permitAll()
                // ログイン必須（セーブ・ランキング登録）
                .requestMatchers("/save/**", "/score/register", "/ranking").authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling((ex -> ex
                .authenticationEntryPoint((req, res, authEx) -> {
                    res.setStatus(401);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"authenticated\":false}");
                })
            ))
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable());
        return http.build();
    }
}
