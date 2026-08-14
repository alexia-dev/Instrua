package br.com.instrua.instrua_api.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public HealthResponse check() {
        return new HealthResponse("UP", "INSTRUA API");
    }

    public record HealthResponse(String status, String application) {
    }
}