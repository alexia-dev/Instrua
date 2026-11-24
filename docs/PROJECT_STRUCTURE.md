# Estrutura do repositório

Este documento explica onde cada parte do Instrua deve ficar e quais pastas são código ativo, documentação ou material histórico.

## Mapa rápido

```text
Instrua/
├── .github/
│   └── workflows/          # CI e automações do GitHub
├── backend/
│   └── instrua-api/         # API Java/Spring Boot
│       ├── src/main/java/com/instrua/
│       │   ├── ai/          # Assistência de IA
│       │   ├── appointments/ # Agenda e reservas
│       │   ├── auth/        # Autenticação e segurança HTTP
│       │   ├── clients/     # Clientes
│       │   ├── clinical/    # Fluxos e acesso clínico
│       │   ├── companies/   # Organizações
│       │   ├── common/      # Infraestrutura comum, auditoria e segurança
│       │   ├── employees/   # Equipe
│       │   ├── finance/     # Base inicial financeiro/pagamentos
│       │   ├── instructions/ # Orientações
│       │   ├── integrations/ # Integrações
│       │   ├── journey/     # Instrua Protege/jornada
│       │   ├── notifications/ # Notificações
│       │   ├── opportunities/ # Oportunidades
│       │   ├── patients/    # Pacientes e cobertura
│       │   ├── reports/     # Relatórios
│       │   ├── services/    # Catálogo de serviços
│       │   ├── social/      # Social e moderação
│       │   └── users/       # Contas, perfis e papéis
│       ├── src/main/resources/
│       │   ├── application.yml # Configuração principal
│       │   └── db/migration/   # Migrações Flyway versionadas
│       ├── src/test/        # Testes automatizados
│       ├── legacy/          # Código antigo arquivado; não é fonte ativa
│       ├── Dockerfile
│       ├── pom.xml
│       └── mvnw
├── docker/
│   └── compose.yaml         # PostgreSQL local de desenvolvimento
├── docs/
│   ├── README.md            # Índice da documentação
│   ├── PROJECT_STRUCTURE.md # Este mapa
│   ├── INSTRUA_BACKEND_IMPLEMENTATION.md
│   ├── INSTRUA_DESIGN_SYSTEM.md
│   └── PLATFORM_CLIENTS_ARCHITECTURE.md
├── frontend/
│   ├── index.html           # Entrada para a demonstração
│   ├── demo.html            # Protótipo web navegável
│   ├── styles.css           # Tokens e estilos compartilhados
│   └── DEMO.md              # Como abrir e limitações
├── .editorconfig
├── .gitignore
├── LICENSE
└── README.md
```

## Regras de organização

1. **Código de produção ativo** fica em `backend/instrua-api/src/main`; não adicionar novas classes em `legacy/`.
2. **Cada domínio Java** fica no pacote correspondente. Código transversal vai em `common/`; evite um pacote genérico com regras de negócio de vários módulos.
3. **Migrações de banco** são imutáveis depois de aplicadas em ambientes compartilhados. Para mudanças de schema, criar nova migração Flyway numerada.
4. **Testes** ficam em `src/test`, espelhando os pacotes do código que testam.
5. **Documentação de produto/arquitetura** fica em `docs/`; instruções específicas do protótipo ficam em `frontend/DEMO.md`.
6. **Configuração local** usa variáveis de ambiente ou arquivos ignorados pelo Git. Nunca versionar credenciais reais, tokens, certificados ou dados de pacientes.
7. **Clientes futuros** devem ter diretórios próprios, por exemplo `apps/mobile/` e `apps/desktop/`, somente quando os respectivos projetos forem iniciados. A arquitetura documentada não significa que esses clientes já existam.
8. **Não duplicar configuração Spring** entre `application.properties` e `application.yml`. O arquivo de configuração ativo é `application.yml`.
9. **Não misturar protótipo e produto integrado**: a demo ainda usa dados locais em parte dos fluxos. Identificar claramente qualquer tela ou integração que não esteja conectada à API.

## Pontos a resolver futuramente

- Revisar se o arquivo `legacy/previous-spring-base` ainda precisa ser mantido; remover apenas após confirmar que nada depende dele e que o histórico está preservado no Git.
- Padronizar os comandos de desenvolvimento e Docker em um guia de contribuição.
- Adicionar contratos OpenAPI e documentação de endpoints quando estiverem estabilizados.
- Criar projetos de mobile e desktop quando começar a implementação real, em vez de criar pastas vazias apenas para aparentar completude.
