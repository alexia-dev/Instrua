-- A local patient can receive clinical care without having a personal Instrua login.
-- Never associate clinical files based only on matching name, phone, email or CPF.
alter table patients add column if not exists linked_client_id uuid references clients(id) on delete restrict;
create unique index if not exists uq_patients_company_linked_client
  on patients(company_id,linked_client_id) where linked_client_id is not null;
create index if not exists idx_patients_company_user on patients(company_id,user_id);
