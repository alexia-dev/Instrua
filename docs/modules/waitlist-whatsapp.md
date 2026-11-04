# Instrua — Lista de espera e WhatsApp Cloud API

Implementação inicial na branch `feat/instrua-waitlist-whatsapp-cloud`.

## Escopo
- Lista de espera de exames integrada ao domínio de clínica (`company_id`) e cadastro existente de pacientes.
- Ordenação: prioridade descendente e entrada mais antiga primeiro.
- Chamada por WhatsApp Cloud API (Meta Graph API), webhook com verificação HMAC SHA-256.
- Webhook responde SIM/NÃO, confirma/cancela e chama próximo elegível.
- Não cria cadastro duplicado de pacientes nem um novo sistema independente.

## Endpoints
- `POST /api/companies/{companyId}/waitlist`
- `GET /api/companies/{companyId}/admin/waitlist/{examId}`
- `POST /api/companies/{companyId}/admin/waitlist/{examId}/call-next`
- `GET /api/webhook/whatsapp?hub.mode=subscribe&hub.verify_token=...&hub.challenge=...`
- `POST /api/webhook/whatsapp/{companyId}`

## Configuração real da Meta
1. Criar/usar Meta Business Portfolio e WhatsApp Business Account (WABA).
2. Registrar e verificar número de telefone; configurar WhatsApp Cloud API.
3. Obter Phone Number ID, access token de sistema e App Secret.
4. Configurar webhook HTTPS público para GET/POST e assinar o campo `messages`.
5. Configurar as variáveis `WHATSAPP_PHONE_NUMBER_ID`, `WHATSAPP_ACCESS_TOKEN`, `WHATSAPP_APP_SECRET`, `WHATSAPP_WEBHOOK_VERIFY_TOKEN` no ambiente seguro.
6. Para mensagens iniciadas pela clínica fora da janela de atendimento de 24h, usar template aprovado pela Meta. O texto livre implementado aqui é somente um protótipo e precisa ser substituído por template aprovado antes de produção.

## Segurança / produção — pendências obrigatórias
- Associar `companyId` do webhook ao `phone_number_id` recebido no payload; não confiar em tenant vindo do cliente.
- Persistir idempotência por `wamid` e impedir processamento duplicado/replay.
- Fazer envio WhatsApp após commit (outbox), não dentro da transação do banco.
- Implementar template aprovado, status de entrega, retry/backoff e rate limiting.
- Exigir opt-in documentado, respeitar opt-out e registrar auditoria.
- Garantir que mensagens não exponham dados de saúde e que a clínica só chame pacientes autorizados.
- Aplicar política de expiração da chamada e rotina agendada para expirar notificações sem resposta.
- Confirmar o mapeamento de `exam_id` com a entidade real de exames/agenda antes de conectar à agenda.
- Validar build/testes e migração em ambiente de staging antes de merge.
