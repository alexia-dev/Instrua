package com.instrua.ai;

import java.util.List;
import java.util.Map;

public interface AiProvider {
    AiProviderResponse complete(AiProviderRequest request);
    record AiProviderRequest(String systemPrompt, String userMessage, Map<String,Object> context, List<String> allowedTools) {}
    record AiProviderResponse(String text, String intent, String tool, Map<String,Object> arguments, boolean requiresConfirmation) {}
}
