-- Instrua vacancy opportunity / auction module.
-- The module is enabled per company; discounts are always optional and explicit.

alter table companies
    add column if not exists vacancy_auction_enabled boolean not null default false;
alter table companies
    add column if not exists vacancy_allow_discount boolean not null default false;
alter table companies
    add column if not exists vacancy_default_expiry_minutes integer not null default 30;
alter table companies
    add column if not exists vacancy_default_reservation_minutes integer not null default 5;

create table if not exists vacancy_opportunities (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    service_offering_id uuid not null references service_offerings(id),
    employee_id uuid references employees(id) on delete set null,
    starts_at timestamptz not null,
    ends_at timestamptz not null,
    slots integer not null check (slots > 0),
    eligible_audience varchar(80),
    published_until timestamptz,
    notes varchar(2000),
    status varchar(40) not null default 'DRAFT',
    regular_price numeric(12,2) not null check (regular_price >= 0),
    discount_type varchar(20),
    discount_value numeric(12,2),
    final_price numeric(12,2),
    constraint vacancy_discount_type_ck check (discount_type in ('PERCENT','FIXED') or discount_type is null),
    constraint vacancy_discount_value_ck check (discount_value is null or discount_value >= 0)
);

create table if not exists vacancy_reservations (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    opportunity_id uuid not null references vacancy_opportunities(id) on delete cascade,
    user_id uuid not null references app_users(id) on delete cascade,
    expires_at timestamptz not null,
    status varchar(40) not null default 'HELD',
    unique(opportunity_id, user_id)
);

create index if not exists idx_vacancy_company_status on vacancy_opportunities(company_id, status, starts_at);
create index if not exists idx_vacancy_public on vacancy_opportunities(status, published_until, starts_at);
create index if not exists idx_vacancy_reservations_expiry on vacancy_reservations(status, expires_at);
create index if not exists idx_vacancy_reservations_user on vacancy_reservations(user_id, status);

create table if not exists vacancy_opportunity_events (
    id uuid primary key,
    created_at timestamptz not null,
    opportunity_id uuid not null references vacancy_opportunities(id) on delete cascade,
    actor_user_id uuid references app_users(id) on delete set null,
    event_type varchar(60) not null,
    metadata varchar(4000)
);
