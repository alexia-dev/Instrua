# Instrua — roteiro de entrega comercial (versão operacional)

## O que foi implementado nesta branch

- Abertura padrão da aplicação web/Android no **ProductionApp** (cliente conectado), não nos antigos protótipos. As interfaces antigas ficam isoladas por `?view=visual-demo` e `?view=gemini-preview` para revisão de layout.
- Cadastro de empresa proprietária, cadastro de cliente/paciente e login JWT.
- Painel real para empresa com clientes, equipe/profissionais, serviços e agendamentos via API Spring Boot e PostgreSQL.
- Visualização de agenda, confirmação, conclusão e cancelamento por usuário autorizado.
- Jornada do cliente: busca de estabelecimentos ativos, serviços e profissionais; reserva na API; consulta/cancelamento dos próprios agendamentos, com aceite de política vigente quando aplicável.
- Restrições de escrita por pertencimento real ao estabelecimento, limite de 20 reservas por profissional/dia, transação e bloqueio de linha no agendamento, e restrição de sobreposição no próprio PostgreSQL.
- Teste de integração `CommercialFlowIntegrationTest` com PostgreSQL e MockMvc, abrangendo cadastro real, acesso por empresa, concorrência de horários e reserva do cliente.
- Empacotamento Docker da API e do site, com PostgreSQL, Caddy HTTPS e variáveis de ambiente externas.

## Iniciar desenvolvimento local

```sh
cd docker
docker compose up -d
cd ../backend/instrua-api
./mvnw spring-boot:run
# outro terminal
cd clients/instrua-app
npm install
npm run dev
```

A interface abre em `http://localhost:5173`; sua API local deve responder em `http://localhost:8080/api/v1/health`.

## Publicação web em ambiente próprio com HTTPS

Pré-requisitos: domínio seu apontando para um servidor com Docker, portas 80/443 liberadas e armazenamento para banco. Configurar senhas e chave JWT fortes. O deploy **não é executado automaticamente por um commit**.

```sh
cd docker
cp .env.production.example .env
# substitua domínio, e-mail ACME, POSTGRES_PASSWORD e JWT_SECRET na .env
docker compose --env-file .env -f compose.production.yaml up -d --build
```

Os serviços `gateway`, `web`, `api` e `database` permanecem dentro de uma rede privada, exceto portas 80/443 do gateway. Monitorar `docker compose logs -f`. A configuração de produção impede iniciar com senha de desenvolvimento, JWT previsível ou CORS local.

Para a versão Android apontar para a mesma API externa, configure `VITE_INSTRUA_API_URL=https://seudominio` **antes de compilar** (também como variável do repositório no GitHub Actions). APK do CI é apenas para QA; publicação na Play Store depende de AAB assinado e validações.

## Bloqueios para ativar clientes reais

1. Testar implantação e operações em servidor real com HTTPS, banco persistente, logs e backup/restauração comprovada.
2. Finalizar convites e contas próprias para recepção e profissionais, redefinição de senha, rate limiting de autenticação e gerenciamento de sessões.
3. Revisar autorização e privacidade de **todos os endpoints antigos** antes de permitir dados clínicos; não misturar prontuários entre empresas.
4. Configurar expediente, salas, feriados e disponibilidade antes de permitir agendamento público irrestrito; o formulário atual tenta o horário escolhido e recebe erro do servidor se houver conflito.
5. Implementar o envio verdadeiro de notificações/confirmar recebimento. WhatsApp, e-mail, push e remanejamento automatizado **não** são prometidos como operacionais nesta versão.
6. Configurar formas de cobrança de mensalidade, cadastro de planos e bloqueio de assinaturas inadimplentes, quando o modelo comercial for definido. Um contrato/licença com cobrança manual pode viabilizar pilotos sem checkout integrado.
7. Finalizar acessibilidade e revisão de UX em aparelhos reais, assinar a build Android e testar publicação.
8. Política de retenção, consentimento, exportação/exclusão e procedimentos de incidentes em produção — são itens técnicos e operacionais obrigatórios, não mera burocracia.

**Condição de lançamento:** não anunciar prontuário eletrônico, automações de WhatsApp ou integrações financeiras/fiscais como serviços ativos antes de testes e validação real. A versão operacional conectada é uma base para o primeiro piloto comercial, não certificação de produção.

## Como verificar

- `mvn verify` deve passar com PostgreSQL de testes e migrações Flyway.
- `npm run build` deve passar no cliente web.
- GitHub Actions: `Backend CI`, `Instrua client build`, `Android APK QA`.
- Testar no navegador e no Android com **contas e dados de teste**, sem utilizar dados clínicos reais até finalizar o isolamento e revisão de segurança.
