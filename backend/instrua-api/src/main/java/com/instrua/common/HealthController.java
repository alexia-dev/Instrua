package com.instrua.common;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("service", "instrua-api", "status", "UP", "timestamp", Instant.now().toString(), "apiVersion", "v1");
    }
    @GetMapping("/capabilities")
    public Map<String, Object> capabilities() {
        return Map.of(
            "accountModel", "single-account-multiple-contexts",
            "modules", List.of("identity-and-access", "organizations-and-teams", "discovery-and-catalog",
                "appointments-and-cancellation-policies", "notifications-outbox", "clinical-records-protected-domain",
                "clinical-document-workflow", "billing-and-fiscal-integration-boundary", "employee-journey",
                "social-moderation", "opportunities", "subscriptions", "audit-and-security-events"),
            "externalIntegrations", Map.of("payments", "adapter-required", "fiscal-invoice", "provider-and-legal-validation-required",
                "whatsapp", "official-meta-provider-adapter-required", "digital-signature", "provider-and-legal-validation-required",
                "ai", "human-review-required"),
            "productionReady", false
        );
    }
}
