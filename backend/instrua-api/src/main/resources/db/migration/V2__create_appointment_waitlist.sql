create table appointment_waitlist_entries (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    client_id uuid not null references clients(id),
    service_offering_id uuid not null references service_offerings(id),
    preferred_from timestamptz,
    preferred_until timestamptz,
    priority integer not null default 0,
    status varchar(40) not null default 'WAITING',
    offered_appointment_id uuid references appointments(id) on delete set null,
    offer_expires_at timestamptz,
    accepted_at timestamptz,
    notes varchar(2000),
    check (preferred_until is null or preferred_from is null or preferred_until >= preferred_from)
);
create index idx_waitlist_company_service_status
    on appointment_waitlist_entries(company_id, service_offering_id, status, priority desc, created_at asc);
create index idx_waitlist_offer_expiry
    on appointment_waitlist_entries(status, offer_expires_at);
