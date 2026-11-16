# Central de Antecipação — Instrua

## Objetivo

Preencher horários cancelados oferecendo antecipação a pacientes que já possuem um agendamento futuro compatível. O módulo é parte da agenda do Instrua; não cria uma agenda paralela nem duplica cadastros.

## Base técnica

- Branch de trabalho: `feat/central-antecipacao`.
- Base: `feat/instrua-original-workflows`, que deriva da fundação Java 21 / Spring Boot, PostgreSQL / Flyway e cliente React + TypeScript.
- Domínio de agenda existente: `appointments`, `clients`, `employees` e `service_offerings`.
- A PR #13 (`feat/instrua-waitlist-whatsapp-cloud`) é uma referência de implementação do WhatsApp, mas foi criada sobre `main` e usa o domínio antigo `patients` / `examId`. Não deve ser mesclada diretamente nem copiada sem adaptação: a fundação atual usa `Client`, `Appointment` e `ServiceOffering`.

## Jornada ponta a ponta

1. Um agendamento é cancelado na agenda.
2. O sistema registra a vaga liberada com empresa, profissional, serviço, início/fim, duração e origem do cancelamento.
3. O motor procura agendamentos futuros já existentes, inicialmente dentro da janela configurada (padrão: próximos 7 dias), com o mesmo serviço e profissional habilitado para executar o procedimento.
4. A fila é ordenada por prioridade configurada pela clínica e, em empate, pela data/hora mais próxima do agendamento atual. Não usar diagnóstico ou dados clínicos para pontuação.
5. Uma única oferta ativa por vaga é enviada por vez, para impedir que duas pessoas aceitem o mesmo horário. O prazo de resposta é configurável (padrão inicial: 15 minutos).
6. O paciente responde SIM/NÃO pelo canal oficial. A resposta precisa estar vinculada a uma oferta/token único, telefone verificado e mensagem do provedor; respostas expiradas, repetidas ou sem oferta ativa não alteram a agenda.
7. No SIM, dentro de uma transação, o sistema bloqueia a vaga e o agendamento original, verifica novamente conflitos, serviço, profissional, duração, capacidade diária e elegibilidade. Só então move o agendamento existente para a vaga, sem criar outro compromisso.
8. A vaga anterior do paciente vira uma nova vaga liberada e pode iniciar outra rodada de antecipação (cascata).
9. O Instrua atualiza o painel em tempo real/consulta e cria notificações para a recepção por e-mail e no próprio painel. A operação fica auditada.
10. Se o paciente responder NÃO, não responder no prazo ou não for elegível, a oferta é encerrada e o próximo candidato é considerado.

## Regras operacionais

- Nunca alterar ou cancelar o agendamento original antes de confirmar a nova vaga.
- Revalidar a disponibilidade no momento do aceite; a lista de candidatos não é garantia de vaga.
- Não permitir dois aceites para a mesma vaga (lock pessimista/controle otimista e restrição de unicidade para oferta ativa).
- Não permitir sobreposição para o mesmo profissional/recurso.
- Respeitar a duração configurada no serviço.
- Aplicar limite inicial de até 20 atendimentos por profissional por dia, configurável por clínica e validado no momento da confirmação.
- Exigir compatibilidade de procedimento/serviço e profissional habilitado.
- Convênio, autorização, sala, equipamento, preparo e demais requisitos só podem ser validados quando esses dados estiverem modelados no domínio da agenda. Até lá, o motor não deve presumir compatibilidade: tais casos precisam ser marcados para validação da recepção e não podem ser confirmados automaticamente.
- Preservar o histórico de data/hora anterior, nova data/hora, oferta, resposta, canal, status e ator que originou a ação.
- Respeitar horário de contato, consentimento/opt-in por canal, opt-out e limites de frequência definidos pela clínica.
- Não incluir diagnóstico, CID, prontuário, pedido médico ou detalhes clínicos no corpo de notificações. Usar apenas informação operacional mínima.
- Isolamento obrigatório por `company_id`; nunca confiar em identificador de clínica enviado pelo webhook como prova de autorização.

## Estados

### Vaga
`OPEN` → `OFFERING` → `FILLED` ou `EXPIRED` / `CLOSED`.

### Oferta
`PENDING` → `SENT` → `ACCEPTED` / `DECLINED` / `EXPIRED` / `FAILED` / `SUPERSEDED`.

### Remarcação
A alteração do agendamento é a fonte de verdade. O estado `ACCEPTED` só é definitivo depois que a transação de remarcação concluir com sucesso. Se a vaga for perdida em uma corrida, responder ao paciente que o horário não está mais disponível e oferecer a próxima opção, sem alterar o agendamento original.

## Modelo de dados proposto

- `appointment_vacancies`: empresa, appointment cancelado de origem, serviço, profissional, início/fim, duração, status, criada_em, expira_em, versão/lock.
- `anticipation_offers`: vaga, appointment futuro candidato, cliente, token hash, canal, status, enviada_em, prazo_resposta, respondida_em, provider_message_id, idempotency_key.
- `appointment_reschedule_history`: appointment, início/fim anteriores e novos, vacancy, offer, origem, ator, timestamp.
- `notification_outbox`: evento de e-mail/painel/WhatsApp a enviar após commit, tentativas, próxima_tentativa, estado e chave idempotente.
- `communication_consents` (ou integração com módulo de consentimentos existente): cliente, canal, finalidade, status, origem e timestamp da evidência.

