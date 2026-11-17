# Arquitetura modular do Instrua

## Decisão

Manter um **monólito modular** em Java 21 + Spring Boot + PostgreSQL. Os módulos são domínios internos com contratos explícitos, não microsserviços separados nesta fase.

## Camadas

- common: entidades-base, exceções, auditoria e utilitários transversais.
- auth e users: identidade e autenticação.
- companies e employees: organizações, unidades/vínculos e equipe.
- modules: catálogo, dependências e assinaturas por organização.
- Domínios funcionais existentes (appointments, patients, instructions, notifications, integrations, reports, services) serão migrados gradualmente para fronteiras de módulo. Não mover classes em massa sem revisar imports, testes, endpoints e compatibilidade.

## Mapeamento funcional

| Módulo comercial | Domínios atuais / futuros |
|---|---|
| CORE | auth, users, companies, employees, common, modules |
| AGENDA | appointments, services, recursos de calendário |
| PACIENTES | patients, clients (consolidar nomenclatura com migração segura) |
| ATENDE | notifications, conversations, messaging, AI |
| CLINICO | prontuários, encontros, documentos clínicos |
| AUTORIZA | autorizações, elegibilidade e guias |
| OPORTUNIDADES | lista de espera, ofertas de vagas e orquestração de remarcação |
| FINANCEIRO | faturamento, contas a receber e pagar |
| FISCAL | notas e integrações fiscais |
| BI | relatórios, métricas e analytics |
| PROTEGE | auditoria, privacidade, consentimento e governança |
| LAUDOS | laudos clínicos, assinaturas e entrega |
| SADT | fluxos de diagnóstico e terapia |
| ESTOQUE | estoque e movimentações |
| TELEMEDICINA | consultas remotas e salas virtuais |
| MULTIUNIDADES | filiais e administração entre unidades |
| PORTAL | autoatendimento do paciente |
| RH | equipe, escalas e rotinas de RH |

## Isolamento e ativação

- Toda assinatura pertence a company_id.
- Toda requisição de dados de uma organização valida o tenant no backend.
- A autorização futura combina papel/permissão + vínculo com organização/unidade + entitlement do módulo + estado da assinatura.
- Não basta esconder menu no frontend. Controllers e services também devem negar operações sem entitlement.
- CORE inclui segurança básica e não é opcional.
- Dependências ficam registradas em module_dependencies.
- Catálogo técnico e catálogo de preços são separados.

## Banco de dados

Migration V4__create_modular_subscription_catalog.sql cria:
- platform_modules: catálogo.
- module_dependencies: dependências.
- company_module_subscriptions: assinatura por organização e módulo.

A migration não altera tabelas legadas nem remove dados existentes.

## Próximas etapas

1. Implementar verificação de entitlement nos serviços de cada domínio.
2. Criar fluxo de contratação e webhook autenticado do provedor de pagamento.
3. Criar histórico imutável de eventos de assinatura.
4. Criar limites de plano (usuários, unidades, mensagens, armazenamento e IA).
5. Criar painel administrativo Kiyubi e loja de módulos do cliente.
6. Migrar gradualmente os pacotes funcionais para fronteiras modules.*, com testes de regressão.
7. Adicionar testes de isolamento multi-tenant e dependências/cancelamento.

## Estado desta entrega

Esta etapa cria catálogo, persistência de assinaturas e endpoints de consulta. Não implementa cobrança, ativação automática, autorização de uso por entitlement, tela de contratação nem migração física de todos os pacotes. Esses itens permanecem pendentes.
