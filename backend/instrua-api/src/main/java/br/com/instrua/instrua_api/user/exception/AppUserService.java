package br.com.instrua.instrua_api.user.exception;

import br.com.instrua.instrua_api.user.controller.model.AppUser;
import br.com.instrua.instrua_api.user.domain.repository.AppUserRepository;
import br.com.instrua.instrua_api.user.dto.CreateUserRequest;
import br.com.instrua.instrua_api.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AppUserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        AppUser user = new AppUser(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.role()
        );

        return UserResponse.from(userRepository.save(user));
    }
}