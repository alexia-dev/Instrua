package com.instrua.commercial;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Production path, not a mocked UI: PostgreSQL migrations, JWT and company isolation. */
@SpringBootTest
@AutoConfigureMockMvc
class CommercialFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private JsonNode payload(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString());
    }
    private String body(Object data) throws Exception { return json.writeValueAsString(data); }

    @Test
    void ownerCreatesRealAgendaAndPatientCannotModifyCompany() throws Exception {
        String random = UUID.randomUUID().toString().substring(0, 12);
        String email = "owner-" + random + "@example.invalid";
        JsonNode registration = payload(mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of(
                        "ownerName","Proprietária de Teste","email",email,"password","Password123!",
                        "companyName","Unidade de Teste","companySlug","unit-" + random,
                        "companyEmail",email,"timezone","America/Sao_Paulo"))))
                .andExpect(status().isCreated()).andReturn());
        String owner = "Bearer " + registration.path("authentication").path("accessToken").asText();
        String companyId = registration.path("company").path("id").asText();
        String base = "/api/v1/companies/" + companyId;

        JsonNode client = payload(mvc.perform(post(base + "/clients")
                .header("Authorization",owner).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("name","Cliente Real de Teste","email","test-" + random + "@example.invalid"))))
                .andExpect(status().isCreated()).andReturn());
        JsonNode professional = payload(mvc.perform(post(base + "/employees")
                .header("Authorization",owner).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("name","Profissional de Teste","title","Consulta","accessRole","CLINICAL"))))
                .andExpect(status().isCreated()).andReturn());
        JsonNode service = payload(mvc.perform(post(base + "/services")
                .header("Authorization",owner).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("name","Atendimento de Teste","durationMinutes",30,"price",100.0))))
                .andExpect(status().isCreated()).andReturn());

        String startsAt = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MINUTES).toString();
        Map<String,Object> booking = Map.of(
                "clientId",client.path("id").asText(),
                "employeeId",professional.path("id").asText(),
                "serviceId",service.path("id").asText(),
                "startsAt",startsAt
        );
        JsonNode scheduled = payload(mvc.perform(post(base + "/appointments")
                .header("Authorization",owner).contentType(MediaType.APPLICATION_JSON)
                .content(body(booking))).andExpect(status().isCreated()).andReturn());
        assertEquals("SCHEDULED", scheduled.path("status").asText());
        mvc.perform(post(base + "/appointments").header("Authorization",owner)
                .contentType(MediaType.APPLICATION_JSON).content(body(booking)))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(patch(base + "/appointments/" + scheduled.path("id").asText() + "/status")
                .header("Authorization",owner).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("status","CONFIRMED"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        mvc.perform(get(base + "/appointments").header("Authorization",owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].clientName").value("Cliente Real de Teste"));

        JsonNode patientAccount = payload(mvc.perform(post("/api/v1/auth/register/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("name","Pessoa de Teste","email","patient-" + random + "@example.invalid","password","Password123!"))))
                .andExpect(status().isCreated()).andReturn());
        String patient = "Bearer " + patientAccount.path("accessToken").asText();

        mvc.perform(get("/api/v1/catalog")).andExpect(status().isOk());
        mvc.perform(get(base + "/clients").header("Authorization",patient))
                .andExpect(status().isNotFound());
        mvc.perform(post(base + "/services").header("Authorization",patient)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("name","Invasão","durationMinutes",10))))
                .andExpect(status().isNotFound());

        String otherTime = Instant.now().plus(2, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES).toString();
        mvc.perform(post(base + "/appointments/self").header("Authorization",patient)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("serviceId",service.path("id").asText(),
                        "employeeId",professional.path("id").asText(),"startsAt",otherTime))))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/v1/me/appointments").header("Authorization",patient))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].serviceName").value("Atendimento de Teste"));

        JsonNode invitation = payload(mvc.perform(post(base + "/invitations")
                .header("Authorization",owner).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email","patient-" + random + "@example.invalid","role","RECEPTION"))))
                .andExpect(status().isCreated()).andReturn());
        String invitationToken = invitation.path("token").asText();
        assertFalse(invitationToken.isBlank());
        mvc.perform(get("/api/v1/me/invitations/" + invitationToken).header("Authorization",patient))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("RECEPTION"));
        mvc.perform(post("/api/v1/me/invitations/" + invitationToken + "/accept").header("Authorization",patient))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/me/invitations/" + invitationToken + "/accept").header("Authorization",patient))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/companies").header("Authorization",patient))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(companyId));
        mvc.perform(post(base + "/clients").header("Authorization",patient)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("name","Cliente Cadastrado pela Recepção"))))
                .andExpect(status().isCreated());
    }
}
