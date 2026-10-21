# Instrua

**Orientação, conexão e confiança.**

O **Instrua** é uma plataforma integrada que conecta pessoas, profissionais e estabelecimentos em uma única experiência. A visão do produto reúne agendamentos e atendimento, instruções e notificações, gestão de serviços, financeiro e fiscal, jornada de trabalho, oportunidades, comunidade e assistência por IA — com recursos liberados conforme o perfil e as permissões de cada conta.

> **Estado do projeto:** em desenvolvimento. O repositório contém uma demonstração web navegável, uma base inicial de backend e documentação de arquitetura/design. Nem todos os módulos descritos nesta visão estão implementados ou conectados ao servidor. Não use o sistema com dados reais de pacientes nem em produção sem completar a implementação, testes e revisão de segurança.

## Visão do produto

O Instrua não deve ser uma coleção de aplicativos independentes. A proposta é uma **plataforma única**, com uma conta por pessoa e diferentes contextos de uso. A mesma pessoa pode, por exemplo, atuar como profissional em uma organização e também usar o Instrua como paciente, sem misturar permissões ou dados desses contextos.

### Experiências planejadas

- **Mobile:** experiência simplificada para clientes/pacientes e profissionais, com acesso rápido a agendamentos, instruções, confirmações e notificações.
- **Web:** acesso pelo navegador a serviços, perfis, agendamentos e áreas de gestão responsivas.
- **Desktop/PC:** experiência de trabalho voltada a recepção, profissionais e gestores, com agenda, tabelas e ferramentas operacionais. A arquitetura está documentada; isso não significa que exista um aplicativo desktop completo hoje.

## Módulos e capacidades planejadas

| Área | Escopo |
| --- | --- |
| Identidade e acesso | Cadastro, login, recuperação de acesso, verificação de identidade e gestão de sessões. |
| Busca e serviços | Descoberta de profissionais/estabelecimentos, catálogo de serviços e disponibilidade. |
| Agendamentos | Marcação, confirmação, check-in, remarcação, cancelamento e histórico. |
| Instruções e notificações | Orientações personalizadas, lembretes e preferências de contato. |
| Saúde | Vínculos de atendimento e solicitações de documentos clínicos com acesso restrito. |
| Financeiro e fiscal | Cobranças, pagamentos, recibos, repasses e documentos fiscais. Integrações reais ainda precisam ser implementadas. |
| Instrua Protege | Jornada prevista, entradas/saídas, intervalos, divergências e solicitações de correção. |
| Oportunidades | Vitrine de oportunidades, propostas e candidaturas. |
| Social | Publicações, comentários, denúncias e moderação. |
| Dante (IA) | Assistência a tarefas e informações; não substitui decisão profissional nem pode diagnosticar, prescrever ou emitir documentos clínicos autonomamente. |
| Administração | Organizações, unidades, equipe, permissões, auditoria e integrações. |

O escopo acima descreve a visão do produto, não uma declaração de que cada funcionalidade esteja pronta.

## Identidade visual

A interface segue o sistema visual oficial do Instrua, com cores naturais, acessíveis e consistentes entre experiências.

| Token | Cor |
| --- | --- |
| Verde-musgo — principal | `#414A35` |
| Oliva | `#69704B` |
| Sálvia | `#96977B` |
| Areia | `#D6C9AD` |
| Terracota | `#A66F4B` |
| Âmbar | `#C18A43` |
| Off-white | `#F5F1E8` |

Documentação: [Sistema de design](docs/INSTRUA_DESIGN_SYSTEM.md).

## Estado técnico atual

### Backend

A base inicial do backend usa:

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- API REST
- PostgreSQL
- Flyway para migrações
- Maven

Há uma migração inicial de domínio e serviços/controladores de base para áreas como perfil, agendamento, documentos clínicos, financeiro, jornada e Social. A presença de uma classe, rota ou tabela não significa que o módulo esteja completo, pronto para produção ou integrado a todos os fluxos.

### Front-end

- `frontend/demo.html`: protótipo web navegável para demonstração.
- `frontend/index.html`: entrada/encaminhamento para a demonstração.
- `frontend/styles.css`: estilos e tokens visuais.
- `frontend/DEMO.md`: instruções e limitações do protótipo.

