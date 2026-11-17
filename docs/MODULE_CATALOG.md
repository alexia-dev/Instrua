# Catálogo comercial de módulos do Instrua

O Instrua adota contratação modular: cada organização mantém o **Instrua Essencial (CORE)** como base e adiciona os módulos operacionais adequados ao seu negócio.

Preços não são definidos nesta especificação. Devem ser configurados no catálogo comercial/provedor de pagamentos, sem codificar valores no backend.

## Módulos

| Código | Nome comercial | Categoria | Obrigatório | Dependências |
|---|---|---|---|---|
| CORE | Instrua Essencial | Core | Sim | — |
| AGENDA | Instrua Agenda | Operacional | Não | CORE |
| PACIENTES | Instrua Pacientes | Operacional | Não | CORE |
| ATENDE | Instrua Atende | Comunicação | Não | CORE, PACIENTES |
| CLINICO | Instrua Clínico | Clínico | Não | CORE, PACIENTES |
| AUTORIZA | Instrua Autoriza | Operacional | Não | CORE, AGENDA, PACIENTES |
| OPORTUNIDADES | Instrua Oportunidades | Operacional | Não | CORE, AGENDA, PACIENTES, ATENDE |
| FINANCEIRO | Instrua Financeiro | Gestão | Não | CORE |
| FISCAL | Instrua Fiscal | Gestão | Não | CORE, FINANCEIRO |
| BI | Instrua BI | Gestão | Não | CORE |
| PROTEGE | Instrua Protege | Segurança | Não | CORE |
| LAUDOS | Instrua Laudos | Especializado | Não | CORE, PACIENTES, AGENDA |
| SADT | Instrua SADT | Especializado | Não | CORE, AGENDA, PACIENTES |
| ESTOQUE | Instrua Estoque | Gestão | Não | CORE |
| TELEMEDICINA | Instrua Telemedicina | Especializado | Não | CORE, AGENDA, PACIENTES |
| MULTIUNIDADES | Instrua Multiunidades | Expansão | Não | CORE |
| PORTAL | Instrua Portal | Comunicação | Não | CORE, PACIENTES |
| RH | Instrua RH | Gestão | Não | CORE |

## Regras de assinatura

1. Uma assinatura pertence a uma organização (company_id), não a um usuário individual.
2. Cada organização pode ter uma assinatura por módulo.
3. CORE é obrigatório e não pode ser removido enquanto a organização possuir módulos contratados.
4. Um módulo só pode ser ativado quando todas as dependências estiverem ativas ou incluídas no mesmo pedido.
5. A confirmação de pagamento deve vir de webhook autenticado do provedor; nunca liberar acesso com base apenas no navegador.
6. Cancelamento ou downgrade não apaga dados. Aplicar política de retenção e exportação.
7. Estados distintos: PENDING, TRIALING, ACTIVE, PAST_DUE, SUSPENDED e CANCELED.
8. Limites de usuários, unidades, mensagens, armazenamento e IA devem ser definidos em camada própria de entitlements.
9. Segurança básica, privacidade, autenticação, isolamento de tenant e auditoria permanecem no CORE. PROTEGE oferece governança e recursos avançados.
10. O catálogo técnico não contém preços, impostos, moeda, periodicidade ou descontos.

## Perfis de contratação sugeridos

- Consultório: CORE + AGENDA + PACIENTES.
- Clínica de consultas: CORE + AGENDA + PACIENTES + ATENDE + AUTORIZA.
- Centro de exames: CORE + AGENDA + PACIENTES + ATENDE + AUTORIZA + OPORTUNIDADES + SADT + LAUDOS.
- Rede: módulos operacionais necessários + MULTIUNIDADES + BI + PROTEGE.

São composições de referência, não pacotes obrigatórios. O cliente pode contratar módulos avulsos respeitando dependências.

## Endpoints iniciais

- GET /api/v1/modules/catalog: lista o catálogo ativo.
- GET /api/v1/companies/{companyId}/modules: lista assinaturas da organização, validando acesso ao tenant.

A criação/alteração de assinatura e o provisionamento de pagamentos ficam para a próxima etapa, após integração com provedor de cobrança e política de aprovação.
