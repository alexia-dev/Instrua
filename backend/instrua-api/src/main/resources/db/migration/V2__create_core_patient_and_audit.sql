-- V1 already creates audit_logs and indexes; make V2 additive for fresh databases.
create table patients (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    name varchar(255) not null,
    email varchar(255),
    phone varchar(60),
    document_number varchar(100),
    notes varchar(4000),
    active boolean not null default true,
    user_id uuid references app_users(id) on delete set null
);

create index idx_patients_company_name on patients(company_id, name);
create index idx_patients_user on patients(user_id);

alter table audit_logs add column if not exists created_at timestamptz;
alter table audit_logs add column if not exists updated_at timestamptz;
update audit_logs set created_at = coalesce(created_at, occurred_at, now()),
    updated_at = coalesce(updated_at, occurred_at, now());
alter table audit_logs alter column created_at set not null;
alter table audit_logs alter column updated_at set not null;

create index if not exists idx_audit_company_occurred on audit_logs(company_id, occurred_at desc);
create index if not exists idx_audit_actor_occurred on audit_logs(actor_user_id, occurred_at desc);
