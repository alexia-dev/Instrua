package com.instrua.common.security;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Accept a single Neon PostgreSQL URI from the hosting provider's private
 * environment. Do not publish the URI in the client, repository, or logs.
 */
@Configuration
@ConditionalOnProperty(name = "NEON_DATABASE_URL")
public class NeonDataSourceConfiguration {
    @Bean
    @Primary
    DataSource neonDataSource(@Value("${NEON_DATABASE_URL}") String url) {
        if (!(url.startsWith("postgresql://") || url.startsWith("postgres://"))) {
            throw new IllegalStateException("NEON_DATABASE_URL precisa ser uma URL PostgreSQL válida.");
        }
        URI uri = URI.create(url);
        String hostname = uri.getHost();
        String rawInfo = uri.getRawUserInfo();
        String rawPath = uri.getRawPath();
        if (hostname == null || rawInfo == null || !rawInfo.contains(":") || rawPath == null || rawPath.length() < 2
                || uri.getFragment() != null || !hostname.toLowerCase(Locale.ROOT).endsWith(".neon.tech")) {
            throw new IllegalStateException("NEON_DATABASE_URL precisa apontar para um banco Neon válido.");
        }
        int separator = rawInfo.indexOf(':');
        String username = URLDecoder.decode(rawInfo.substring(0,separator), StandardCharsets.UTF_8);
        String password = URLDecoder.decode(rawInfo.substring(separator+1), StandardCharsets.UTF_8);
        if (password.isBlank()) throw new IllegalStateException("NEON_DATABASE_URL sem credencial.");
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String database = rawPath.substring(1);
        if (!database.matches("[a-zA-Z0-9_-]+")) throw new IllegalStateException("Banco inválido na conexão Neon.");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + hostname + ":" + port + "/" + database + "?sslmode=require");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(0);
        config.setIdleTimeout(60000);
        config.setConnectionTimeout(20000);
        config.setPoolName("instrua-neon");
        return new HikariDataSource(config);
    }
}
