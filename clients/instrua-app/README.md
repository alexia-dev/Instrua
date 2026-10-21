# Instrua — aplicativos desktop, Android e iOS

Este projeto compartilha a interface React + TypeScript através do Tauri 2 como base inicial para Windows desktop, Android e iOS. Layouts e fluxos devem ser validados por plataforma; compartilhar componentes não significa que todo dispositivo já esteja suportado.

## O que existe nesta primeira base

- Interface responsiva com navegação inicial por Início, Agenda, Pacientes, Orientações, Financeiro, Protege e Configurações.
- Identidade visual oficial Instrua.
- Tela de configuração da URL base da API.
- Configuração Tauri 2 para desktop Windows e scripts de inicialização/build Android e iOS.
- Fluxos demonstrativos; não usar dados de pacientes reais.

## Requisitos gerais

- Node.js LTS e npm.
- Windows: Rust estável com toolchain MSVC, Microsoft C++ Build Tools e WebView2.
- Android: Android Studio/SDK, JDK suportado e toolchain Android.
- iOS: Mac com Xcode, ferramentas de linha de comando, targets Rust Apple e assinatura/configuração de desenvolvimento.
- Consulte os pré-requisitos oficiais do Tauri 2: https://v2.tauri.app/start/prerequisites/

## Executar no navegador

```bash
npm install
npm run dev
```

## Windows desktop

```bash
npm run desktop
npm run desktop:build
```

## Android

Inicialize uma vez e execute:

```bash
npm run android:init
npm run android:dev
npm run android:build
```

### Compatibilidade Android a validar

A compilação do pacote não garante compatibilidade universal. Antes de distribuição, testar em celulares e tablets de fabricantes diferentes e, quando possível, em dobráveis. A matriz de testes deve cobrir:

- versões Android suportadas e diferentes densidades/resoluções;
- recortes, barras de sistema, navegação por gestos e botões;
- retrato/paisagem, tablets, divisão de tela e redimensionamento;
- retomada do app, rotação, perda/retorno de conexão e restauração do estado;
- acessibilidade, tamanho de fonte, leitor de tela, contraste e áreas de toque;
- permissões, notificações, consumo de bateria e comportamento em segundo plano;
- armazenamento seguro de credenciais e ausência de dados clínicos em logs/notificações.

O suporte a tablets e dobráveis exige ajustes e testes explícitos; não é garantido apenas pelo layout responsivo.

## iOS (iPhone e iPad)

A configuração inicial deve ser feita em um Mac com Xcode:

```bash
npm install
npm run ios:init
npm run ios:dev
npm run ios:build
```

Configure Bundle ID, assinatura e permissões; valide em simulador, iPhone e iPad reais, incluindo orientação, multitarefa e tamanhos de tela. Uma build não equivale a um app pronto para a App Store. Nenhum IPA assinado é prometido neste repositório.

## Backend

A configuração inicial aponta para `http://localhost:8080`. Em Configurações, é possível guardar outra URL localmente. Isso **não significa que todos os módulos estejam integrados**: as telas usam dados fictícios até cada fluxo ser ligado à API com autenticação e autorização reais.

Em dispositivos físicos, `localhost` refere-se ao próprio dispositivo, não ao computador. Use uma URL acessível na rede de desenvolvimento ou HTTPS em ambiente seguro. Não exponha a API sem TLS, autenticação e controles de acesso.

## Segurança antes de produção

- Não inserir dados de pacientes ou dados clínicos reais nesta versão demonstrativa.
- Não colocar tokens, senhas, chaves de provedores ou credenciais no frontend.
- Implementar sessão segura e armazenamento nativo protegido antes de autenticação real.
- Confirmar autorização no backend em cada endpoint, inclusive por organização, unidade e contexto assistencial.
- Validar CSP, CORS, atualizações, assinatura dos instaladores e tratamento de logs.
- Pagamentos, NFS-e, WhatsApp, push e IA externa não são integrações operacionais nesta base visual.
