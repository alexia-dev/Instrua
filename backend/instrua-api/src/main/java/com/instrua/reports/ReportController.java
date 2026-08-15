package com.instrua.reports;

import com.instrua.appointments.AppointmentRepository;
import com.instrua.appointments.AppointmentStatus;
import com.instrua.clients.ClientRepository;
import com.instrua.companies.CompanyService;
import com.instrua.services.ServiceOfferingRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/reports")
public class ReportController {
    private final CompanyService companies;
    private final AppointmentRepository appointments;
    private final ClientRepository clients;
    private final ServiceOfferingRepository services;
    public ReportController(CompanyService companies, AppointmentRepository appointments, ClientRepository clients, ServiceOfferingRepository services) { this.companies = companies; this.appointments = appointments; this.clients = clients; this.services = services; }

    @GetMapping("/summary")
    public SummaryResponse summary(@PathVariable UUID companyId, @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to) {
        companies.requireAccess(companyId);
        LocalDate startDate = from == null ? LocalDate.now(ZoneOffset.UTC) : from;
        LocalDate endDate = to == null ? startDate : to;
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("O período final deve ser igual ou posterior ao inicial");
        Instant start = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return new SummaryResponse(startDate, endDate, clients.findAllByCompanyIdOrderByNameAsc(companyId).size(), services.findAllByCompanyIdOrderByNameAsc(companyId).size(),
                appointments.countByCompanyIdAndStartsAtBetween(companyId, start, end),
                appointments.countByCompanyIdAndStatusAndStartsAtBetween(companyId, AppointmentStatus.CONFIRMED, start, end),
                appointments.countByCompanyIdAndStatusAndStartsAtBetween(companyId, AppointmentStatus.CANCELLED, start, end),
                appointments.countByCompanyIdAndStatusAndStartsAtBetween(companyId, AppointmentStatus.NO_SHOW, start, end));
    }
    public record SummaryResponse(LocalDate from, LocalDate to, long totalClients, long activeServices, long appointments, long confirmed, long cancelled, long noShows) { }
}
