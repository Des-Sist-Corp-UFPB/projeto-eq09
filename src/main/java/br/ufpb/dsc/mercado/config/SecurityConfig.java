package br.ufpb.dsc.mercado.config;

import br.ufpb.dsc.mercado.config.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public SecurityConfig() {
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                // Habilita CORS com configuração detalhada e desabilita CSRF (stateless)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                
                // Gerenciamento de sessão stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configuração das rotas e permissões
                .authorizeHttpRequests(auth -> auth
                        // Endpoints de autenticação pública
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // Consultar catálogo de filmes e detalhes de filmes é público
                        .requestMatchers(HttpMethod.GET, "/api/filmes/**").permitAll()
                        
                        // Monitoramento de saúde é público
                        .requestMatchers("/actuator/health").permitAll()
                        
                        // Adicionar novos filmes é de acesso exclusivo a usuários ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/filmes").hasRole("ADMIN")
                        
                        // Avaliar e comentar filmes exige login (tanto ADMIN quanto USER comuns podem)
                        .requestMatchers(HttpMethod.POST, "/api/filmes/*/avaliar").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/filmes/*/comentarios").authenticated()
                        
                        // Qualquer outra requisição deve estar autenticada
                        .anyRequest().authenticated()
                );

        // Adiciona o filtro JWT antes do filtro padrão de autenticação por usuário/senha
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite o frontend React (dev local e produção Docker)
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
