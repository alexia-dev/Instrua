# INSTRUA

## Aplicativo do ecossistema NEXA

O Instrua é um aplicativo independente do ecossistema NEXA. Ele pode ser instalado e usado separadamente, mas compartilha identidade e serviços de plataforma quando o usuário autorizar.

### O que o Instrua resolve

O produto organiza jornadas de atendimento, agendamento, instruções, confirmações, check-in, notificações e histórico.

O público não é limitado a clínicas ou hospitais. O modelo pode atender diferentes tipos de organizações e profissionais que precisam organizar atendimento e comunicação.

### Relação com o NEXA

```text
NEXA ECOSYSTEM
   │
   ├── NEXA ACCOUNT
   ├── NEXA PLATFORM
   │      ├── identidade
   │      ├── organizações
   │      ├── apps
   │      ├── entitlements
   │      └── assinatura
   │
   └── INSTRUA
          ├── agenda
          ├── pacientes/clientes
          ├── instruções
          ├── confirmações
          └── check-in
```

O Instrua não incorpora o Nexa Bill. Os produtos permanecem separados.

### Arquitetura atual

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- REST API
- PostgreSQL
- Flyway
- Docker

A direção arquitetural é uma plataforma com domínios separados e contratos de API. O backend do Instrua pode evoluir para consumir ou expor serviços compartilhados do NEXA Platform sem transformar o Instrua em um monólito de todos os produtos.

### Identidade e acesso

O objetivo é uma conta NEXA única com acesso por aplicativo.

A autorização deverá ser determinada no servidor por:

- identidade;
- organização/membership;
- aplicativo;
- papel/permissão;
- entitlement/plano.

A interface do aplicativo nunca deve ser a fonte de verdade para premium ou autorização.

### Estado

O backend já possui JWT, empresas/tenants, pacientes, auditoria e agenda em evolução. A camada de plataforma compartilhada ainda está sendo implementada.
