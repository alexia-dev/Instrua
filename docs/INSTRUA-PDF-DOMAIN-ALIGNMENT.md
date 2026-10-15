# Instrua — contrato de domínio alinhado ao produto

> Documento de implementação. A referência visual e de experiência do produto é o PDF oficial do Instrua. Este documento traduz os fluxos do produto para responsabilidades de backend; não substitui o PDF nem presume integrações externas já contratadas.

## 1. Direção do produto

O Instrua é uma plataforma de gestão da jornada de atendimento que conecta pacientes/clientes, profissionais e estabelecimentos. O backend deve sustentar as áreas apresentadas no produto: área pública e autenticação, área do paciente, área do profissional, recepção, administração do estabelecimento, administração da plataforma, agenda/atendimentos, documentos, financeiro/fiscal conforme permissões, Instrua Protege, Instrua Registros, Instrua Clínico restrito, Oportunidades e Dante IA com revisão humana.

A interface pode evoluir independentemente, mas cada fluxo deve consumir contratos explícitos da API. Dados demonstrativos ficam no frontend ou em fixtures de desenvolvimento; nunca devem ser confundidos com dados reais.

## 2. Arquitetura-alvo

- Java 21, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL e Flyway.
- Monólito modular com módulos e limites de domínio claros.
- API REST documentada; DTOs de entrada/saída separados das entidades JPA.
- Autorização obrigatória no servidor, por permissão e contexto de organização/tenant.
- Auditoria de ações sensíveis, migrações versionadas e testes automatizados.
- Integrações externas atrás de interfaces/adapters; nenhuma integração é considerada ativa só porque existe uma tela.

## 3. Módulos e responsabilidades

| Módulo | Responsabilidade |
| --- | --- |
| Identidade e acesso | autenticação, recuperação de acesso, convites, usuários, papéis e permissões |
| Organizações | estabelecimentos, unidades, vínculo profissional/estabelecimento e isolamento por tenant |
| Pacientes | cadastro e acesso do paciente aos próprios dados, com minimização de dados |
| Agenda e atendimentos | disponibilidade, agendamentos, confirmações, cancelamentos, check-in e histórico |
| Instruções e notificações | instruções pré-atendimento, modelos, preferências, fila e estado de entrega |
| Recepção | operações permitidas de agenda, cadastro e confirmação, sem acesso clínico irrestrito |
| Documentos e registros | documentos associados, versões, origem, permissões e trilha de acesso |
| Instrua Clínico | conteúdo clínico restrito a profissionais autorizados e contexto de atendimento |
| Instrua Protege | jornada de trabalho, ocorrências, divergências, solicitações de correção e auditoria |
| Financeiro e fiscal | lançamentos e documentos por responsável legal/emissor, com permissões e integração própria |
| Oportunidades | publicação, candidatura/reserva, estado e expiração; não modelar como leilão/lances |
| Dante IA | pedidos de assistência, contexto mínimo, resultado proposto e aprovação humana |
| Administração | gestão da plataforma separada da administração de cada estabelecimento |
| Auditoria | eventos relevantes com ator, tenant, ação, alvo, data e resultado |

## 4. Perfis iniciais e limites

Os nomes abaixo são papéis de referência, não autorização implícita por nome:

- PLATFORM_ADMIN: operação global estritamente administrativa; acesso a dados de saúde não é automático.
- COMPANY_OWNER / COMPANY_ADMIN: gestão do estabelecimento conforme permissões delegadas.
- RECEPTION: agenda, check-in, confirmação e dados cadastrais mínimos necessários.
- PROFESSIONAL: agenda própria e dados necessários ao atendimento.
- CLINICAL: acesso clínico apenas quando autorizado e relacionado ao atendimento.
- BILLING: módulo financeiro/fiscal e somente os estabelecimentos/itens permitidos.
- PATIENT: somente o próprio perfil, compromissos e documentos que lhe pertençam.
- AUDITOR (quando habilitado): leitura de trilhas e registros autorizados, sem edição.

Usar permissões granulares por recurso/ação. A interface pode ocultar ações, mas a API precisa validar cada requisição. Convites devem criar vínculos com organização e permissões definidas; não exigir cadastro manual indiscriminado de todas as contas.

## 5. Regras de segurança e privacidade

