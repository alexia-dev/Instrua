package com.instrua.platform;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AppCatalogController {

    @GetMapping("/apps")
    public List<AppDescriptor> apps() {
        return List.of(
                new AppDescriptor("INSTRUA", "Instrua", "Gestão, agenda, jornada e instruções", true, "FREE"),
                new AppDescriptor("NEXA_BILL", "Nexa Bill", "Faturamento, lotes e relatórios", false, "PLANNED"),
                new AppDescriptor("NEXA_AI", "Nexa AI", "Assistente e automações inteligentes", false, "PLANNED")
        );
    }

    public record AppDescriptor(String id, String name, String description, boolean available, String plan) {}
}