O protótipo ainda mantém dados e interações locais em partes da experiência. Existe um bootstrap para configurar a URL da API, mas isso **não conecta automaticamente todas as telas** ao backend.

### Clientes mobile e desktop

A arquitetura proposta para os clientes está documentada em [Arquitetura multiplataforma](docs/PLATFORM_CLIENTS_ARCHITECTURE.md). Aplicativos mobile e desktop completos ainda precisam ser desenvolvidos; não devem ser considerados entregues apenas porque estão previstos na arquitetura.

## Executar a demonstração web

A demonstração é um arquivo HTML estático. Para uma visualização simples, abra `frontend/index.html` em um navegador ou publique a pasta `frontend/` em um servidor estático.

Para configurar a URL da API no protótipo, pode-se usar o parâmetro `api` na URL, por exemplo:

```text
?api=http://localhost:8080
```

A configuração padrão não pressupõe que o backend esteja disponível. Consulte [frontend/DEMO.md](frontend/DEMO.md) para detalhes e limitações.

## Executar o backend

Os comandos e requisitos exatos dependem da configuração local e das variáveis de ambiente do projeto. Em geral, é necessário ter Java 21, Maven Wrapper e uma instância PostgreSQL configurada. Não coloque senhas, tokens ou dados reais no repositório.

A partir da raiz, o projeto de API está em:

```text
backend/instrua-api/
```

Para validar a base do backend com o Maven Wrapper:

```bash
cd backend/instrua-api
bash ./mvnw verify
```

A validação pode depender de serviços e variáveis de ambiente usados pelo CI. Consulte os arquivos de configuração e o workflow em `.github/workflows/backend-ci.yml`.

## Segurança, privacidade e limites

O Instrua pode tratar dados pessoais e dados sensíveis de saúde. Antes de qualquer uso real, é necessário implementar e validar, no mínimo:

- autorização por contexto, organização/unidade, vínculo e necessidade de acesso;
- isolamento entre organizações e verificação de propriedade em cada rota;
- proteção reforçada para prontuários, documentos e anexos clínicos;
- auditoria de acesso e alteração de dados sensíveis;
- gestão segura de tokens, sessões, segredos e configurações de CORS;
- armazenamento seguro de arquivos, backups, retenção e recuperação;
- consentimentos e preferências de comunicação compatíveis com a LGPD;
- testes de migração, integração, autorização negativa e isolamento entre tenants;
- revisão de segurança, privacidade e requisitos legais antes da produção.

**Regras de produto:** dados clínicos só podem ser acessados pelo paciente e por pessoas/equipes devidamente autorizadas no contexto assistencial adequado. Módulos não clínicos não recebem acesso ao prontuário. A existência de um cadastro de convênio ou forma de pagamento não comprova cobertura nem autorização de atendimento.

## Integrações

Integrações com pagamentos e reembolsos, emissão fiscal/NFS-e, WhatsApp Business Platform, notificações push, assinatura digital e provedores de IA não devem ser consideradas operacionais sem configuração, credenciais, implementação e testes específicos. Para mensagens de WhatsApp, a direção é usar a plataforma oficial e respeitar consentimento, modelos e políticas aplicáveis.

## Documentação do repositório

- [Sistema de design](docs/INSTRUA_DESIGN_SYSTEM.md)
- [Arquitetura multiplataforma](docs/PLATFORM_CLIENTS_ARCHITECTURE.md)
- [Implementação inicial do backend](docs/INSTRUA_BACKEND_IMPLEMENTATION.md)
- [Demonstração web](frontend/DEMO.md)

## Próximas etapas sugeridas

1. Revisar e estabilizar os contratos da API e os limites de cada módulo.
2. Completar autorização contextual e testes de isolamento entre organizações.
3. Validar as migrações de banco em instalação limpa e em atualização.
4. Conectar os fluxos prioritários do protótipo à API real.
5. Completar testes automatizados de backend, integração e segurança.
6. Implementar os clientes mobile e desktop conforme a arquitetura.
7. Integrar serviços externos somente com os controles de segurança, privacidade e operação necessários.

---

**Projeto em desenvolvimento.** A documentação distingue a visão planejada das funcionalidades que já existem no código; confirme o estado de cada módulo antes de depender dele.
