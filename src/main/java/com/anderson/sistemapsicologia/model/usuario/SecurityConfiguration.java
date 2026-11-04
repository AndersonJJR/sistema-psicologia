package com.anderson.sistemapsicologia.model.usuario;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de segurança Spring Security com RBAC estrito e autenticação stateless via JWT.
 * Bloqueia o perfil RECEPCAO dos endpoints clínicos (/api/v1/prontuarios e /api/v1/registros-documentais).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"erro\":\"Não autenticado. Token JWT ausente ou inválido.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"erro\":\"Acesso negado para o seu perfil (RBAC/CFP).\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Bloqueio estrito do perfil RECEPCAO a prontuários e registros documentais (CFP 001/2009)
                        .requestMatchers("/api/v1/registros-documentais/**").hasRole("PSICOLOGO")
                        .requestMatchers("/api/v1/prontuarios/**").hasAnyRole("PSICOLOGO", "ADMIN")
                        .requestMatchers("/api/v1/exportacao/**").hasAnyRole("PSICOLOGO", "ADMIN")
                        .requestMatchers("/api/v1/auditoria/**").hasAnyRole("PSICOLOGO", "ADMIN")
                        // Endpoints demográficos, agenda e financeiro acessíveis à RECEPCAO, PSICOLOGO e ADMIN
                        .requestMatchers("/api/v1/pacientes/**").hasAnyRole("RECEPCAO", "PSICOLOGO", "ADMIN")
                        .requestMatchers("/api/v1/agendamentos/**").hasAnyRole("RECEPCAO", "PSICOLOGO", "ADMIN")
                        .requestMatchers("/api/v1/psicologos/**").hasAnyRole("RECEPCAO", "PSICOLOGO", "ADMIN")
                        .requestMatchers("/api/v1/pagamentos/**").hasAnyRole("RECEPCAO", "PSICOLOGO", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
