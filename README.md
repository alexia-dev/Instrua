# INSTRUA

### Módulo do NEXA para jornada clínica, agendamento e comunicação

O **Instrua** é o módulo clínico do **NEXA**. O foco é acompanhar a jornada entre clínica e paciente: agendamento, confirmação, check-in, instruções, notificações e histórico do compromisso.

> O Instrua não incorpora faturamento. O domínio financeiro permanece no **Nexa Bill**.

## Objetivos do módulo

- Organizar agenda e compromissos;
- manter cadastro de pacientes;
- confirmar presença;
- apoiar check-in;
- distribuir instruções personalizadas;
- enviar notificações;
- registrar histórico e alterações;
- preparar integrações futuras.

## Perfis

- **PLATFORM_ADMIN** — administração da plataforma;
- **COMPANY_OWNER** — responsável pela clínica;
- **COMPANY_ADMIN** — administração da clínica;
- **RECEPTION** — agenda, cadastro e confirmação;
- **CLINICAL** — fluxo clínico e instruções;
- **BILLING** — acesso financeiro quando o perfil possuir o módulo;
- **PATIENT** — acesso apenas aos próprios dados e compromissos.

## Arquitetura atual

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- REST API
- PostgreSQL
- Flyway
- Docker

A direção arquitetural do NEXA é **monólito modular**: os domínios ficam separados no código, mas compartilham uma API e um banco central na primeira fase.

## Regras de segurança

Como o módulo pode lidar com dados pessoais e potencialmente dados de saúde:

- aplicar isolamento por clínica/tenant em todas as consultas;
- usar autenticação e autorização por papel;
- manter auditoria para alterações relevantes;
- nunca versionar dados reais no repositório público;
- armazenar segredos fora do código;
- preparar backups e políticas de retenção antes de produção.

## Próximos passos

1. Formalizar o contexto de tenant no backend.
2. Completar RBAC por módulo.
3. Criar auditoria.
4. Evoluir pacientes, agenda, confirmação e instruções.
5. Documentar a API com OpenAPI.
6. Integrar o Instrua ao NEXA Core sem misturar o domínio do Nexa Bill.

## Estrutura

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
```

🚧 Projeto em desenvolvimento.
