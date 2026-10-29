package com.instrua.ai;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class AiProviderRouter implements AiProvider {
    private final OpenAiCompatibleProvider external; private final MockAiProvider mock;
    public AiProviderRouter(OpenAiCompatibleProvider external,MockAiProvider mock){this.external=external;this.mock=mock;}
    @Override public AiProviderResponse complete(AiProviderRequest request){return external.configured()?external.complete(request):mock.complete(request);}
}