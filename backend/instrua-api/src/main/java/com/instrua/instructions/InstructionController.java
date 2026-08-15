package com.instrua.instructions;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.NotFoundException;
import com.instrua.services.ServiceOffering;
import com.instrua.services.ServiceOfferingRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/instructions")
@Transactional
public class InstructionController {
    private final CompanyService companies;
    private final InstructionRepository instructions;
    private final ServiceOfferingRepository services;
    public InstructionController(CompanyService companies, InstructionRepository instructions, ServiceOfferingRepository services) { this.companies = companies; this.instructions = instructions; this.services = services; }

    @GetMapping
    public List<InstructionResponse> list(@PathVariable UUID companyId, @RequestParam(required = false) UUID serviceId) {
        companies.requireAccess(companyId);
        List<Instruction> result = serviceId == null ? instructions.findAllByCompanyIdOrderByDisplayOrderAsc(companyId) : instructions.findAllByCompanyIdAndServiceOfferingIdOrderByDisplayOrderAsc(companyId, serviceId);
        return result.stream().map(InstructionResponse::from).toList();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstructionResponse create(@PathVariable UUID companyId, @Valid @RequestBody InstructionRequest request) {
        Company company = companies.requireAccess(companyId);
        ServiceOffering offering = request.serviceId() == null ? null : services.findByIdAndCompanyIdAndActiveTrue(request.serviceId(), companyId).orElseThrow(() -> new NotFoundException("Serviço não encontrado"));
        return InstructionResponse.from(instructions.save(new Instruction(company, offering, request.title(), request.contentType(), request.content(), request.displayOrder())));
    }
    public record InstructionRequest(UUID serviceId, @NotBlank String title, @NotNull InstructionContentType contentType, @NotBlank String content, int displayOrder) { }
    public record InstructionResponse(UUID id, UUID serviceId, String title, InstructionContentType contentType, String content, int displayOrder, boolean active) {
        static InstructionResponse from(Instruction instruction) { return new InstructionResponse(instruction.getId(), instruction.getServiceOffering() == null ? null : instruction.getServiceOffering().getId(), instruction.getTitle(), instruction.getContentType(), instruction.getContent(), instruction.getDisplayOrder(), instruction.isActive()); }
    }
}
