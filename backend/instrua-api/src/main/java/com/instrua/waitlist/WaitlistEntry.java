package com.instrua.waitlist;
import com.instrua.companies.Company;
import com.instrua.common.model.BaseEntity;
import com.instrua.patients.Patient;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name = "exam_waitlist_entries", indexes = {@Index(name="idx_waitlist_company_exam_status_priority", columnList="company_id, exam_id, status, priority_level, created_at")})
public class WaitlistEntry extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="company_id", nullable=false) private Company company;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="patient_id", nullable=false) private Patient patient;
 @Column(name="exam_id", nullable=false) private java.util.UUID examId;
 @Column(name="medical_request_url", length=1000) private String medicalRequestUrl;
 @Column(name="preferred_shifts", length=100) private String preferredShifts;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private WaitlistStatus status=WaitlistStatus.WAITING;
 @Column(name="priority_level",nullable=false) private int priorityLevel=0;
 @Column(name="notified_at") private Instant notifiedAt;
 @Column(name="response_deadline") private Instant responseDeadline;
 protected WaitlistEntry() {}
 public WaitlistEntry(Company company, Patient patient, java.util.UUID examId, String url, String shifts, int priority) { this.company=company;this.patient=patient;this.examId=examId;this.medicalRequestUrl=url;this.preferredShifts=shifts;this.priorityLevel=priority; }
 public Company getCompany(){return company;} public Patient getPatient(){return patient;} public java.util.UUID getExamId(){return examId;} public String getMedicalRequestUrl(){return medicalRequestUrl;} public String getPreferredShifts(){return preferredShifts;} public WaitlistStatus getStatus(){return status;} public int getPriorityLevel(){return priorityLevel;} public Instant getNotifiedAt(){return notifiedAt;} public Instant getResponseDeadline(){return responseDeadline;}
 public void notifyPatient(Instant deadline){status=WaitlistStatus.NOTIFIED;notifiedAt=Instant.now();responseDeadline=deadline;} public void confirm(){status=WaitlistStatus.CONFIRMED;} public void cancel(){status=WaitlistStatus.CANCELED;} public void expire(){status=WaitlistStatus.EXPIRED;}
}
