package com.instrua.ai;

import com.instrua.companies.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/ai")
public class AiController {
    private final CompanyService companies;
    private final AiService ai;

    public AiController(CompanyService companies, AiService ai) { this.companies = companies; this.ai = ai; }

    @PostMapping("/instructions")
    public AiInstructionResponse generateInstruction(@PathVariable UUID companyId, @Valid @RequestBody AiInstructionRequest request) {
        companies.requireAccess(companyId);
        String instruction = ai.generateInstruction(request.context(), request.style());
        return new AiInstructionResponse(instruction, "OPENAI_RESPONSES_API");
    }

    public record AiInstructionRequest(@NotBlank String context, String style) {}
    public record AiInstructionResponse(String instruction, String provider) {}
}