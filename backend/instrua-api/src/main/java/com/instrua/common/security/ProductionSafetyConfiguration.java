package com.instrua.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Do not start a production server with local development credentials. */
@Configuration
@Profile("prod")
public class ProductionSafetyConfiguration {
    @Bean
    ApplicationRunner failClosedWithoutProductionSecrets(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${spring.datasource.password}") String dbPassword,
            @Value("${app.security.cors.allowed-origins:}") String corsOrigins) {
        return args -> {
            if (secret.length() < 48 || secret.contains("local-development-secret") || secret.contains("ci-only")) {
                throw new IllegalStateException("Defina JWT_SECRET aleatório de pelo menos 48 caracteres em produção.");
            }
            if (dbPassword.isBlank() || "instrua_dev".equals(dbPassword) || "instrua".equals(dbPassword)) {
                throw new IllegalStateException("Senha de banco de desenvolvimento não pode ser usada em produção.");
            }
            if (corsOrigins.isBlank() || corsOrigins.contains("localhost") || !corsOrigins.startsWith("https://")) {
                throw new IllegalStateException("Configure APP_SECURITY_CORS_ALLOWED_ORIGINS com origem HTTPS de produção.");
            }
        };
    }
}
