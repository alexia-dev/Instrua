create table app_catalog (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    app_code varchar(80) not null unique,
    name varchar(120) not null,
    description varchar(500),
    active boolean not null default true
);

create index idx_app_catalog_active on app_catalog(active);

create table organization_app_entitlements (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    app_code varchar(80) not null,
    plan_code varchar(80) not null,
    enabled boolean not null default true,
    unique(company_id, app_code)
);

create index idx_org_entitlements_company on organization_app_entitlements(company_id);

create table appointment_journey (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    appointment_id uuid not null references appointments(id) on delete cascade,
    registration_completed boolean not null default false,
    documents_verified boolean not null default false,
    instructions_sent boolean not null default false,
    instructions_completed boolean not null default false,
    confirmed boolean not null default false,
    check_in_at timestamptz,
    completed_at timestamptz,
    unique(appointment_id)
);

create index idx_journey_company on appointment_journey(company_id);
