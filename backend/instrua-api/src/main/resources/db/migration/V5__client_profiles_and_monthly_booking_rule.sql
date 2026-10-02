alter table clients
    add column client_type varchar(30) not null default 'PERSON',
    add column legal_name varchar(255),
    add column trade_name varchar(255),
    add column contact_name varchar(255),
    add column profile_data jsonb not null default '{}'::jsonb;

create index idx_appointments_client_start on appointments(client_id, starts_at);
