# Instrua — arquitetura multiplataforma

## Produto

O Instrua é um único produto disponível em três clientes: **site web**, **aplicativo mobile** (Android primeiro; iOS como cliente previsto) e **aplicativo desktop** (Windows primeiro; macOS/Linux como clientes previstos). Os clientes não são produtos separados e não mantêm contas ou regras de negócio independentes.

## Regra de arquitetura

- Um único backend REST versionado (`/api/v1`) e uma base de dados central com PostgreSQL.
- Uma identidade por pessoa; papéis, vínculos com organizações, unidades, equipes e permissões são contextuais.
- Os três clientes usam os mesmos contratos de API, regras de domínio, auditoria e políticas de segurança.
- A interface pode ser adaptada ao dispositivo, mas autorização nunca depende de esconder telas/botões no cliente.
- O prontuário permanece no domínio clínico protegido; o cliente desktop ou site de um negócio não clínico não pode acessá-lo por compartilhar a mesma plataforma.
- Sessões, tokens, cache e arquivos locais não devem compartilhar dados sensíveis sem necessidade. Segredos não ficam embutidos no app.

## Stack recomendada

| Camada | Direção |
|---|---|
| Backend | Java 21 + Spring Boot, API REST, Spring Security, PostgreSQL e Flyway |
| Site | React + TypeScript, responsivo e acessível |
| Mobile | Kotlin + Jetpack Compose para Android; considerar cliente iOS próprio quando entrar no escopo |
| Desktop | Tauri + React + TypeScript para Windows, com distribuição assinada; avaliar macOS/Linux conforme a necessidade |
| API contracts | OpenAPI como contrato único; tipos gerados/validados para clientes |
| Auth | OAuth2/OIDC quando definido, tokens de acesso curtos, renovação segura, MFA para áreas sensíveis |
| Release | CI para testes, empacotamento e distribuição separados por plataforma |

Esta é uma proposta de stack para decidir e implementar; ela não afirma que os três clientes já existam. A entrega atual nesta branch continua concentrada no backend e seus contratos.

## Clientes e áreas

- **Paciente/cliente:** busca, perfil único, agendamento, aceite de política, lembretes, instruções, documentos disponibilizados, preferências, dados próprios e acessibilidade.
- **Profissional e equipes de saúde:** agenda e tarefas conforme vínculo; dados clínicos apenas mediante autorização contextual e necessidade assistencial.
- **Clínica/hospital/rede:** recepção, agenda, equipes, documentos, autorização, operações, financeiro/fiscal, auditoria e relatórios conforme permissões.
- **Outros estabelecimentos:** catálogo, agenda, pedidos, comunicação e operações; nunca recebem acesso ao prontuário só por estarem na mesma plataforma.
- **Desktop:** adequado a recepção, faturamento, gestão, atendimento e operação de clínicas, com controles adicionais contra exposição em ambiente compartilhado.

## Segurança multiplataforma

1. Toda requisição é autorizada no backend por conta, organização, unidade, equipe, vínculo, contexto e permissão mínima.
2. Aplicativos devem usar armazenamento seguro de tokens (Android Keystore / credenciais protegidas do sistema no desktop) e nunca salvar tokens em logs ou arquivos de configuração.
3. Segredos de provedores ficam no servidor ou em secret manager; clientes recebem apenas configuração pública necessária.
4. Downloads clínicos devem usar autorização por recurso, URLs temporárias, expiração, rastreabilidade e cache restrito.
5. Push, e-mail, SMS e WhatsApp respeitam preferências, consentimento e limites do canal; entrega requer adaptadores de provedores.
6. Atualizações de desktop/mobile precisam de assinatura, verificação de integridade e política de versão suportada.

## Etapas de entrega

1. Fechar contratos REST/OpenAPI e critérios de autorização.
2. Fazer o fluxo paciente e o fluxo de agenda funcionar com backend real.
3. Construir o site React conectado ao backend.
4. Construir o app Android usando os mesmos contratos.
5. Empacotar o desktop Tauri reutilizando componentes de UI adequados, sem copiar lógica de negócio para o cliente.
6. Automatizar build/testes e release de web, Android e desktop.
7. Adicionar iOS e demais sistemas desktop quando houver validação de necessidade, assinatura e pipeline.

## Não negociáveis

- A mesma conta funciona nos três ambientes.
- Não cobrar do paciente pelo núcleo essencial.
- Não duplicar permissões ou regras clínicas em cada cliente.
- Funcionalidade exibida precisa ser suportada pelo backend; se uma integração não está conectada, marcar como indisponível/em configuração.
- Antes de produção: threat model, LGPD, revisão de segurança, testes de isolamento entre organizações e validação dos fluxos de documentos/financeiro.
