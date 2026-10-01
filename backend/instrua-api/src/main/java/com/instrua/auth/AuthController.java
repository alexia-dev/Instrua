package com.instrua.auth;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyController.CompanyResponse;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.users.Role;
import com.instrua.users.User;
import com.instrua.users.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CompanyService companies;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService, CompanyService companies) {
        this.users=users; this.passwordEncoder=passwordEncoder; this.jwtService=jwtService; this.companies=companies;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody RegisterRequest request) {
        if(users.existsByEmailIgnoreCase(request.email())) throw new BusinessException("Já existe uma conta com este e-mail");
        boolean customer="CUSTOMER".equalsIgnoreCase(request.accountType());
        Set<Role> roles=customer?Set.of(Role.CUSTOMER):Set.of(Role.COMPANY_OWNER);
        User user=users.save(new User(request.ownerName(),request.email(),passwordEncoder.encode(request.password()),roles));
        Company company=null;
        if(!customer){
            if(request.companyName()==null||request.companyName().isBlank()) throw new BusinessException("Informe o nome do estabelecimento");
            company=companies.create(request.companyName(),request.companySlug(),request.companyEmail(),request.companyPhone(),request.timezone(),user);
            if (request.niche() != null && !request.niche().isBlank()) company.setNiche(request.niche());
        }
        return new RegistrationResponse(authResponse(user),company==null?null:CompanyResponse.from(company));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user=users.findByEmailIgnoreCase(request.email()).orElseThrow(()->new BusinessException("E-mail ou senha inválidos"));
        if(!user.isEnabled()||!passwordEncoder.matches(request.password(),user.getPasswordHash())) throw new BusinessException("E-mail ou senha inválidos");
        return authResponse(user);
    }

    private AuthResponse authResponse(User user){return new AuthResponse(jwtService.generate(user),"Bearer",Instant.now().plusSeconds(jwtService.getExpirationSeconds()),user.getId(),user.getName(),user.getEmail(),user.getRoles());}

    public record RegisterRequest(@NotBlank String ownerName,@Email @NotBlank String email,@NotBlank @Size(min=8) String password,
                                  String accountType,String companyName,String companySlug,@Email String companyEmail,String companyPhone,String timezone,String niche){}
    public record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
    public record AuthResponse(String accessToken,String tokenType,Instant expiresAt,java.util.UUID userId,String name,String email,Set<Role> roles){}
    public record RegistrationResponse(AuthResponse authentication,CompanyResponse company){}
}
