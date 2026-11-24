# Instrua backend — implementação incremental

> Esta branch adiciona uma fundação de domínio ao backend existente. Não é uma declaração de prontidão para produção.

**Formato do produto:** o Instrua precisa existir como site, aplicativo mobile e aplicativo desktop. Todos usam uma API única e contas/permissões compartilhadas. A proposta de clientes e stack está em [PLATFORM_CLIENTS_ARCHITECTURE.md](PLATFORM_CLIENTS_ARCHITECTURE.md); esta PR implementa backend, não os três clientes.

## Decisões refletidas no backend
- Uma identidade por pessoa e múltiplos papéis/vínculos contextuais; conta, vínculo, papel e permissão são conceitos distintos.
- Saúde é o domínio central; outros estabelecimentos podem reutilizar catálogo/agenda/serviços, mas nunca ganham acesso ao prontuário.
- O núcleo essencial do paciente deve permanecer gratuito: busca, agendamento básico, confirmação, orientação necessária e acesso aos próprios dados.
- A política de cancelamento deve aparecer antes de confirmar e ficar registrada em snapshot no aceite. Alterações futuras não reescrevem o aceite antigo.
- Prontuário exige vínculo assistencial ativo, contexto e menor privilégio; cargo isolado não basta.
- Dante é assistivo. A emissão de documento clínico depende do profissional responsável; upload/scan não equivale a assinatura digital.
- Financeiro/Fiscal são áreas integradas com permissões próprias. Pagamento real e NFS-e exigem adaptadores reais, homologação e validação jurídica.
- Protege mantém eventos originais e trata correção como solicitação revisada por outra pessoa autorizada.
- Social tem estados de moderação; exportação demonstrativa não promete cadeia de custódia forense certificada.
- Notificações dependem de preferências/consentimento e adaptadores. WhatsApp requer plataforma oficial e modelos permitidos.

## Conteúdo desta etapa
- Migração aditiva V4 para permissões contextuais, unidades/equipes, políticas/aceites, outbox de notificações, encontros/registros/acesso clínico, fluxo documental, financeiro/fiscal, Protege, Social, oportunidades, planos, integração e eventos de segurança.
- Serviço base de decisão de acesso clínico/financeiro.
- Máquinas de estado iniciais para documentos, pagamentos, correções de jornada e moderação.
- Endpoints de saúde e capacidades.
- Testes unitários focados em transições e restrições.

## Pendências obrigatórias antes de produção
1. Validar vínculo persistido usuário+empresa+unidade+equipe+encontro+paciente em cada rota e consulta; nunca confiar só no companyId enviado pelo cliente.
2. Conectar ClinicalRecordAccessService a repositórios tenant-filtered, concessões persistidas e testes reais de isolamento. Ele é base, não política completa.
3. Implementar controladores/serviços/repos e DTOs validados para os novos domínios, com paginação e auditoria.
4. Revisar prontuário para retificação sem sobrescrever, assinaturas, retenção, interoperabilidade e normas aplicáveis.
5. Armazenar documentos em storage privado com criptografia, URLs temporárias, validação de tamanho/tipo, antivírus e backup.
6. Integrar pagamentos/estornos, NFS-e, e-signature, e-mail/SMS e WhatsApp Business Platform; verificar webhooks, idempotência e segredos.
7. Revisão jurídica e de privacidade para LGPD, retenção, bases legais, contratos, prontuário, assinatura e jornada de trabalho.
8. Definir rotação JWT, CORS restrito, rate limit, MFA, monitoramento e resposta a incidentes.
9. Nunca incluir conteúdo clínico completo em logs, analytics ou metadados de auditoria.
10. Executar ./mvnw test e testes de integração PostgreSQL no CI. Ainda não declarar checks aprovados até que executem.

## Desenvolvimento local
Defina DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD e JWT_SECRET com valor aleatório seguro. Inicie PostgreSQL e execute ./mvnw test seguido de ./mvnw spring-boot:run. Não use dados reais de pacientes no desenvolvimento.
