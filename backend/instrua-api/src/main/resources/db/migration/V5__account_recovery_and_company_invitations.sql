-- Account recovery and organization invitations. Tokens are stored as hashes, never raw secrets.
create table if not exists account_recovery_tokens (
  id uuid primary key,
  user_id uuid not null references app_users(id) on delete cascade,
  token_hash varchar(128) not null unique,
  expires_at timestamptz not null,
  consumed_at timestamptz,
  requested_at timestamptz not null default now(),
  request_ip_hash varchar(128),
  constraint ck_recovery_expiry check (expires_at > requested_at)
);
create index if not exists idx_recovery_user_expiry on account_recovery_tokens(user_id, expires_at);

create table if not exists company_invitations (
  id uuid primary key,
  company_id uuid not null references companies(id) on delete cascade,
  invited_email varchar(320) not null,
  invited_user_id uuid references app_users(id) on delete set null,
  proposed_role varchar(60) not null,
  proposed_unit_id uuid references company_units(id) on delete set null,
  proposed_permissions jsonb not null default '[]'::jsonb,
  token_hash varchar(128) not null unique,
  status varchar(24) not null default 'PENDING',
  message varchar(2000),
  created_by uuid not null references app_users(id),
  created_at timestamptz not null default now(),
  expires_at timestamptz not null,
  responded_at timestamptz,
  response_note varchar(2000),
  constraint ck_company_invitation_status check (status in ('PENDING','ACCEPTED','DECLINED','EXPIRED','REVOKED')),
  constraint ck_company_invitation_expiry check (expires_at > created_at),
  constraint ck_company_invitation_response check (status = 'PENDING' or responded_at is not null)
);
create index if not exists idx_company_invitation_company_status on company_invitations(company_id, status, created_at desc);
create index if not exists idx_company_invitation_email_status on company_invitations(lower(invited_email), status);
