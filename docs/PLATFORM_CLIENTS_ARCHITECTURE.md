# Instrua — arquitetura multiplataforma

## Produto e ecossistema

O Instrua é um único produto com uma conta por pessoa, disponível em clientes adaptados ao contexto:

- **Web:** navegador moderno, incluindo Safari, Chrome e Edge.
- **iPhone e iPad:** app iOS/iPadOS com navegação e layouts próprios para celular, tablet, orientação e multitarefa.
- **Mac:** experiência desktop para recepção, faturamento, gestão e operação; requer alvo, assinatura e testes próprios.
- **Apple Watch:** possível app complementar para lembretes e ações rápidas; não é lugar para prontuário completo.
- **Android:** app para diferentes fabricantes e categorias de aparelho, com versões e tamanhos de tela variados.
- **Tablets Android e dobráveis:** layouts adaptáveis, duas colunas quando úteis, mudanças de janela/orientação e continuidade ao dobrar/abrir quando suportada.
- **Wear OS:** possível complemento futuro para notificações e ações rápidas; não implementado nesta fase.
- **Windows e, futuramente, Linux:** desktop.
- **Outros dispositivos (como Apple Vision Pro):** considerar somente com caso de uso, acessibilidade e viabilidade técnica validados.

“Pensado para todos os aparelhos” significa planejar e testar cada categoria, não apenas esticar a tela de um celular. Nem todo módulo precisa existir em todo dispositivo: cada cliente mostra o que faz sentido e mantém as mesmas regras e permissões.

## Regra de arquitetura

- Um único backend REST versionado (`/api/v1`) e uma base central PostgreSQL.
- Uma identidade por pessoa; papéis, vínculos com organizações, unidades, equipes e permissões são contextuais.
- Os clientes usam os mesmos contratos de API, regras de domínio, auditoria e políticas de segurança.
- Autorização nunca depende de esconder telas ou botões no cliente.
- O prontuário permanece no domínio clínico protegido; negócios não clínicos não ganham acesso por compartilhar a plataforma.
- Sessões, tokens, cache e arquivos locais não devem compartilhar dados sensíveis sem necessidade. Segredos não ficam embutidos no app.

## Stack recomendada

| Camada | Direção |
|---|---|
| Backend | Java 21 + Spring Boot, API REST, Spring Security, PostgreSQL e Flyway |
| Site | React + TypeScript, responsivo e acessível |
| Mobile | React + TypeScript com Tauri 2 como base compartilhada inicial; validar Android e iOS em dispositivos reais e adotar componentes nativos quando necessário |
| Desktop | Tauri 2 + React + TypeScript para Windows; avaliar macOS/Linux com configuração e testes próprios |
| Relógios | Avaliar apps complementares nativos para watchOS/Wear OS após estabilizar os fluxos principais |
| Contratos | OpenAPI como contrato único; tipos gerados/validados para clientes |
| Autenticação | OAuth2/OIDC quando definido, tokens curtos, renovação segura e MFA para áreas sensíveis |
| Releases | CI para testes, empacotamento, assinatura e distribuição por plataforma |

A fundação em `clients/instrua-app/` é uma interface React + TypeScript com Tauri 2 e scripts para Windows, Android e iOS. É um ponto de partida, não garantia de compatibilidade final. Android precisa ser compilado e testado em aparelhos reais de diferentes tamanhos e fabricantes; iOS exige macOS/Xcode; macOS exige alvo e testes próprios. Relógios exigem implementação específica. A interface usa dados fictícios e ainda não está integrada ponta a ponta ao backend.

## Experiências por dispositivo

- **iPhone:** agenda, confirmações, instruções, notificações, documentos autorizados e ações rápidas acessíveis.
- **iPad:** painéis e agenda com mais espaço, retrato/paisagem, teclado e multitarefa quando suportados.
- **Mac:** fluxo de recepção/gestão com teclado, atalhos, janelas e proteção para ambientes compartilhados.
- **Apple Watch:** lembretes discretos e ações curtas; ocultar dados clínicos sensíveis por padrão.
- **Celulares Android:** navegação adaptada aos padrões Android, acessibilidade, diferentes densidades, recortes de tela e modos de navegação.
- **Tablets e dobráveis Android:** layouts de uma ou duas colunas conforme espaço, redimensionamento, transição entre telas e continuidade de estado.
- **Wear OS:** possível complemento para lembretes e confirmações simples, sem dados sensíveis expostos.
- **Web:** acesso responsivo sem exigir instalação.
- **Windows:** experiência desktop com teclado e telas amplas.

## Áreas e permissões

- **Paciente/cliente:** busca, perfil único, agendamento, aceite de política, lembretes, instruções, documentos disponibilizados, preferências, dados próprios e acessibilidade.
- **Profissionais e equipes de saúde:** agenda e tarefas conforme vínculo; dados clínicos apenas mediante autorização contextual e necessidade assistencial.
- **Clínica/hospital/rede:** recepção, agenda, equipes, documentos, autorização, operações, financeiro/fiscal, auditoria e relatórios conforme permissões.
- **Outros estabelecimentos:** catálogo, agenda, pedidos, comunicação e operações; nunca recebem acesso ao prontuário só por estarem na mesma plataforma.

## Segurança multiplataforma

1. Toda requisição é autorizada no backend por conta, organização, unidade, equipe, vínculo, contexto e permissão mínima.
2. Usar armazenamento seguro de credenciais: Keychain no ecossistema Apple, Android Keystore no Android e armazenamento protegido do sistema no desktop.
3. Segredos de provedores ficam no servidor ou em secret manager; clientes recebem apenas configuração pública necessária.
4. Downloads clínicos exigem autorização por recurso, URLs temporárias, expiração, rastreabilidade e cache restrito.
5. Push, e-mail, SMS e WhatsApp respeitam preferências, consentimento e limites do canal.
6. Atualizações precisam de assinatura, verificação de integridade e política de versão suportada.
7. Notificações de tela bloqueada devem ocultar detalhes clínicos e permitir controle de prévias.
8. Validar acessibilidade, permissões do sistema, consumo de bateria e comportamento em conexões instáveis.

## Etapas de entrega

1. Fechar contratos REST/OpenAPI e critérios de autorização.
2. Fazer os fluxos de paciente e agenda funcionarem com backend real.
3. Construir o site React conectado ao backend.
4. Validar e empacotar Android e iOS em aparelhos reais, com diferentes telas e versões de sistema.
5. Testar tablets, dobráveis, orientação, redimensionamento, acessibilidade e restauração de estado.
6. Criar e validar clientes desktop Windows e, em etapa própria, macOS/Linux.
7. Avaliar apps de relógio somente após estabilizar os fluxos principais.
8. Automatizar build/testes e releases por plataforma; configurar assinatura, privacidade, acessibilidade e distribuição.
9. Considerar outros dispositivos apenas após validar demanda e viabilidade.

## Não negociáveis

- A mesma conta funciona nos ambientes suportados.
- Não cobrar do paciente pelo núcleo essencial.
- Não duplicar permissões ou regras clínicas em cada cliente.
- Funcionalidade exibida precisa ser suportada pelo backend; integrações não conectadas devem ser identificadas como indisponíveis/em configuração.
- Antes de produção: threat model, LGPD, revisão de segurança, testes de isolamento entre organizações e validação dos fluxos de documentos/financeiro.
- Não declarar uma plataforma suportada até haver build, testes de interface, acessibilidade, privacidade e distribuição verificados nela.
