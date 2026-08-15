package com.instrua.appointments;

import com.instrua.clients.Client;
import com.instrua.clients.ClientRepository;
import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.employees.Employee;
import com.instrua.employees.EmployeeRepository;
import com.instrua.services.ServiceOffering;
import com.instrua.services.ServiceOfferingRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    private final CompanyService companies;
    private final ClientRepository clients;
    private final EmployeeRepository employees;
    private final ServiceOfferingRepository serviceOfferings;
    private final AppointmentRepository appointments;

    public AppointmentService(CompanyService companies, ClientRepository clients, EmployeeRepository employees,
                              ServiceOfferingRepository serviceOfferings, AppointmentRepository appointments) {
        this.companies = companies; this.clients = clients; this.employees = employees; this.serviceOfferings = serviceOfferings; this.appointments = appointments;
    }

    @Transactional(readOnly = true)
    public List<Appointment> list(UUID companyId) {
        companies.requireAccess(companyId);
        return appointments.findAllByCompanyIdOrderByStartsAtAsc(companyId);
    }

    @Transactional
    public Appointment create(UUID companyId, UUID clientId, UUID employeeId, UUID serviceId, Instant startsAt, String notes) {
        Company company = companies.requireAccess(companyId);
        Client client = clients.findByIdAndCompanyIdAndActiveTrue(clientId, companyId).orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        ServiceOffering offering = serviceOfferings.findByIdAndCompanyIdAndActiveTrue(serviceId, companyId).orElseThrow(() -> new NotFoundException("Serviço não encontrado"));
        Employee employee = employeeId == null ? null : employees.findByIdAndCompanyIdAndActiveTrue(employeeId, companyId).orElseThrow(() -> new NotFoundException("Funcionário não encontrado"));
        Instant endsAt = startsAt.plus(offering.getDurationMinutes(), ChronoUnit.MINUTES);
        if (employee != null && appointments.existsByCompanyIdAndEmployeeIdAndStartsAtLessThanAndEndsAtGreaterThanAndStatusIn(
                companyId, employee.getId(), endsAt, startsAt, EnumSet.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED))) {
            throw new BusinessException("O funcionário já possui um agendamento neste horário");
        }
        return appointments.save(new Appointment(company, client, employee, offering, startsAt, endsAt, notes));
    }

    @Transactional
    public Appointment changeStatus(UUID companyId, UUID appointmentId, AppointmentStatus status, String reason) {
        companies.requireAccess(companyId);
        Appointment appointment = appointments.findByIdAndCompanyId(appointmentId, companyId).orElseThrow(() -> new NotFoundException("Agendamento não encontrado"));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("Não é possível alterar um agendamento encerrado");
        }
        if (status == AppointmentStatus.CANCELLED && (reason == null || reason.isBlank())) throw new BusinessException("Informe o motivo do cancelamento");
        appointment.changeStatus(status, reason);
        return appointment;
    }
}
