create table platform_modules (
    code varchar(40) primary key,
    name varchar(120) not null,
    description varchar(1000) not null,
    category varchar(40) not null,
    required_module boolean not null default false,
    active boolean not null default true,
    display_order integer not null default 0
);

create table module_dependencies (
    module_code varchar(40) not null references platform_modules(code) on delete cascade,
    required_module_code varchar(40) not null references platform_modules(code),
    primary key (module_code, required_module_code),
    check (module_code <> required_module_code)
);

create table company_module_subscriptions (
    id uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    company_id uuid not null references companies(id) on delete cascade,
    module_code varchar(40) not null references platform_modules(code),
    status varchar(24) not null,
    started_at timestamptz,
    current_period_start timestamptz,
    current_period_end timestamptz,
    trial_ends_at timestamptz,
    canceled_at timestamptz,
    external_subscription_id varchar(180),
    unique (company_id, module_code),
    check (status in ('PENDING', 'TRIALING', 'ACTIVE', 'PAST_DUE', 'SUSPENDED', 'CANCELED'))
);

create index idx_company_module_subscriptions_company on company_module_subscriptions(company_id, status);
create index idx_company_module_subscriptions_module on company_module_subscriptions(module_code, status);

insert into platform_modules (code, name, description, category, required_module, display_order) values
('CORE', 'Instrua Essencial', 'Núcleo obrigatório: empresas, usuários, autenticação, permissões e configurações.', 'CORE', true, 0),
('AGENDA', 'Instrua Agenda', 'Agendamentos, profissionais, serviços, salas, encaixes, cancelamentos e remarcações.', 'OPERACIONAL', false, 10),
('PACIENTES', 'Instrua Pacientes', 'Cadastro, contatos, documentos, convênios e relacionamento com pacientes.', 'OPERACIONAL', false, 20),
('ATENDE', 'Instrua Atende', 'WhatsApp, atendimento humano, automações, mensagens e inteligência artificial.', 'COMUNICACAO', false, 30),
('CLINICO', 'Instrua Clínico', 'Prontuários, evoluções, registros e documentos clínicos.', 'CLINICO', false, 40),
('AUTORIZA', 'Instrua Autoriza', 'Guias, convênios, solicitações e acompanhamento de autorizações.', 'OPERACIONAL', false, 50),
('OPORTUNIDADES', 'Instrua Oportunidades', 'Lista de espera, encaixes e antecipação de horários vagos.', 'OPERACIONAL', false, 60),
('FINANCEIRO', 'Instrua Financeiro', 'Contas a pagar e receber, fluxo de caixa e conciliação.', 'GESTAO', false, 70),
('FISCAL', 'Instrua Fiscal', 'Documentos fiscais e integrações de emissão.', 'GESTAO', false, 80),
('BI', 'Instrua BI', 'Indicadores, relatórios e análise operacional.', 'GESTAO', false, 90),
('PROTEGE', 'Instrua Protege', 'Recursos avançados de auditoria, privacidade e governança.', 'SEGURANCA', false, 100),
('LAUDOS', 'Instrua Laudos', 'Elaboração, revisão, assinatura e entrega de laudos.', 'ESPECIALIZADO', false, 110),
('SADT', 'Instrua SADT', 'Fluxos especializados de serviços auxiliares de diagnóstico e terapia.', 'ESPECIALIZADO', false, 120),
('ESTOQUE', 'Instrua Estoque', 'Controle de materiais, insumos e movimentações.', 'GESTAO', false, 130),
('TELEMEDICINA', 'Instrua Telemedicina', 'Atendimentos remotos e recursos de consulta virtual.', 'ESPECIALIZADO', false, 140),
('MULTIUNIDADES', 'Instrua Multiunidades', 'Gestão consolidada de filiais e unidades da organização.', 'EXPANSAO', false, 150),
('PORTAL', 'Instrua Portal', 'Autoatendimento e acesso do paciente aos serviços habilitados.', 'COMUNICACAO', false, 160),
('RH', 'Instrua RH', 'Gestão de equipe, escalas e rotinas de recursos humanos.', 'GESTAO', false, 170);

insert into module_dependencies (module_code, required_module_code) values
('AGENDA', 'CORE'), ('PACIENTES', 'CORE'),
('ATENDE', 'CORE'), ('ATENDE', 'PACIENTES'),
('CLINICO', 'CORE'), ('CLINICO', 'PACIENTES'),
('AUTORIZA', 'CORE'), ('AUTORIZA', 'AGENDA'), ('AUTORIZA', 'PACIENTES'),
('OPORTUNIDADES', 'CORE'), ('OPORTUNIDADES', 'AGENDA'), ('OPORTUNIDADES', 'PACIENTES'), ('OPORTUNIDADES', 'ATENDE'),
('FINANCEIRO', 'CORE'), ('FISCAL', 'CORE'), ('FISCAL', 'FINANCEIRO'),
('BI', 'CORE'), ('PROTEGE', 'CORE'),
('LAUDOS', 'CORE'), ('LAUDOS', 'PACIENTES'), ('LAUDOS', 'AGENDA'),
('SADT', 'CORE'), ('SADT', 'AGENDA'), ('SADT', 'PACIENTES'),
('ESTOQUE', 'CORE'), ('TELEMEDICINA', 'CORE'), ('TELEMEDICINA', 'AGENDA'), ('TELEMEDICINA', 'PACIENTES'),
('MULTIUNIDADES', 'CORE'), ('PORTAL', 'CORE'), ('PORTAL', 'PACIENTES'), ('RH', 'CORE');
