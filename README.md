# Instrua

Plataforma SaaS de agendamento **Multi-Nicho**, com Web + Mobile planejado para Android/iOS.

## Produto

O Instrua é um aplicativo independente. Ele não é um launcher do NEXA e não incorpora o produto de faturamento NEXA.

### Nichos

- Saúde
- Beleza
- Bem-estar
- Pet
- Consultorias
- Educação
- Serviços técnicos
- Outros nichos configuráveis

## 3 visões

### Cliente

- Busca por serviço, nicho e proximidade
- Filtros e descoberta
- Agendamento rápido
- Histórico e favoritos
- Lista de espera ativa
- Carteira e pagamentos
- Agendamento em grupo
- Assinaturas/recorrência
- Triagem conversacional com IA como módulo desacoplado

### Profissional / Estabelecimento

- Agenda e escala
- Clientes e serviços
- Dashboard financeiro
- Comissões, faturamento, cancelamentos e no-show
- Fichas/anamnese customizáveis por nicho
- Equipe e permissões
- Integrações de calendário
- Relatórios

### Administrador Master

- Validação de documentos
- Moderação de profissionais
- Planos e comissões
- Relatórios globais
- Auditoria

## Diferenciais

A arquitetura já possui fundações de banco para:

- Lista de espera ativa
- Match por proximidade/interesse
- Agendamento em grupo e participantes
- Transações, split e taxas
- Assinaturas recorrentes
- Formulários customizáveis
- Documentos de profissionais
- Google/Apple calendar connections
- Sessões de triagem
- Cashback cruzado
- Gamificação
- Experiências AR
- Assinaturas dos parceiros da plataforma

Esses módulos devem ser ativados progressivamente; a fundação de dados não significa que uma integração externa ou regra financeira já esteja pronta para produção.

## Calendários e Double Booking

O desenho de integração usa conexão por profissional/estabelecimento, armazenamento seguro de referência de credencial e sincronização incremental.

- Google Calendar: OAuth, refresh token protegido, watch/webhook e sincronização incremental.
- Apple Calendar: integração compatível com CalDAV/credencial do ambiente.
- Antes de confirmar um horário, o backend deve validar a agenda interna e os bloqueios externos.
- Eventos recebidos devem ser idempotentes e reconciliados por identificador externo.
- A confirmação final continua sendo uma operação server-side, evitando confiar no calendário do navegador.

## LGPD e segurança

- Minimização de dados
- Finalidade e base legal documentadas
- Controle por organização e papel
- Auditoria de operações relevantes
- Criptografia em trânsito e proteção de segredos
- Referências de storage em vez de guardar arquivos sensíveis no banco
- Retenção e descarte definidos por categoria
- Exportação/eliminação quando aplicável
- Dados de pagamento tratados por PSP; não armazenar dados completos de cartão
- Dados de saúde e outros dados sensíveis com controles adicionais de acesso e tratamento

## Roadmap técnico

1. Core: autenticação, descoberta, organizações, serviços e agenda.
2. Cliente: busca, favoritos, histórico, lista de espera e fluxo rápido.
3. Profissional: escala, financeiro, formulários e calendários.
4. Master: validação, assinaturas, comissões e relatórios.
5. Pagamentos: Pix/cartão/split com PSP e webhooks.
6. Diferenciais: grupo, recorrência, IA, cashback/gamificação e AR.
7. Mobile: Android/iOS consumindo a mesma API.

## Stack

- Backend: Java 21 + Spring Boot
- Banco: PostgreSQL + Flyway
- Frontend atual: HTML/CSS/JavaScript modular
- Desktop: Electron shell
- Infra: Docker
- API: REST
- Auth: JWT


## Instrua AI

O Instrua possui um AI Core separado do NEXA. Ele usa ferramentas autorizadas pela sessão para consultar agendamentos, horários e instruções, sem acesso direto ao banco pela IA. Por padrão funciona com um provider mock determinístico. Para usar um provider compatível com a API de chat, configure `AI_API_KEY`, `AI_BASE_URL` e `AI_MODEL` no ambiente; nunca versionar a chave.

Ações mutáveis exigem confirmação e o próximo estágio é derivar toda ação do usuário autenticado, em vez de aceitar identificadores de usuário fornecidos pelo cliente.
