# Instrua — agenda, disponibilidade e bloqueios

## Objetivo desta etapa

Definir e implementar progressivamente a primeira fatia funcional da agenda sem substituir a arquitetura existente nem expor alterações incompletas na branch principal.

## Estado confirmado no código antes desta etapa

- O agendamento já usa a duração do serviço cadastrado em `service_offerings.duration_minutes`.
- O cancelamento já exige motivo em `AppointmentService.changeStatus`.
- O acesso à empresa passa por `CompanyService.requireAccess(companyId)`.
- A verificação de conflito existente cobre o profissional atribuído, mas não implementa disponibilidade recorrente, bloqueios, sala, nem uma garantia transacional contra solicitações concorrentes.
- O frontend ainda utiliza dados em memória e não foi conectado à API.
- A API atual tem `GET/POST /api/v1/companies/{companyId}/appointments` e `PATCH /api/v1/companies/{companyId}/appointments/{appointmentId}/status`.

## Regras de trabalho adotadas

1. Preservar a duração cadastrada em cada serviço; não introduzir uma segunda duração independente no agendamento.
2. Manter motivo obrigatório ao cancelar.
3. Modelar bloqueios com escopo explícito de profissional, sala ou estabelecimento.
4. Validar conflitos de profissional e sala quando esses recursos forem informados.
5. Manter o status atual `SCHEDULED` para novos agendamentos até que o fluxo de confirmação seja definido explicitamente. Não introduzir silenciosamente `PENDING_CONFIRMATION` sem atualizar domínio, migrações, contratos e UI em conjunto.
6. Aplicar isolamento por empresa e vínculo do usuário na camada de serviço em todas as operações.
7. Nunca considerar uma verificação prévia de conflito suficiente para impedir concorrência. A implementação final deve garantir exclusão atômica no PostgreSQL ou usar bloqueio transacional apropriado, com teste concorrente.
8. Não marcar funcionalidade como pronta até existirem contrato de API, migração, autorização, testes e interface conectada.

## Plano de implementação

- Revisar o schema atual e escolher uma estratégia de persistência compatível para regras de disponibilidade e bloqueios.
- Adicionar migrations Flyway novas, sem modificar migrations já aplicadas.
- Criar entidades, repositórios, DTOs e serviços para disponibilidade e bloqueios com validação de pertencimento à empresa.
- Adicionar endpoints para consulta e manutenção de disponibilidade/bloqueios e consulta de horários disponíveis.
- Fortalecer a criação e o cancelamento de agendamentos, registrando evento de auditoria com dados mínimos.
- Criar testes unitários/integração para conflito, cancelamento, papéis e isolamento multiempresa.
- Conectar o frontend à API com configuração explícita de URL, estados de carregamento/erro/vazio e fallback de demonstração apenas quando habilitado para desenvolvimento.
- Documentar os comandos de teste e as limitações que ainda existirem.

## Validação pendente

Este documento é um plano versionado, não uma declaração de que a vertical slice está implementada. O Maven/Docker e os testes ainda precisam ser executados num ambiente de build.