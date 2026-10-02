package com.instrua.ai;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    private final AiProvider provider;
    private final AiToolService tools;
    public AiController(AiProvider provider,AiToolService tools){this.provider=provider;this.tools=tools;}

    @PostMapping("/chat")
    public AiResponse chat(@RequestBody ChatRequest request, Authentication authentication){
        UUID userId=UUID.fromString(authentication.getName());
        var result=provider.complete(new AiProvider.AiProviderRequest(
            "Instrua AI: assistente de agendamento multi-nicho. Nunca invente dados. Use somente ferramentas autorizadas.",
            request.message(),Map.of("userId",userId),List.of("listBookings","checkAvailability","getInstructions")));
        Map<String,Object> data=switch(result.tool()){
            case "listBookings" -> tools.listBookings(userId);
            case "checkAvailability" -> tools.checkAvailability(userId);
            case "getInstructions" -> tools.getInstructions(userId);
            default -> Map.of();
        };
        return new AiResponse(result.text(),result.intent(),result.tool(),data,result.requiresConfirmation());
    }

    public record ChatRequest(String message){}
    public record AiResponse(String message,String intent,String tool,Map<String,Object> data,boolean requiresConfirmation){}
}
