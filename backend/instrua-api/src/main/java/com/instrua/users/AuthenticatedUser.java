package com.instrua.users;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AuthenticatedUser(UUID id, String email, String passwordHash, boolean enabled,
                                Collection<? extends GrantedAuthority> authorities) implements UserDetails {
    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return passwordHash; }
    @Override public boolean isEnabled() { return enabled; }
}
