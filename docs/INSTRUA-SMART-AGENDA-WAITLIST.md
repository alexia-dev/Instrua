# Instrua — Agenda inteligente, confirmação e fila de espera

## Princípio de produto
Agenda, confirmação, cancelamento, encaixe, fila de espera e remanejamento são capacidades nativas do Instrua — não um produto separado. Devem compartilhar pacientes/clientes, profissionais, estabelecimentos, serviços/exames, permissões, notificações e auditoria.

## Fluxo de confirmação de exames
1. O estabelecimento configura antecedência e política de confirmação; padrão desejado: 48 horas antes.
2. O Instrua envia solicitação pelo canal habilitado (WhatsApp/SMS/e-mail).
3. O paciente confirma, recusa ou solicita contato/remarcação.
4. Sem resposta, o compromisso fica pendente e recebe lembrete conforme política.
5. A vaga só é liberada conforme regra da clínica, com aviso e tolerância configuráveis.
6. A vaga é oferecida à fila do mesmo exame, respeitando prioridade e disponibilidade.
7. A oferta tem prazo; se expirar ou for recusada, segue para próximo paciente elegível.
8. O aceite confirma o remanejamento e preserva histórico de vaga e comunicação.

## Regras de segurança
- Validar tenant/estabelecimento no servidor em todas as operações.
- Nunca expor dados pessoais de um paciente a outro paciente da fila.
- Não incluir informações clínicas em mensagens de confirmação.
- Registrar confirmação, liberação, oferta, aceite, recusa, expiração e remanejamento.
- A recepção pode revisar exceções e corrigir erros.
- Prioridade é configurável; não presumir urgência clínica sem regra formal.
- Usar templates aprovados e consentimento/canal apropriado.

## API inicial
Base: /api/v1/companies/{companyId}/appointment-waitlist
- GET /services/{serviceId}: listar fila por exame/serviço.
- POST /: cadastrar paciente na fila.
- POST /{entryId}/offer: oferecer vaga já liberada com prazo de aceite.
- POST /{entryId}/accept: registrar aceite.
- POST /{entryId}/decline: registrar recusa.

A primeira etapa entrega o domínio/API da fila. Ainda faltam: lembretes automáticos 48h, WhatsApp, expiração automática de ofertas, criação/reatribuição automática do agendamento, interface da recepção e testes end-to-end. Não anunciar esses fluxos como prontos até serem implementados e validados.

## Critérios de produção
- Testes de estados/transições e isolamento entre clínicas.
- Idempotência de webhooks e mensagens.
- Jobs agendados com retry e proteção contra duplicidade.
- Auditoria e rastreio de comunicações.
- Revisão LGPD, retenção e consentimentos.
- Testes ponta a ponta de confirmação, expiração, fila, oferta, aceite e remanejamento.