Índices e constraints devem cobrir empresa/status/data, ofertas pendentes e idempotência do identificador de mensagem do provedor. Todas as chaves estrangeiras devem manter o escopo da empresa validado na camada de serviço.

## Integrações

### WhatsApp do paciente
- WhatsApp Business Platform / Cloud API oficial.
- Templates aprovados para mensagens iniciadas pela clínica fora da janela de atendimento.
- Webhook HTTPS com validação HMAC SHA-256, mapeamento confiável de `phone_number_id` para empresa e idempotência por `wamid`.
- Opt-in documentado, opt-out e status de entrega.
- O webhook apenas registra a resposta e dispara o caso de uso transacional; nunca executa remarcação sem revalidar a vaga.

### Recepção
- Notificação interna no painel da Central de Antecipação.
- E-mail após commit para destinatários configurados pela clínica.
- E-mail não deve conter dados clínicos nem ser o único registro da operação.
- Falhas de e-mail não desfazem a remarcação; ficam na outbox para retry e aparecem no painel.

## Endpoints propostos

- `GET /api/v1/companies/{companyId}/anticipation/dashboard`
- `GET /api/v1/companies/{companyId}/anticipation/vacancies`
- `GET /api/v1/companies/{companyId}/anticipation/vacancies/{vacancyId}/candidates`
- `POST /api/v1/companies/{companyId}/anticipation/vacancies/{vacancyId}/start`
- `POST /api/v1/companies/{companyId}/anticipation/offers/{offerId}/resend` (somente se permitido e sem duplicar oferta ativa)
- `POST /api/v1/companies/{companyId}/anticipation/offers/{offerId}/close`
- Webhook do provedor em rota pública própria, com validação de assinatura e tenant resolvido pelo número receptor configurado.

A API administrativa exige autenticação, vínculo ativo com a empresa e permissão de agenda/recepção. O webhook não usa sessão do usuário, mas exige assinatura do provedor e mapeamento de tenant no servidor.

## Configurações por clínica

- Janela de busca de antecipação (padrão 7 dias).
- Prazo de resposta (padrão 15 minutos).
- Limite de tentativas e horário permitido para contato.
- Ordem/prioridade da fila.
- Profissionais e serviços elegíveis.
- Capacidade diária por profissional (padrão 20; configurável).
- Canais habilitados e consentimentos exigidos.
- E-mails da recepção/gestão que recebem os eventos.

## Interface da recepção

1. **Resumo:** vagas abertas, ofertas aguardando resposta, antecipações concluídas, falhas e tempo médio para preencher vaga.
2. **Vagas:** horário, procedimento/serviço, profissional, duração e estado.
3. **Fila de candidatos:** nome, contato mascarado conforme permissão, data atual, prioridade, elegibilidade e motivo de bloqueio.
4. **Ofertas:** canal, envio, prazo, resposta, status de entrega e histórico.
5. **Histórico:** agendamento anterior/novo, horário, responsável e notificações emitidas.
6. **Configurações:** regras e destinatários da unidade.

## Fases de entrega

### Fase 1 — Fundação segura
Modelagem de vagas/ofertas/histórico/outbox, transação de remarcação, locks, idempotência, testes de concorrência e auditoria.

### Fase 2 — Motor e painel
Captura de cancelamento, matching por serviço/profissional/duração, fila, cascata e telas da recepção.

### Fase 3 — WhatsApp e e-mail
Cloud API, templates, webhook assinado, opt-in/opt-out, outbox/retry e notificação interna/e-mail.

### Fase 4 — Regras clínicas/operacionais avançadas
Convênios, autorizações, salas, equipamentos, preparo, bloqueios, escalas e capacidade específica por procedimento.

## Critérios de aceite

- Um cancelamento elegível cria uma única vaga.
- O motor encontra apenas agendamentos futuros compatíveis.
- Só existe uma oferta ativa por vaga.
- Duas respostas SIM concorrentes nunca geram dupla reserva.
- Resposta duplicada/replay do webhook não repete efeitos.
- Oferta expirada não altera agendamento.
- Aceite válido move o mesmo appointment e preserva o histórico.
- A vaga antiga do paciente entra na cascata uma única vez.
- Capacidade diária de 20 por profissional e conflitos são revalidados na transação.
- Eventos de e-mail/painel são criados após commit e têm retry idempotente.
- Usuários de outra empresa não consultam nem alteram a fila.
- Nenhuma mensagem externa é enviada sem consentimento e configuração real do canal.
- Testes, migrations e build devem passar em CI antes de habilitar produção.

## Limites de produção

A implementação de lista de espera existente na PR #13 é uma referência, não uma integração pronta com a agenda atual: ela usa o modelo antigo `patients/examId`, enquanto a base integrada usa `clients/appointments/service_offerings`. O código precisa ser adaptado ao domínio atual. A integração real depende de credenciais guardadas em secrets, configuração da Meta, templates aprovados, provedor de e-mail, migrações testadas e revisão de segurança/LGPD. Até isso ser validado, a funcionalidade deve permanecer desabilitada para dados reais.
