package com.instrua.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AiServiceTest {
    @Test
    void rejectsRequestsWhenAiIsNotConfigured() {
        AiService service = new AiService(new ObjectMapper(), "", "gpt-6-luna");
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.generateInstruction("Exame amanhã às 8h.", "simples"));
        assertEquals(503, exception.getStatusCode().value());
    }
}