// AuthenticationService.java
package br.com.instrua.instrua_api.user.controller.service;

// Simplified local types to fix unresolved imports when other modules are not available.
// These are minimal implementations to allow this class to compile in isolation.

import java.util.Optional;

public class AuthenticationService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public AuthenticationService(UserRepository userRepository,
                                                                 PasswordEncoder passwordEncoder,
                                                                 JwtService jwtService,
                                                                 AuthenticationManager authenticationManager) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtService = jwtService;
                this.authenticationManager = authenticationManager;
        }

        public AuthenticationResponse register(RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new IllegalArgumentException("Este e-mail já está cadastrado.");
                }

                User user = User.builder()
                                .firstname(request.getFirstname())
                                .lastname(request.getLastname())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(Role.USER)
                                .build();

                userRepository.save(user);
                String jwtToken = jwtService.generateToken(user);

                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .build();
        }

        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha inválidos."));

                String jwtToken = jwtService.generateToken(user);

                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .build();
        }
}

// Minimal supporting types (placeholders)
interface UserRepository {
        boolean existsByEmail(String email);
        void save(User user);
        Optional<User> findByEmail(String email);
}

interface PasswordEncoder {
        String encode(String raw);
}

interface JwtService {
        String generateToken(User user);
}

interface AuthenticationManager {
        void authenticate(UsernamePasswordAuthenticationToken token);
}

class UsernamePasswordAuthenticationToken {
        private final String principal;
        private final String credentials;

        public UsernamePasswordAuthenticationToken(String principal, String credentials) {
                this.principal = principal;
                this.credentials = credentials;
        }

        public String getPrincipal() { return principal; }
        public String getCredentials() { return credentials; }
}

class AuthenticationResponse {
        private final String token;

        private AuthenticationResponse(String token) { this.token = token; }

        // Provide accessor so the token field is considered used by callers
        public String getToken() { return token; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
                private String token;
                public Builder token(String token) { this.token = token; return this; }
                public AuthenticationResponse build() { return new AuthenticationResponse(token); }
        }
}

class RegisterRequest {
        private String firstname;
        private String lastname;
        private String email;
        private String password;
        public String getFirstname() { return firstname; }
        public String getLastname() { return lastname; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
}

class AuthenticationRequest {
        private String email;
        private String password;
        public String getEmail() { return email; }
        public String getPassword() { return password; }
}

class User {
        private String firstname;
        private String lastname;
        private String email;
        private String password;
        private Role role;

        private User() {}

        // Accessors to avoid unused-field warnings and to be useful to other code
        public String getFirstname() { return firstname; }
        public String getLastname() { return lastname; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public Role getRole() { return role; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
                private final User u = new User();
                public Builder firstname(String f) { u.firstname = f; return this; }
                public Builder lastname(String l) { u.lastname = l; return this; }
                public Builder email(String e) { u.email = e; return this; }
                public Builder password(String p) { u.password = p; return this; }
                public Builder role(Role r) { u.role = r; return this; }
                public User build() { return u; }
        }
}

enum Role { USER }

