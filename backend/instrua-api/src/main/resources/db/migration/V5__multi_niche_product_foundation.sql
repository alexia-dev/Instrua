-- Instrua multi-niche product foundation
-- Core extensions and disruptive-feature foundations. Business rules remain enforced server-side.

alter table companies add column if not exists niche varchar(80) not null default 'GENERAL';
alter table companies add column if not exists address_line varchar(500);
alter table companies add column if not exists latitude numeric(9,6);
alter table companies add column if not exists longitude numeric(9,6);
create index if not exists idx_companies_niche on companies(niche);
create index if not exists idx_companies_geo on companies(latitude, longitude);

create table if not exists favorites (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    user_id uuid not null references app_users(id) on delete cascade,
    company_id uuid not null references companies(id) on delete cascade,
    unique(user_id, company_id)
);

create table if not exists waitlist_entries (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    user_id uuid not null references app_users(id) on delete cascade,
    company_id uuid not null references companies(id) on delete cascade,
    service_offering_id uuid references service_offerings(id) on delete set null,
    preferred_employee_id uuid references employees(id) on delete set null,
    preferred_start timestamptz,
    preferred_end timestamptz,
    latitude numeric(9,6),
    longitude numeric(9,6),
    status varchar(40) not null default 'ACTIVE',
    notified_at timestamptz,
    expires_at timestamptz
);

create index if not exists idx_waitlist_match on waitlist_entries(company_id, status, preferred_start);
create index if not exists idx_waitlist_geo on waitlist_entries(latitude, longitude);

create table if not exists group_bookings (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    appointment_id uuid not null references appointments(id) on delete cascade,
    organizer_user_id uuid not null references app_users(id),
    max_participants integer not null check(max_participants > 0),
    status varchar(40) not null default 'OPEN'
);

create table if not exists group_booking_participants (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    group_booking_id uuid not null references group_bookings(id) on delete cascade,
    user_id uuid not null references app_users(id) on delete cascade,
    display_name varchar(255) not null,
    amount_due numeric(12,2) not null default 0,
    payment_status varchar(40) not null default 'PENDING',
    unique(group_booking_id, user_id)
);

create table if not exists payment_transactions (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid references companies(id) on delete set null,
    appointment_id uuid references appointments(id) on delete set null,
    payer_user_id uuid references app_users(id) on delete set null,
    provider varchar(60) not null,
    method varchar(30) not null,
    amount numeric(12,2) not null check(amount >= 0),
    platform_fee numeric(12,2) not null default 0,
    partner_amount numeric(12,2) not null default 0,
    status varchar(40) not null default 'PENDING',
    external_reference varchar(500),
    metadata varchar(8000)
);

create table if not exists subscription_plans (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    name varchar(255) not null,
    description varchar(2000),
    interval_unit varchar(20) not null default 'MONTH',
    interval_count integer not null default 1,
    price numeric(12,2) not null,
    active boolean not null default true
);

create table if not exists subscriptions (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    plan_id uuid not null references subscription_plans(id),
    user_id uuid not null references app_users(id),
    status varchar(40) not null default 'ACTIVE',
    current_period_start timestamptz not null,
    current_period_end timestamptz not null,
    external_reference varchar(500)
);

create table if not exists custom_forms (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    name varchar(255) not null,
    niche varchar(80) not null,
    schema_json text not null,
    active boolean not null default true
);

create table if not exists form_submissions (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    form_id uuid not null references custom_forms(id) on delete cascade,
    client_id uuid not null references clients(id) on delete cascade,
    appointment_id uuid references appointments(id) on delete set null,
    answers_json text not null
);

create table if not exists professional_documents (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    employee_id uuid references employees(id) on delete cascade,
    document_type varchar(80) not null,
    storage_reference varchar(1000) not null,
    status varchar(40) not null default 'PENDING',
    reviewed_by uuid references app_users(id),
    reviewed_at timestamptz,
    rejection_reason varchar(1000)
);

create table if not exists calendar_connections (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    employee_id uuid references employees(id) on delete cascade,
    provider varchar(30) not null,
    external_account_id varchar(500),
    calendar_id varchar(500),
    encrypted_refresh_token_ref varchar(1000),
    sync_cursor varchar(1000),
    active boolean not null default true,
    unique(employee_id, provider, calendar_id)
);

create table if not exists triage_sessions (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    user_id uuid references app_users(id) on delete set null,
    niche varchar(80) not null,
    input_text varchar(10000) not null,
    recommendation_json text,
    consent_at timestamptz
);

create table if not exists cashback_ledger (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    user_id uuid not null references app_users(id) on delete cascade,
    source_company_id uuid references companies(id) on delete set null,
    amount numeric(12,2) not null,
    reason varchar(255) not null,
    expires_at timestamptz
);

create table if not exists gamification_events (
    id uuid primary key,
    created_at timestamptz not null,
    user_id uuid not null references app_users(id) on delete cascade,
    event_type varchar(80) not null,
    points integer not null,
    metadata varchar(4000)
);

create table if not exists ar_experiences (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    service_offering_id uuid references service_offerings(id) on delete set null,
    asset_reference varchar(1000) not null,
    configuration_json text,
    active boolean not null default true
);

create table if not exists platform_subscriptions (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    plan_name varchar(120) not null,
    commission_percent numeric(5,2) not null default 0,
    status varchar(40) not null default 'ACTIVE',
    external_reference varchar(500)
);

create index if not exists idx_favorites_user on favorites(user_id);
create index if not exists idx_group_booking_appointment on group_bookings(appointment_id);
create index if not exists idx_payments_company_created on payment_transactions(company_id, created_at desc);
create index if not exists idx_subscriptions_user_status on subscriptions(user_id, status);
create index if not exists idx_forms_company_niche on custom_forms(company_id, niche);
create index if not exists idx_documents_status on professional_documents(status);
create index if not exists idx_calendar_company on calendar_connections(company_id);
create index if not exists idx_triage_user_created on triage_sessions(user_id, created_at desc);
create index if not exists idx_cashback_user on cashback_ledger(user_id, created_at desc);
create index if not exists idx_gamification_user on gamification_events(user_id, created_at desc);
create index if not exists idx_ar_company on ar_experiences(company_id);
create index if not exists idx_platform_subscriptions_status on platform_subscriptions(status);
