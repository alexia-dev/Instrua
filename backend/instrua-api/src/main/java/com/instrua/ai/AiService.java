package com.instrua.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiService {
    private static final String DEFAULT_SYSTEM_PROMPT = """
        Você é o assistente de IA do Instrua.
        Sua função é ajudar equipes de clínicas e serviços a transformar informações fornecidas
        pela equipe em instruções claras, objetivas e acessíveis para pacientes ou clientes.

        Regras:
        - Não faça diagnóstico.
        - Não prescreva medicamentos, doses ou tratamentos.
        - Não invente informações clínicas, horários, preparo ou contraindicações.
        - Quando faltar uma informação essencial, sinalize explicitamente que ela precisa ser confirmada pela equipe.
        - Preserve números, datas, horários e nomes exatamente quando fornecidos.
        - Use linguagem simples e acolhedora.
        - Não substitua a avaliação de um profissional.
        """;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;

    public AiService(ObjectMapper objectMapper,
            @Value("$" + "{app.ai.openai-api-key:}") String apiKey,
            @Value("$" + "{app.ai.model:gpt-6-luna}") String model) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generateInstruction(String context, String requestedStyle) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Integração de IA não configurada");
        }
        String userPrompt = """
            Gere uma instrução para o paciente com base nos dados abaixo.

            Contexto:
            %s

            Estilo solicitado:
            %s

            Retorne somente o texto final da instrução, sem prefácio.
            """.formatted(context.trim(), requestedStyle == null || requestedStyle.isBlank()
                ? "simples, claro e acolhedor" : requestedStyle.trim());
        try {
            String body = objectMapper.createObjectNode()
                    .put("model", model)
                    .set("input", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode().put("role", "system").put("content", DEFAULT_SYSTEM_PROMPT))
                            .add(objectMapper.createObjectNode().put("role", "user").put("content", userPrompt)))
                    .toString();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .timeout(Duration.ofSeconds(45))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao consultar o provedor de IA");
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode outputText = root.get("output_text");
            if (outputText == null || outputText.asText().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "O provedor de IA não retornou texto");
            }
            return outputText.asText().trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A chamada à IA foi interrompida");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível comunicar com o provedor de IA");
        }
    }
}