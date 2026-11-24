# Instrua — Auditoria inicial e plano de implementação

**Branch auditada:** `feat/instrua-backend-foundation`  
**Commit auditado:** `a550e36317ddafacd70c653a2f4b5912517d8b10`  
**Data:** 2026-10-28  
**Objetivo:** mapear o briefing do produto contra o código existente antes de ampliar a implementação.

> O projeto está em desenvolvimento. A existência de uma tela, controller ou entidade não comprova que o fluxo esteja completo, conectado, seguro ou pronto para produção. Não usar dados reais de pacientes até concluir implementação, testes e revisão de segurança.

## 1. Resumo executivo

O repositório já possui uma base React/TypeScript, API Java 21/Spring Boot, PostgreSQL/Flyway e módulos iniciais para identidade, empresas, equipe, pacientes/clientes, serviços, agendamentos, instruções, notificações, documentos clínicos, financeiro, jornada, oportunidades, social, relatórios, integrações, IA, auditoria e controle de tenant.

A interface publicada é demonstrativa: `clients/instrua-app/src/App.tsx` contém estados locais e dados de exemplo; o fluxo `enterDemo` entra sem autenticação real e informa que os dados não são enviados a serviço real. A PR #8 continua draft e não mesclada. A descrição da PR confirma que o cliente ainda não está conectado ponta a ponta à API.

## 2. Estado encontrado por área

| Área | Evidência no repositório | Avaliação inicial |
|---|---|---|
| Landing page | React contém fluxo `landing` | Interface demonstrativa; revisar fidelidade ao design oficial e CTAs. |
| Login | React contém fluxo `login`; API `/api/v1/auth/login` | Não conectado ponta a ponta; autenticação real não deve ser presumida no frontend. |
| Criar conta | API `/register` para proprietário de empresa e `/register/patient` para paciente | Cadastro genérico multi-papel e verificação de contato ainda não comprovados. |
| Recuperação de conta | Nenhum fluxo de recuperação confirmado na inspeção inicial | Lacuna a implementar. |
| Conta única/múltiplos papéis | `User`, `Role`, `CurrentUser`; enum atual contém PLATFORM_ADMIN, COMPANY_OWNER, COMPANY_ADMIN, RECEPTION, CLINICAL, BILLING, PATIENT | Base parcial. O enum sozinho não modela todos os contextos, vínculos e permissões solicitados. |
| Organizações/empresas | `companies/`, `employees/`, `TenantAccessService` | Base parcial; verificar convites, unidades/filiais, vínculos e revogação. |
| Agenda | Entidades, repositório, serviço e controllers em `appointments/` | API inicial existente; ainda falta validar todas as regras, UI conectada, concorrência e notificações. |
| Minha vez/fila | Não aparece como módulo dedicado na árvore inspecionada | Lacuna importante: fila real, check-in, chamada, posição e estimativa. |
| Pacientes/clientes | `patients/` e `clients/` | Base parcial; validar consistência de identidade e separação de papéis. |
| Orientações | `instructions/` | API inicial; validar anexos, aprovação, entrega, confirmação de leitura e integração com agendamentos. |
| Documentos clínicos | `clinical/ClinicalDocumentRequestController`, `ClinicalRecordAccessService` | Base parcial; exige testes negativos de autorização e fluxo completo. |
| Rede Social | `social/SocialController`, `SocialModerationPolicy` | API inicial; validar comentários, reações, comunidades, privacidade, denúncias e moderação completa. |
| Oportunidades | `opportunities/OpportunityController` | API inicial com catálogo/publicação/candidaturas; validar regras, UI, auditoria e notificações. |
| Financeiro | `finance/FinanceEntryController`, `PaymentStateMachine` | Base parcial; nenhum provedor de pagamento real comprovado. |
| Fiscal/NFS-e | Não foi identificado módulo fiscal dedicado na árvore inicial | Lacuna; depende de desenho e integração fiscal. |
| Protege/jornada | `journey/JourneyController`, `JourneyCorrectionPolicy` | Base parcial; validar trilha imutável, aprovação e relatórios. |
| Notificações | `notifications/` | API inicial; não comprova entrega por e-mail, push, SMS ou WhatsApp. |
| Dante IA | `ai/AiController`, `AiService` | Base inicial; ações precisam de autorização, limites e aprovação conforme risco. |
| Radar de Imprevistos | Nenhum módulo dedicado identificado | Lacuna de produto e implementação. |
| Sugestões/votação | Nenhum módulo dedicado identificado | Lacuna de produto e implementação. |
| Mapa/localização | Nenhum módulo dedicado identificado | Lacuna; não inventar endereços ou coordenadas. |
| Configurações e preferências | Controles locais no frontend | Não equivalem a preferências persistidas por usuário/contexto. |
| Auditoria | `common/audit/` | Base parcial; revisar cobertura, integridade e acesso aos logs. |
| Segurança/tenant | `common/security/`, `common/tenant/` | Base inicial; exige testes de autorização por organização e contexto em todos os endpoints. |
| Frontend/API | Frontend usa estados locais e dados seed em `App.tsx` | Integração ponta a ponta não concluída. |
| Infraestrutura/produção | Documentação de arquitetura existe | Não significa que produção segura esteja implantada. |

