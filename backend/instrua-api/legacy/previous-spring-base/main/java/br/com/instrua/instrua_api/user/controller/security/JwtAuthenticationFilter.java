package br.com.instrua.instrua_api.user.controller.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static class JwtService {
        public String extractUsername(String jwt) {
            if (jwt == null || jwt.isBlank()) {
                return null;
            }
            return jwt;
        }

        public boolean isTokenValid(String jwt, UserDetails userDetails) {
            if (jwt == null || jwt.isBlank() || userDetails == null) {
                return false;
            }
            String username = extractUsername(jwt);
            return username != null && username.equals(userDetails.getUsername());
        }
    }

    public interface UserDetails {
        String getUsername();

        Collection<?> getAuthorities();
    }

    public interface UserDetailsService {
        UserDetails loadUserByUsername(String username);
    }

    public static class UsernamePasswordAuthenticationToken {
        private final Object principal;
        private final Object credentials;
        private final Collection<?> authorities;
        private Object details;

        public UsernamePasswordAuthenticationToken(Object principal, Object credentials, Collection<?> authorities) {
            this.principal = principal;
            this.credentials = credentials;
            this.authorities = authorities == null ? Collections.emptyList() : authorities;
        }

        public Object getPrincipal() {
            return principal;
        }

        public Object getCredentials() {
            return credentials;
        }

        public Collection<?> getAuthorities() {
            return authorities;
        }

        public void setDetails(Object details) {
            this.details = details;
        }

        public Object getDetails() {
            return details;
        }
    }

    public static class SecurityContextHolder {
        private static final ThreadLocal<SecurityContext> CONTEXT = ThreadLocal.withInitial(SecurityContext::new);

        public static SecurityContext getContext() {
            return CONTEXT.get();
        }

        public static void clearContext() {
            CONTEXT.remove();
        }
    }

    public static class SecurityContext {
        private Object authentication;

        public Object getAuthentication() {
            return authentication;
        }

        public void setAuthentication(Object authentication) {
            this.authentication = authentication;
        }
    }

    public static class WebAuthenticationDetailsSource {
        public Object buildDetails(HttpServletRequest request) {
            return request != null ? request.getRemoteAddr() : null;
        }
    }

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtém o cabeçalho Authorization da requisição HTTP
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Verifica se o cabeçalho existe e se começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrai o token ignorando o prefixo "Bearer " (7 caracteres)
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        // 4. Se houver e-mail e o usuário ainda não estiver autenticado no contexto atual
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Carrega os detalhes do usuário do banco de dados
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Valida se o token JWT é íntegro e pertence a este usuário
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Cria o objeto de autenticação do Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

                // Adiciona os detalhes da requisição Web (IP, Sessão, etc.) ao token
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Atualiza o contexto do Spring Security com o usuário autenticado
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Continua a execução da cadeia de filtros do Spring
        filterChain.doFilter(request, response);
    }
}
