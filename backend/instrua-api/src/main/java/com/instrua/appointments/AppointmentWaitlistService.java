package com.instrua.appointments;

import com.instrua.clients.Client;
import com.instrua.clients.ClientRepository;
import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.services.ServiceOffering;
import com.instrua.services.ServiceOfferingRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentWaitlistService {
    private final CompanyService companies;
    private final ClientRepository clients;
    private final ServiceOfferingRepository services;
    private final AppointmentRepository appointments;
    private final AppointmentWaitlistRepository waitlist;

    public AppointmentWaitlistService(CompanyService companies, ClientRepository clients,
            ServiceOfferingRepository services, AppointmentRepository appointments,
            AppointmentWaitlistRepository waitlist) {
        this.companies = companies; this.clients = clients; this.services = services;
        this.appointments = appointments; this.waitlist = waitlist;
    }

    @Transactional(readOnly = true)
    public List<AppointmentWaitlistEntry> list(UUID companyId, UUID serviceId) {
        companies.requireAccess(companyId);
        return waitlist.findAllByCompanyIdAndServiceOfferingIdAndStatusOrderByPriorityDescCreatedAtAsc(
                companyId, serviceId, WaitlistStatus.WAITING);
    }

    @Transactional
    public AppointmentWaitlistEntry join(UUID companyId, UUID clientId, UUID serviceId,
            Instant preferredFrom, Instant preferredUntil, int priority, String notes) {
        Company company = companies.requireAccess(companyId);
        Client client = clients.findByIdAndCompanyIdAndActiveTrue(clientId, companyId)
                .orElseThrow(() -> new NotFoundException("Paciente não encontrado"));
        ServiceOffering service = services.findByIdAndCompanyIdAndActiveTrue(serviceId, companyId)
                .orElseThrow(() -> new NotFoundException("Exame/serviço não encontrado"));
        if (priority < 0 || priority > 100) throw new BusinessException("Prioridade deve estar entre 0 e 100");
        if (preferredFrom != null && preferredUntil != null && preferredUntil.isBefore(preferredFrom))
            throw new BusinessException("O fim da preferência deve ser posterior ao início");
        return waitlist.save(new AppointmentWaitlistEntry(company, client, service, preferredFrom, preferredUntil, priority, notes));
    }

    @Transactional
    public AppointmentWaitlistEntry offer(UUID companyId, UUID entryId, UUID appointmentId, Instant expiresAt) {
        companies.requireAccess(companyId);
        AppointmentWaitlistEntry entry = waitlist.findByIdAndCompanyId(entryId, companyId)
                .orElseThrow(() -> new NotFoundException("Entrada da fila não encontrada"));
        Appointment appointment = appointments.findByIdAndCompanyId(appointmentId, companyId)
                .orElseThrow(() -> new NotFoundException("Agendamento/vaga não encontrado"));
        if (!entry.getServiceOffering().getId().equals(appointment.getServiceOffering().getId()))
            throw new BusinessException("A vaga não corresponde ao exame solicitado");
        if (appointment.getStatus() != AppointmentStatus.CANCELLED)
            throw new BusinessException("A vaga precisa estar liberada antes de ser oferecida");
        if (expiresAt == null || !expiresAt.isAfter(Instant.now()))
            throw new BusinessException("Informe um prazo futuro para aceitar a oferta");
        entry.offer(appointment, expiresAt);
        return entry;
    }

    @Transactional
    public AppointmentWaitlistEntry accept(UUID companyId, UUID entryId) {
        companies.requireAccess(companyId);
        AppointmentWaitlistEntry entry = waitlist.findByIdAndCompanyId(entryId, companyId)
                .orElseThrow(() -> new NotFoundException("Oferta não encontrada"));
        try { entry.accept(Instant.now()); }
        catch (IllegalStateException e) { throw new BusinessException(e.getMessage()); }
        // Reatribuição do agendamento será integrada à operação de remanejamento na próxima etapa.
        return entry;
    }

    @Transactional
    public AppointmentWaitlistEntry decline(UUID companyId, UUID entryId) {
        companies.requireAccess(companyId);
        AppointmentWaitlistEntry entry = waitlist.findByIdAndCompanyId(entryId, companyId)
                .orElseThrow(() -> new NotFoundException("Oferta não encontrada"));
        try { entry.decline(); }
        catch (IllegalStateException e) { throw new BusinessException(e.getMessage()); }
        return entry;
    }
}
