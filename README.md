# INSTRUA

### Plataforma modular de jornada, atendimento e gestão clínica

O **Instrua** acompanha a jornada entre clínica e paciente: agendamento, confirmação, check-in, instruções, notificações, comunicação e histórico dos compromissos. A plataforma utiliza uma arquitetura SaaS modular: cada organização contrata os módulos adequados à sua operação.

## Modelo de módulos

- **Instrua Essencial (CORE)** — base obrigatória: organizações, usuários, autenticação, permissões e configurações.
- **Módulos operacionais** — Agenda, Pacientes, Atende, Clínico, Autoriza e Oportunidades.
- **Módulos de gestão** — Financeiro, Fiscal, BI, Protege, RH e Estoque.
- **Módulos especializados** — Laudos, SADT, Telemedicina, Portal e Multiunidades.

O catálogo técnico, as dependências e o estado de assinatura ficam documentados em [docs/MODULE_CATALOG.md](docs/MODULE_CATALOG.md). A arquitetura está descrita em [docs/MODULAR_SAAS_ARCHITECTURE.md](docs/MODULAR_SAAS_ARCHITECTURE.md).

Os domínios financeiros e fiscais devem respeitar a separação de responsabilidades do ecossistema NEXA. O módulo comercial Instrua Financeiro pode oferecer acesso e integração aos fluxos habilitados, sem duplicar silenciosamente o domínio contábil/faturamento do Nexa Bill.

## Perfis

- **PLATFORM_ADMIN** — administração da plataforma;
- **COMPANY_OWNER** — responsável pela clínica;
- **COMPANY_ADMIN** — administração da clínica;
- **RECEPTION** — agenda, cadastro e confirmação;
- **CLINICAL** — fluxo clínico e instruções;
- **BILLING** — acesso financeiro quando o perfil possuir permissão e módulo;
- **PATIENT** — acesso apenas aos próprios dados e compromissos.

## Arquitetura

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- REST API
- PostgreSQL
- Flyway
- Docker

A direção arquitetural é **monólito modular**: domínios separados no código, API e banco central na primeira fase, com contratos explícitos entre módulos.

## Regras de segurança

Como a plataforma pode lidar com dados pessoais e dados de saúde:

- aplicar isolamento por clínica/tenant em todas as consultas;
- combinar papel, vínculo organizacional e entitlement do módulo;
- validar autorização no backend, nunca apenas na interface;
- manter auditoria para alterações relevantes;
- nunca versionar dados reais no repositório;
- armazenar segredos fora do código;
- preservar dados após cancelamento de assinatura, conforme política de retenção;
- preparar backups e políticas de recuperação antes de produção.

## Estrutura atual

```text
backend/instrua-api/
└── src/main/java/com/instrua/
    ├── auth/
    ├── users/
    ├── companies/
    ├── employees/
    ├── modules/       # catálogo e assinaturas SaaS
    ├── appointments/
    ├── patients/
    ├── clients/
    ├── instructions/
    ├── notifications/
    ├── integrations/
    ├── reports/
    └── common/
```

## Estado da implementação modular

A fundação inicial contém:

- catálogo persistido de módulos;
- dependências entre módulos;
- assinaturas por organização e estados de assinatura;
- endpoint de consulta do catálogo;
- endpoint de consulta dos módulos de uma organização com validação de acesso ao tenant.

Ainda precisam ser implementados: contratação e cancelamento, integração de cobrança/webhooks, provisionamento automático, verificação de entitlement em cada domínio, limites por plano e interface de gestão/loja de módulos.

🚧 Projeto em desenvolvimento.
