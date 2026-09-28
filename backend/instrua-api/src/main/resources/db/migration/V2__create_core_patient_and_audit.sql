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

create table audit_logs (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid references companies(id) on delete set null,
    actor_user_id uuid references app_users(id) on delete set null,
    action varchar(80) not null,
    entity_type varchar(120) not null,
    entity_id uuid,
    occurred_at timestamptz not null,
    metadata varchar(8000)
);

create index idx_audit_company_occurred on audit_logs(company_id, occurred_at desc);
create index idx_audit_actor_occurred on audit_logs(actor_user_id, occurred_at desc);
