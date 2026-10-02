package com.instrua.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MockAiProviderTest {
    private final MockAiProvider provider=new MockAiProvider();

    @Test void recognizesBookings(){
        var result=provider.complete(new AiProvider.AiProviderRequest("system","Quais meus agendamentos?",java.util.Map.of(),java.util.List.of("listBookings")));
        assertEquals("listBookings",result.tool());
    }

    @Test void marksMutationsForConfirmation(){
        var result=provider.complete(new AiProvider.AiProviderRequest("system","quero cancelar",java.util.Map.of(),java.util.List.of()));
        assertTrue(result.requiresConfirmation());
    }
}
