# INSTRUA

### Orientação, conexão e confiança.

O **Instrua** é uma plataforma para organizar a jornada entre pacientes, profissionais e estabelecimentos. Este repositório contém o backend Java/Spring Boot. A experiência visual e os fluxos de produto são definidos pelo PDF oficial do Instrua; a API deve oferecer contratos estáveis para essas telas sem misturar dados de demonstração com dados reais.

## Escopo do produto

- Área pública, autenticação, convites e permissões;
- área do paciente e área do profissional;
- recepção, agenda, agendamentos, confirmações, cancelamentos e check-in;
- instruções pré-atendimento e notificações;
- administração do estabelecimento e administração da plataforma;
- documentos e registros com permissões e auditoria;
- **Instrua Clínico**, área restrita a profissionais autorizados;
- **Instrua Protege**, registros de jornada, ocorrências, divergências e solicitações de correção;
- área financeira/fiscal de acordo com o responsável legal, permissões e fronteira de integração;
- **Oportunidades**, com publicação e candidatura/reserva — não é um leilão;
- **Dante IA**, com sugestões sujeitas à revisão humana.

O escopo do PDF é a referência do produto, mas não significa que todos os módulos ou integrações já estejam implementados. Consulte o [contrato de domínio](docs/INSTRUA-PDF-DOMAIN-ALIGNMENT.md) e o [estado de implementação por etapa](docs/INSTRUA-PDF-IMPLEMENTATION-STATUS.md).

## Arquitetura

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- API REST
- PostgreSQL
- Flyway
- Docker
- Monólito modular com limites de domínio explícitos

O backend existente deve ser evoluído por etapas, preservando funcionalidades compatíveis e criando migrações versionadas para mudanças de banco. Não remover módulos ou migrar domínios financeiros sem antes confirmar as fronteiras de integração.

## Perfis de referência

- **PLATFORM_ADMIN** — administração global estritamente administrativa;
- **COMPANY_OWNER / COMPANY_ADMIN** — administração do estabelecimento conforme permissões;
- **RECEPTION** — agenda, cadastro mínimo, confirmações e check-in;
- **PROFESSIONAL** — agenda própria e dados necessários ao atendimento;
- **CLINICAL** — acesso clínico autorizado;
- **BILLING** — financeiro/fiscal nos estabelecimentos autorizados;
- **PATIENT** — acesso aos próprios dados e compromissos;
- **AUDITOR** — leitura autorizada de trilhas e registros.

Papéis não substituem permissões granulares. A autorização precisa ser aplicada no servidor em cada requisição; ocultar botões na interface não é controle de segurança.

## Segurança e privacidade

- Validar o tenant e o vínculo do usuário no servidor para toda consulta ou alteração.
- Aplicar menor privilégio, validação de entrada e minimização de dados.
- Auditar alterações e acessos sensíveis.
- Não registrar segredos, dados clínicos completos ou documentos pessoais em logs.
- Não versionar dados reais.
- Não declarar como reais pagamentos, emissão fiscal, controle oficial de jornada, envio de mensagens ou ações de IA sem integrações e validações efetivas.
- Exigir revisão humana para ações sensíveis sugeridas pelo Dante IA.

## Estrutura atual

```text
backend/
└── instrua-api/
    ├── auth/
    ├── users/
    ├── companies/
    ├── clients/
    ├── appointments/
    ├── instructions/
    ├── notifications/
    ├── integrations/
    ├── reports/
    └── common/
docs/
└── INSTRUA-PDF-DOMAIN-ALIGNMENT.md
```

## Frontend de demonstração

A pasta `frontend/` contém uma interface navegável responsiva em HTML, CSS e JavaScript, alinhada às páginas do PDF: perfis, busca, agenda, oportunidades de preço fixo, instruções, notificações, equipe e acessibilidade. Para experimentar, abra `frontend/index.html` no navegador ou publique a pasta em um servidor estático.

**Importante:** esta primeira interface usa dados de demonstração em memória. Reservas, convites, notificações, preferências e alterações ainda não são persistidos nem enviados à API. Consulte o [estado de implementação](docs/INSTRUA-PDF-IMPLEMENTATION-STATUS.md) antes de considerar qualquer fluxo pronto para produção.

## Validação

Antes de considerar um módulo pronto, verificar regras de domínio, persistência/migração, autorização, contrato de API, auditoria e testes. Executar testes Maven e testes de isolamento entre organizações antes de publicar para produção.

🚧 Projeto em desenvolvimento.
