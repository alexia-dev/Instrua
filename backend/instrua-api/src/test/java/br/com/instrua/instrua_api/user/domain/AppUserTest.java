package br.com.instrua.instrua_api.user.domain;

import org.junit.jupiter.api.Test;

import br.com.instrua.instrua_api.user.controller.model.AppUser;

import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppUserTest {

    @Test
    void shouldInitializeAuditFieldsAndDefaultStateOnPersist() throws Exception {
        AppUser user = new AppUser("Ana Silva", "ana@example.com", "hashed-password", UserRole.PATIENT);

        // call non-public lifecycle method
        invokeLifecycle(user, "beforeInsert");

        assertNotNull(user.getId());
        assertEquals("Ana Silva", user.getFullName());
        assertEquals("ana@example.com", user.getEmail());
        assertEquals("hashed-password", user.getPasswordHash());
        assertEquals(UserRole.PATIENT, user.getRole());
        assertTrue(user.isActive());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    void shouldUpdateUpdatedAtOnPersistUpdate() throws Exception {
        AppUser user = new AppUser("Eduardo", "edu@example.com", "hash", UserRole.DOCTOR);
        invokeLifecycle(user, "beforeInsert");

        OffsetDateTime firstUpdatedAt = user.getUpdatedAt();

        invokeLifecycle(user, "beforeUpdate");

        assertTrue(user.getUpdatedAt().isAfter(firstUpdatedAt) || user.getUpdatedAt().equals(firstUpdatedAt));
    }

    private void invokeLifecycle(Object target, String methodName) throws Exception {
        java.lang.reflect.Method m = target.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(target);
    }
}
