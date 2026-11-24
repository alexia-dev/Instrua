-- Clinical SOAP draft foundation. No prescription/signature/issuance is implied.
-- A care encounter created through this workflow belongs to one appointment within one company.
create unique index if not exists uq_care_encounters_company_appointment
  on care_encounters(company_id, appointment_id)
  where appointment_id is not null;
alter table clinical_records add column if not exists updated_at timestamptz not null default now();
create unique index if not exists uq_clinical_soap_active_draft
  on clinical_records(encounter_id, author_user_id, record_type)
  where status = 'DRAFT' and record_type = 'SOAP';