## 3. Lacunas prioritárias

### P0 — Segurança e identidade antes de dados reais
1. Mapear a entidade User, roles, autenticação, token e políticas atuais.
2. Definir modelo explícito de papéis globais, vínculos, organização, unidade e permissões contextuais, evitando usar apenas um enum global.
3. Criar testes automatizados de isolamento entre organizações para todos os domínios sensíveis.
4. Validar o acesso ao prontuário e a titularidade do próprio paciente em cada caminho.
5. Definir sessões, recuperação de conta, verificação de contato e armazenamento seguro de token no cliente.
6. Manter dados demonstrativos claramente identificados e bloquear uso clínico até revisão de segurança.

### P1 — Conta única e operações essenciais
1. Cadastro/login/recuperação completos, sem duplicar identidade ao adicionar papéis.
2. Vínculos de organização, convites com aceite/recusa/expiração/revogação e trilha de auditoria.
3. Agenda com disponibilidade, conflitos, cancelamento em lote por indisponibilidade, opções de remarcação e histórico.
4. Notificações com outbox/idempotência, estado de entrega, preferências e adaptadores.
5. Fila de atendimento real, check-in, chamada, estados e painel da recepção.
6. Orientações e solicitações clínicas conectadas ao agendamento e ao paciente, com autorização profissional.

### P2 — Descoberta e comunidade
1. Busca e catálogo de serviços/profissionais/estabelecimentos com disponibilidade real.
2. Rede Social com feed persistente, comentários/reação, comunidades, eventos, denúncia e moderação.
3. Oportunidades com publicação, candidaturas, vagas liberadas/lista de espera e regras claras.
4. Mapa, dados de chegada e integração com serviço de mapas usando dados verificados.
5. Sugestões de melhorias com votação, estados e moderação.

### P3 — Gestão e integrações
1. Financeiro com pagamentos/repasses/conciliação e integração de provedor.
2. Fiscal/NFS-e com integração homologada e tratamento de falhas.
3. Protege com jornada, correções aprovadas e auditoria.
4. Radar de Imprevistos com explicação da recomendação, simulação e registro de decisão.
5. Dante IA com ferramentas autorizadas e confirmação de ações sensíveis.
6. Relatórios, administração de plataforma, acessibilidade e internacionalização.

## 4. Plano de execução verificável

Cada entrega deverá conter: requisito relacionado, arquivos alterados, migração quando necessária, endpoints/contratos, UI conectada, testes unitários, testes de integração, testes de autorização/isolamento, instruções de configuração e limitações conhecidas.

1. **Fundação de identidade e autorização:** inspecionar e testar User, Role, JWT, CurrentUser, DomainAccessService, TenantAccessService, Company/Employee e migrations. Especificar e implementar modelo de múltiplos papéis/contextos sem quebrar dados existentes.
2. **Convites e vínculos:** convites com estado, expiração, aceite/recusa, revisão de permissões, revogação e auditoria.
3. **Agenda operacional:** disponibilidade e conflitos; cancelamento em lote com alternativas de remarcação, histórico e notificações idempotentes.
4. **Fila e check-in:** estados persistidos e painel operacional.
5. **Orientações e documentos clínicos:** autorização por contexto, entrega e histórico.
6. **Conectar o frontend às APIs:** remover dados fictícios das telas que já tiverem API pronta; estados loading/empty/error e mensagens claras.
7. **Social e Oportunidades:** persistência, moderação, privacidade e notificações.
8. **Financeiro/Fiscal/Protege/Radar/Dante:** implementar módulo a módulo com testes e conectores configuráveis.
9. **Hardening e implantação:** backups/restauração, logs, monitoramento, revisão de LGPD, acessibilidade, segurança e checklist de produção.

## 5. Critérios de aceite globais

- Nenhuma funcionalidade marcada como pronta apenas por existir uma tela, classe ou endpoint.
- O frontend mostra claramente quando está em demonstração e não afirma que dados fictícios são reais.
- O backend valida autorização e contexto em toda operação; menus ocultos não são controle de segurança.
- Usuários e organizações não conseguem acessar registros de terceiros por troca de IDs.
- Mudanças de agenda e notificações são idempotentes e auditáveis.
- Migrações funcionam em banco vazio e em upgrade a partir do esquema anterior.
- Testes e build são executados e seus resultados registrados no PR.
- Integrações externas usam segredos configurados fora do código e têm modo não configurado explícito.
- Nenhum uso com dados reais de saúde antes de revisão independente de segurança e validação legal.

## 6. Referências internas

- [README](../README.md)
- [Implementação do backend](INSTRUA_BACKEND_IMPLEMENTATION.md)
- [Sistema de design](INSTRUA_DESIGN_SYSTEM.md)
- [Arquitetura dos clientes](PLATFORM_CLIENTS_ARCHITECTURE.md)
- [Estrutura do projeto](PROJECT_STRUCTURE.md)
- [Arquitetura de hospedagem e segurança](DEPLOYMENT_SECURITY_ARCHITECTURE.md)
- [PR #8](https://github.com/alexia-dev/Instrua/pull/8)

