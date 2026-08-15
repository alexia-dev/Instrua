create table app_users (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    name varchar(255) not null,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    enabled boolean not null default true
);

create table user_roles (
    user_id uuid not null references app_users(id) on delete cascade,
    role varchar(40) not null,
    primary key (user_id, role)
);

create table companies (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    owner_user_id uuid not null references app_users(id),
    name varchar(255) not null,
    slug varchar(120) not null unique,
    email varchar(255),
    phone varchar(60),
    timezone varchar(80) not null default 'America/Sao_Paulo',
    active boolean not null default true
);

create table employees (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    account_user_id uuid references app_users(id),
    name varchar(255) not null,
    email varchar(255),
    phone varchar(60),
    title varchar(255),
    access_role varchar(40) not null default 'EMPLOYEE',
    active boolean not null default true,
    unique (company_id, account_user_id)
);

create table clients (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    name varchar(255) not null,
    email varchar(255),
    phone varchar(60),
    document_number varchar(100),
    notes varchar(4000),
    active boolean not null default true
);

create table service_offerings (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    name varchar(255) not null,
    description varchar(4000),
    duration_minutes integer not null check (duration_minutes > 0),
    price numeric(12,2),
    active boolean not null default true
);

create table appointments (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    client_id uuid not null references clients(id),
    employee_id uuid references employees(id),
    service_offering_id uuid not null references service_offerings(id),
    starts_at timestamptz not null,
    ends_at timestamptz not null,
    status varchar(40) not null default 'SCHEDULED',
    confirmation_status varchar(40) not null default 'PENDING',
    notes varchar(2000),
    cancellation_reason varchar(1000),
    check (ends_at > starts_at)
);

create table instructions (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    service_offering_id uuid references service_offerings(id) on delete cascade,
    title varchar(255) not null,
    content_type varchar(40) not null,
    content varchar(10000) not null,
    display_order integer not null default 0,
    active boolean not null default true
);

create table notifications (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    appointment_id uuid references appointments(id) on delete set null,
    channel varchar(40) not null,
    status varchar(40) not null,
    recipient varchar(500) not null,
    subject varchar(500),
    body varchar(10000) not null,
    scheduled_for timestamptz,
    sent_at timestamptz,
    provider_message_id varchar(500),
    failure_reason varchar(2000)
);

create table integrations (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    provider varchar(60) not null,
    name varchar(255) not null,
    active boolean not null default true,
    external_account_id varchar(500),
    configuration varchar(4000),
    secret_reference varchar(500)
);

create index idx_employees_company on employees(company_id);
create index idx_clients_company_name on clients(company_id, name);
create index idx_services_company_name on service_offerings(company_id, name);
create index idx_appointments_company_start on appointments(company_id, starts_at);
create index idx_appointments_employee_time on appointments(employee_id, starts_at, ends_at);
create index idx_instructions_company_service on instructions(company_id, service_offering_id);
create index idx_notifications_company_created on notifications(company_id, created_at desc);
create index idx_integrations_company on integrations(company_id);
