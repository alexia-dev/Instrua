package com.instrua.platform;

import com.instrua.companies.CompanyController.CompanyResponse;
import com.instrua.companies.CompanyService;
import com.instrua.users.CurrentUser;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {
    private final CurrentUser currentUser;
    private final CompanyService companies;

    public MeController(CurrentUser currentUser, CompanyService companies) {
        this.currentUser = currentUser;
        this.companies = companies;
    }

    @GetMapping
    public MeResponse me() {
        var user = currentUser.get();
        return new MeResponse(user.getId(), user.getName(), user.getEmail(), user.getRoles());
    }

    @GetMapping("/organizations")
    public List<CompanyResponse> organizations() {
        return companies.accessible().stream().map(CompanyResponse::from).toList();
    }

    @GetMapping("/entitlements")
    public List<Entitlement> entitlements() {
        boolean instrua = !companies.accessible().isEmpty();
        return List.of(
                new Entitlement("INSTRUA", instrua, "FREE"),
                new Entitlement("NEXA_BILL", false, "PLANNED"),
                new Entitlement("NEXA_AI", false, "PLANNED")
        );
    }

    public record MeResponse(UUID userId, String name, String email, java.util.Set<com.instrua.users.Role> roles) {}
    public record Entitlement(String appId, boolean enabled, String plan) {}
}
