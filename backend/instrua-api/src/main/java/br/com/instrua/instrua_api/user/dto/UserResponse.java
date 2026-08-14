package br.com.instrua.instrua_api.user.dto;

import br.com.instrua.instrua_api.user.controller.model.AppUser;
import br.com.instrua.instrua_api.user.domain.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        UserRole role,
        boolean active,
        OffsetDateTime createdAt
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}