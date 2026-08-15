package br.com.instrua.instrua_api.user.dto;

import br.com.instrua.instrua_api.user.controller.model.AppUser;
import br.com.instrua.instrua_api.user.domain.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserResponseTest {

    @Test
    void shouldMapUserToResponse() {
        AppUser user = new AppUser("Maria", "maria@example.com", "hashed", UserRole.RECEPTIONIST);

        UserResponse response = UserResponse.from(user);

        assertEquals(user.getId(), response.id());
        assertEquals(user.getFullName(), response.fullName());
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getRole(), response.role());
        assertEquals(user.isActive(), response.active());
        assertEquals(user.getCreatedAt(), response.createdAt());
    }
}
