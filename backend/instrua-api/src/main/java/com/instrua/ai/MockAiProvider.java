package com.instrua.ai;

import org.springframework.stereotype.Component;
import java.util.Locale;
import java.util.Map;

@Component
public class MockAiProvider implements AiProvider {
    @Override public AiProviderResponse complete(AiProviderRequest request) {
        String q=request.userMessage().toLowerCase(Locale.ROOT);
        if(q.contains("agendamento")||q.contains("agenda")||q.contains("marcação"))
            return new AiProviderResponse("Posso consultar seus próximos agendamentos.","LIST_BOOKINGS","listBookings",Map.of(),false);
        if(q.contains("disponib")||q.contains("horário")||q.contains("horario"))
            return new AiProviderResponse("Posso consultar horários disponíveis.","CHECK_AVAILABILITY","checkAvailability",Map.of(),false);
        if(q.contains("instru")||q.contains("prepar"))
            return new AiProviderResponse("Posso consultar as instruções do serviço.","GET_INSTRUCTIONS","getInstructions",Map.of(),false);
        if(q.contains("cancel")||q.contains("remarc"))
            return new AiProviderResponse("Essa ação altera um agendamento e precisa da sua confirmação antes de ser executada.","MUTATION","none",Map.of(),true);
        return new AiProviderResponse("Entendi. Nesta primeira versão posso consultar agendamentos, disponibilidade e instruções. O provedor de IA pode ser conectado depois sem alterar essas ferramentas.","GENERAL","none",Map.of(),false);
    }
}
