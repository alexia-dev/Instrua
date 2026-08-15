package com.instrua.users;

import com.instrua.common.exception.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final UserRepository users;

    public CurrentUser(UserRepository users) { this.users = users; }

    public User get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new NotFoundException("Usuário autenticado não encontrado");
        }
        return users.findById(principal.id()).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }
}