1. Toda consulta e mutação pertencente a um estabelecimento deve validar o tenant no servidor.
2. Nunca confiar em tenantId, companyId, papel ou userId enviados pelo cliente sem validar a identidade e o vínculo.
3. Aplicar menor privilégio, validação de entrada, paginação e respostas sem campos desnecessários.
4. Auditar acesso/alteração de registros sensíveis, permissões, documentos, correções e decisões administrativas.
5. Não registrar tokens, senhas, dados clínicos completos ou documentos pessoais em logs.
6. Não usar dados reais em fixtures, testes, exemplos ou commits.
7. Acesso clínico, emissão fiscal, pagamentos, controle oficial de jornada e envio de mensagens só podem ser considerados reais após implementação e validação das integrações e requisitos aplicáveis.

## 6. Financeiro e documentos fiscais

O escopo atualizado do produto inclui uma área financeira/fiscal, mas o repositório antigo descreve o financeiro como pertencente ao Nexa Bill. Antes de mover ou duplicar esse domínio, manter uma fronteira de integração explícita. O backend do Instrua não deve emitir documento fiscal em nome de profissional ou estabelecimento sem identificar o prestador/emissor correto, suas autorizações e o fluxo legal aplicável. Registrar estados e referências de integração; não simular sucesso de emissão.

## 7. Dante IA — assistência com revisão humana

A API deve tratar respostas da IA como sugestões, não como decisões finais. Guardar referência da solicitação, usuário responsável, contexto mínimo necessário, estado (pendente, aprovado, rejeitado, falhou), versão do provedor/modelo quando disponível e trilha de revisão. A IA não pode alterar prontuário, conceder acesso, enviar mensagens externas ou concluir atos financeiros/fiscais sem autorização explícita e validações próprias.

## 8. Contratos e estados recomendados

- Agendamento: PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW.
- Convite: PENDING, ACCEPTED, EXPIRED, REVOKED.
- Correção de registro/jornada: REQUESTED, UNDER_REVIEW, APPROVED, REJECTED.
- Sugestão de IA: PENDING_REVIEW, APPROVED, REJECTED, FAILED.
- Documento fiscal/integrado: estado informado pelo provedor, sem marcar como emitido antes de confirmação externa.

Estados definitivos devem ser harmonizados com o contrato da interface e com as regras operacionais antes de criar migrações irreversíveis.

## 9. Plano de adaptação do backend existente

### Etapa 1 — preservar e estabilizar
- Manter Java/Spring Boot, PostgreSQL, Flyway, autenticação, organizações, pacientes, agenda, instruções, notificações e auditoria que já estejam corretos.
- Mapear entidades, endpoints, permissões e migrações existentes antes de alterá-los.
- Corrigir isolamento multi-tenant e autorização no servidor antes de abrir novos módulos.

### Etapa 2 — contratos da API
- Publicar DTOs e endpoints com paginação, validação e erros consistentes.
- Documentar os contratos para frontend web/mobile.
- Criar testes de integração para permissões, isolamento entre estabelecimentos e transições de estado.

### Etapa 3 — ampliar os domínios
- Adicionar convites/permissões granulares, recepção, documentos/registros, Instrua Protege e área administrativa.
- Definir a fronteira de Financeiro/Fiscal com Nexa Bill antes de implementar emissão ou cobrança.
- Criar Oportunidades como fluxo de publicação e reserva/candidatura, sem leilão.
- Adicionar Dante IA somente por adapter, com revisão humana e auditoria.

### Etapa 4 — validação
- Executar testes Maven e testes de migração em banco descartável.
- Verificar que usuários de uma organização não leem nem alteram dados de outra.
- Confirmar os contratos com a interface do PDF e registrar lacunas.
- Não declarar pronto para produção sem configuração de segredos, backups, monitoramento, política de retenção e revisão de segurança.

## 10. Critério de pronto

Um módulo só é considerado implementado quando tiver regras de domínio, autorização no servidor, persistência/migração quando necessária, contrato de API, auditoria apropriada e testes. Uma tela ou endpoint isolado não comprova que o fluxo está funcional.


## 11. Primeira alteração de código aplicada nesta branch

- Adicionados os papéis `PROFESSIONAL` e `AUDITOR` ao enum de usuários para representar melhor a área profissional e a leitura de auditoria descritas no produto.
- Habilitada a autorização por método do Spring Security.
- Protegidas as rotas de agenda com autorização por papel: leitura para administração, recepção, profissionais e equipe clínica; criação e mudança de estado para administração, recepção e profissionais.
- O controle por papel complementa — não substitui — a validação de vínculo com o estabelecimento feita pelos serviços de tenant.

**Validação pendente:** os testes Maven ainda precisam ser executados. Também é necessário revisar os demais controllers e adicionar testes negativos de acesso para garantir que PATIENT e BILLING não consigam operar a agenda por estas rotas, além de validar a matriz de permissões com o PDF.
