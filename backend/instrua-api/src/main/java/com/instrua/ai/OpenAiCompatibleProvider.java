package com.instrua.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleProvider implements AiProvider {
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final HttpClient client=HttpClient.newHttpClient();

    public OpenAiCompatibleProvider(ObjectMapper mapper,
        @Value("${app.ai.api-key:}") String apiKey,
        @Value("${app.ai.base-url:https://api.openai.com/v1/chat/completions}") String baseUrl,
        @Value("${app.ai.model:gpt-5.6-mini}") String model){
        this.mapper=mapper;this.apiKey=apiKey;this.baseUrl=baseUrl;this.model=model;}
    public boolean configured(){return apiKey!=null&&!apiKey.isBlank();}
    @Override public AiProviderResponse complete(AiProviderRequest request){
        if(!configured()) return new AiProviderResponse("Provedor externo não configurado.","GENERAL","none",Map.of(),false);
        try{
            String body=mapper.writeValueAsString(Map.of("model",model,"temperature",0.1,"messages",List.of(Map.of("role","system","content",request.systemPrompt()),Map.of("role","user","content",request.userMessage()))));
            HttpRequest http=HttpRequest.newBuilder(URI.create(baseUrl)).header(HttpHeaders.AUTHORIZATION,"Bearer "+apiKey).header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE).POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response=client.send(http,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()<200||response.statusCode()>=300) throw new IllegalStateException("AI provider HTTP "+response.statusCode());
            JsonNode root=mapper.readTree(response.body());
            String text=root.path("choices").path(0).path("message").path("content").asText();
            return new AiProviderResponse(text,"GENERAL","none",Map.of(),false);
        }catch(Exception ex){return new AiProviderResponse("Não foi possível consultar o provedor de IA agora. O aplicativo continua funcionando sem ele.","PROVIDER_ERROR","none",Map.of(),false);}
    }
}