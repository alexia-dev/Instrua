-- V7: reusable personal identity, accessible preferences, and explicit company-level sharing.
-- Administrative data is separate from private clinical notes.
alter table user_profiles add column if not exists rg varchar(30);
alter table user_profiles add column if not exists postal_code varchar(12);
alter table user_profiles add column if not exists street varchar(255);
alter table user_profiles add column if not exists street_number varchar(30);
alter table user_profiles add column if not exists complement varchar(150);
alter table user_profiles add column if not exists neighborhood varchar(150);
alter table user_profiles add column if not exists city varchar(150);
alter table user_profiles add column if not exists state varchar(2);
alter table user_profiles add column if not exists emergency_contact_name varchar(255);
alter table user_profiles add column if not exists emergency_contact_phone varchar(60);
alter table user_profiles add column if not exists theme varchar(15) not null default 'LIGHT';
alter table user_profiles add column if not exists font_scale numeric(3,2) not null default 1.00;
alter table user_profiles add column if not exists high_contrast boolean not null default false;
alter table user_profiles add constraint ck_user_profile_font_scale check (font_scale between 0.90 and 1.40);
alter table user_profiles add constraint ck_user_profile_theme check (theme in ('LIGHT','DARK'));

-- The establishment owns its own operational client snapshot. It is not a shared medical record.
alter table clients add column if not exists rg varchar(30);
alter table clients add column if not exists birth_date date;
alter table clients add column if not exists postal_code varchar(12);
alter table clients add column if not exists street varchar(255);
alter table clients add column if not exists street_number varchar(30);
alter table clients add column if not exists complement varchar(150);
alter table clients add column if not exists neighborhood varchar(150);
alter table clients add column if not exists city varchar(150);
alter table clients add column if not exists state varchar(2);
alter table clients add column if not exists linked_user_id uuid references app_users(id) on delete set null;
create index if not exists idx_clients_tenant_email on clients(company_id, lower(email));
create index if not exists idx_clients_tenant_phone on clients(company_id, phone);
create index if not exists idx_clients_linked_user on clients(company_id, linked_user_id);

-- A person opts in per company. Searching an email or phone is NOT consent.
create table if not exists profile_shares (
 user_id uuid not null references app_users(id) on delete cascade,
 company_id uuid not null references companies(id) on delete cascade,
 permitted boolean not null default false,
 updated_at timestamptz not null default now(),
 primary key (user_id, company_id)
);
create index if not exists idx_profile_shares_company on profile_shares(company_id, permitted);

-- Private, self-managed wellness notes. Never sent to reception via client lookup.
create table if not exists user_private_health (
 user_id uuid primary key references app_users(id) on delete cascade,
 allergies varchar(2000),
 height_cm numeric(5,2) check(height_cm is null or height_cm between 20 and 300),
 updated_at timestamptz not null default now()
);
