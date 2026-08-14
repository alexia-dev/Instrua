package br.com.instrua.instrua_api.user.controller;

import br.com.instrua.instrua_api.user.dto.CreateUserRequest;
import br.com.instrua.instrua_api.user.dto.UserResponse;
import br.com.instrua.instrua_api.user.exception.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class AppUserController {

    private final AppUserService userService;

    public AppUserController(AppUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse response = userService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    public AppUserService getUserService() {
        return userService;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userService == null) ? 0 : userService.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AppUserController other = (AppUserController) obj;
        if (userService == null) {
            if (other.userService != null)
                return false;
        } else if (!userService.equals(other.userService))
            return false;
        return true;
    }
}