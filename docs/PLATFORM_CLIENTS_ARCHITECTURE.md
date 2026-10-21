# Instrua — arquitetura multiplataforma

## Produto e ecossistema

O Instrua é um único produto com uma conta por pessoa, disponível em clientes adaptados ao contexto:

- **Web:** navegador moderno, incluindo Safari, Chrome e Edge.
- **iPhone:** app iOS com navegação e fluxos pensados para uso móvel.
- **iPad:** layout adaptável à tela maior, multitarefa e uso com teclado/ponteiro quando suportado.
- **Mac:** experiência desktop para recepção, faturamento, gestão e operação; avaliar distribuição nativa macOS e compatibilidade com Apple Silicon/Intel.
- **Apple Watch:** possível app complementar, limitado a notificações, lembretes e ações rápidas seguras; não é lugar para prontuário completo nem operações complexas.
- **Android:** app móvel.
- **Windows e, futuramente, Linux:** desktop.
- **Outros dispositivos Apple (como Apple Vision Pro):** considerar apenas se houver um caso de uso real, acessibilidade e viabilidade técnica; não prometer suporte antes de protótipo e testes.

“Pensado para todos os aparelhos Apple” significa planejar e testar a experiência em cada categoria — não apenas esticar a tela do iPhone. Nem todos os módulos precisam existir em todo dispositivo: cada cliente mostra o que faz sentido e mantém as mesmas regras e permissões.

## Regra de arquitetura

- Um único backend REST versionado (`/api/v1`) e uma base de dados central com PostgreSQL.
- Uma identidade por pessoa; papéis, vínculos com organizações, unidades, equipes e permissões são contextuais.
- Os clientes usam os mesmos contratos de API, regras de domínio, auditoria e políticas de segurança.
- A interface pode ser adaptada ao dispositivo, mas autorização nunca depende de esconder telas/botões no cliente.
- O prontuário permanece no domínio clínico protegido; o cliente desktop ou site de um negócio não clínico não pode acessá-lo por compartilhar a mesma plataforma.
- Sessões, tokens, cache e arquivos locais não devem compartilhar dados sensíveis sem necessidade. Segredos não ficam embutidos no app.

## Stack recomendada

| Camada | Direção |
|---|---|
| Backend | Java 21 + Spring Boot, API REST, Spring Security, PostgreSQL e Flyway |
| Site | React + TypeScript, responsivo e acessível |
| Mobile | React + TypeScript com Tauri 2 como base compartilhada inicial; validar iOS e Android em dispositivos reais e adotar componentes nativos quando necessários |
| Desktop | Tauri 2 + React + TypeScript para Windows; avaliar macOS/Linux e assinatura/distribuição por sistema |
| Apple Watch | Avaliar posteriormente um companion nativo/watchOS para notificações e ações rápidas; não está implementado nesta base |
| Contratos de API | OpenAPI como contrato único; tipos gerados/validados para clientes |
| Autenticação | OAuth2/OIDC quando definido, tokens de acesso curtos, renovação segura, MFA para áreas sensíveis |
| Releases | CI para testes, empacotamento, assinatura e distribuição separados por plataforma |

A fundação atual em `clients/instrua-app/` é uma interface React + TypeScript com Tauri 2 e scripts para Windows, Android e iOS. É um ponto de partida, não uma garantia de compatibilidade final: iOS precisa ser gerado e testado em macOS/Xcode; macOS precisa de alvo/configuração e testes próprios; Apple Watch exige projeto e implementação específicos. A interface atual usa dados fictícios e ainda não está integrada ponta a ponta ao backend.

## Experiências por dispositivo

- **iPhone:** agenda, confirmações, instruções, notificações, documentos autorizados e ações rápidas com acessibilidade.
- **iPad:** os mesmos fluxos móveis, com espaço extra para agenda e painéis em duas colunas quando apropriado; testar orientação retrato/paisagem e multitarefa.
- **Mac:** fluxo de trabalho de recepção/gestão com teclado, atalhos, janelas e controles para ambientes compartilhados. Não assumir que a build Windows automaticamente é um app Mac pronto.
- **Apple Watch:** lembretes discretos e ações curtas, sem expor dados clínicos sensíveis em notificações ou mostradores por padrão.
- **Web:** acesso responsivo sem exigir instalação.
- **Android/Windows:** clientes equivalentes nos fluxos suportados, com padrões de navegação próprios da plataforma.

## Áreas e permissões

- **Paciente/cliente:** busca, perfil único, agendamento, aceite de política, lembretes, instruções, documentos disponibilizados, preferências, dados próprios e acessibilidade.
- **Profissionais e equipes de saúde:** agenda e tarefas conforme vínculo; dados clínicos apenas mediante autorização contextual e necessidade assistencial.
- **Clínica/hospital/rede:** recepção, agenda, equipes, documentos, autorização, operações, financeiro/fiscal, auditoria e relatórios conforme permissões.
- **Outros estabelecimentos:** catálogo, agenda, pedidos, comunicação e operações; nunca recebem acesso ao prontuário só por estarem na mesma plataforma.

## Segurança multiplataforma

1. Toda requisição é autorizada no backend por conta, organização, unidade, equipe, vínculo, contexto e permissão mínima.
2. Aplicativos devem usar armazenamento seguro de tokens (Keychain no ecossistema Apple; Android Keystore no Android; credenciais protegidas do sistema no desktop) e nunca salvar tokens em logs ou arquivos de configuração.
3. Segredos de provedores ficam no servidor ou em secret manager; clientes recebem apenas configuração pública necessária.
4. Downloads clínicos devem usar autorização por recurso, URLs temporárias, expiração, rastreabilidade e cache restrito.
5. Push, e-mail, SMS e WhatsApp respeitam preferências, consentimento e limites do canal; entrega requer adaptadores de provedores.
6. Atualizações de desktop/mobile precisam de assinatura, verificação de integridade e política de versão suportada.
7. Notificações de tela bloqueada devem ocultar detalhes clínicos e permitir controle de prévias pelo usuário.

## Etapas de entrega

1. Fechar contratos REST/OpenAPI e critérios de autorização.
2. Fazer os fluxos de paciente e agenda funcionarem com backend real.
3. Construir o site React conectado ao backend.
4. Validar e empacotar Android e iOS, com testes em aparelhos reais e tamanhos de tela diferentes.
5. Criar e validar o cliente desktop Windows e, em etapa própria, macOS/Linux.
6. Avaliar um companion Apple Watch apenas após os fluxos centrais estarem estáveis.
7. Automatizar build/testes e releases por plataforma; configurar assinatura, privacidade, acessibilidade e distribuição.
8. Considerar Vision Pro ou outros dispositivos apenas após validar demanda e viabilidade.

## Não negociáveis

- A mesma conta funciona nos ambientes suportados.
- Não cobrar do paciente pelo núcleo essencial.
- Não duplicar permissões ou regras clínicas em cada cliente.
- Funcionalidade exibida precisa ser suportada pelo backend; se uma integração não está conectada, marcar como indisponível/em configuração.
- Antes de produção: threat model, LGPD, revisão de segurança, testes de isolamento entre organizações e validação dos fluxos de documentos/financeiro.
- Não declarar uma plataforma suportada até haver build, testes de interface, acessibilidade, privacidade e distribuição verificados nela.
