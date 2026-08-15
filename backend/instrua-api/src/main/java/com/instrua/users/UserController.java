package com.instrua.users;

import java.util.Set;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final CurrentUser currentUser;
    public UserController(CurrentUser currentUser) { this.currentUser = currentUser; }
    @GetMapping("/me")
    public UserResponse me() {
        User user = currentUser.get();
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRoles(), user.isEnabled());
    }
    public record UserResponse(UUID id, String name, String email, Set<Role> roles, boolean enabled) { }
}
