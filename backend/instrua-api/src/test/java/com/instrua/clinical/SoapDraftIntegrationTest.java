package com.instrua.clinical;

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

/** Exercises PostgreSQL Flyway migration, real JWT and strict clinical access separation. */
@SpringBootTest
@AutoConfigureMockMvc
class SoapDraftIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String body(Object value) throws Exception { return mapper.writeValueAsString(value); }
    private JsonNode json(MvcResult response) throws Exception {
        return mapper.readTree(response.getResponse().getContentAsString());
    }
    private String bearer(String token){return "Bearer "+token;}
    private JsonNode registerPatient(String name,String email) throws Exception {
        return json(mvc.perform(post("/api/v1/auth/register/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("name",name,"email",email,"password","Password123!"))))
            .andExpect(status().isCreated()).andReturn());
    }

    @Test
    void clinicianCanSaveOwnSoapDraftButReceptionAndOwnerCannotSeeClinicalContent() throws Exception {
        String stamp=UUID.randomUUID().toString().substring(0,12);
        String ownerEmail="owner-soap-"+stamp+"@example.invalid";
        JsonNode ownerRegistration=json(mvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("ownerName","Gestora Teste","email",ownerEmail,
                "password","Password123!","companyName","Clínica de Teste",
                "companySlug","soap-test-"+stamp,"companyEmail",ownerEmail,
                "timezone","America/Sao_Paulo"))))
            .andExpect(status().isCreated()).andReturn());
        String companyId=ownerRegistration.path("company").path("id").asText();
        String owner=bearer(ownerRegistration.path("authentication").path("accessToken").asText());
        String base="/api/v1/companies/"+companyId;

        JsonNode service=json(mvc.perform(post(base+"/services").header("Authorization",owner)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("name","Consulta de teste","durationMinutes",30))))
            .andExpect(status().isCreated()).andReturn());

        String clinicianEmail="clinician-soap-"+stamp+"@example.invalid";
        JsonNode clinicianAccount=registerPatient("Profissional Teste",clinicianEmail);
        String clinician=bearer(clinicianAccount.path("accessToken").asText());
        JsonNode invite=json(mvc.perform(post(base+"/invitations").header("Authorization",owner)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("email",clinicianEmail,"role","CLINICAL"))))
            .andExpect(status().isCreated()).andReturn());
        JsonNode membership=json(mvc.perform(post("/api/v1/me/invitations/"+invite.path("token").asText()+"/accept")
            .header("Authorization",clinician))
            .andExpect(status().isOk()).andReturn());
        String employeeId=membership.path("employeeId").asText();

        String patientEmail="patient-soap-"+stamp+"@example.invalid";
        JsonNode patientAccount=registerPatient("Paciente Teste",patientEmail);
        String patient=bearer(patientAccount.path("accessToken").asText());
        String nextTime=Instant.now().plus(4,ChronoUnit.DAYS).truncatedTo(ChronoUnit.MINUTES).toString();
        JsonNode booking=json(mvc.perform(post(base+"/appointments/self").header("Authorization",patient)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("serviceId",service.path("id").asText(),
                "employeeId",employeeId,"startsAt",nextTime))))
            .andExpect(status().isCreated()).andReturn());
        String appointmentId=booking.path("appointment").path("id").asText();
        assertFalse(appointmentId.isBlank());

        String appointmentPath=base+"/clinical/appointments/"+appointmentId;
        mvc.perform(get(appointmentPath).header("Authorization",owner)).andExpect(status().isNotFound());
        mvc.perform(get(appointmentPath).header("Authorization",patient)).andExpect(status().isNotFound());
        mvc.perform(get(appointmentPath).header("Authorization",clinician))
            .andExpect(status().isOk()).andExpect(jsonPath("$.patientLinked").value(true))
            .andExpect(jsonPath("$.encounterId").isEmpty());

        JsonNode started=json(mvc.perform(post(appointmentPath+"/encounter").header("Authorization",clinician))
            .andExpect(status().isCreated()).andReturn());
        String encounterId=started.path("encounterId").asText();
        assertFalse(encounterId.isBlank());

        String soapPath=base+"/clinical/encounters/"+encounterId+"/soap-draft";
        Map<String,String> clinical=Map.of(
            "subjective","Queixa de teste registrada pelo profissional.",
            "objective","Sem observações adicionais neste teste.",
            "assessment","Avaliação descritiva de teste.",
            "plan","Acompanhamento conforme registro de teste.");
        mvc.perform(put(soapPath).header("Authorization",owner)
            .contentType(MediaType.APPLICATION_JSON).content(body(clinical)))
            .andExpect(status().isNotFound());
        mvc.perform(put(soapPath).header("Authorization",patient)
            .contentType(MediaType.APPLICATION_JSON).content(body(clinical)))
            .andExpect(status().isNotFound());
        mvc.perform(put(soapPath).header("Authorization",clinician)
            .contentType(MediaType.APPLICATION_JSON).content(body(clinical)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.subjective").value(clinical.get("subjective")));
        mvc.perform(get(appointmentPath).header("Authorization",clinician))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.soapDraft.objective").value(clinical.get("objective")));

        // A second clinician employed by the same company cannot open this person's encounter.
        String otherEmail="other-clinician-"+stamp+"@example.invalid";
        String other=bearer(registerPatient("Outra Profissional",otherEmail).path("accessToken").asText());
        JsonNode otherInvite=json(mvc.perform(post(base+"/invitations").header("Authorization",owner)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("email",otherEmail,"role","CLINICAL"))))
            .andExpect(status().isCreated()).andReturn());
        mvc.perform(post("/api/v1/me/invitations/"+otherInvite.path("token").asText()+"/accept")
            .header("Authorization",other)).andExpect(status().isOk());
        mvc.perform(get(appointmentPath).header("Authorization",other)).andExpect(status().isNotFound());
        mvc.perform(put(soapPath).header("Authorization",other)
            .contentType(MediaType.APPLICATION_JSON).content(body(clinical)))
            .andExpect(status().isNotFound());
    }
}
