# Instrua — aplicativos desktop, Android e iOS

Este projeto compartilha a interface React + TypeScript entre **Windows desktop, Android e iOS** através do Tauri 2. A interface se adapta a telas grandes e pequenas; regras de negócio e dados reais devem continuar no backend central.

## O que existe nesta primeira base

- Interface responsiva com navegação inicial por Início, Agenda, Pacientes, Orientações, Financeiro, Protege e Configurações.
- Identidade visual oficial Instrua.
- Tela de configuração da URL base da API.
- Configuração Tauri 2 para desktop Windows e scripts de inicialização/build Android e iOS.
- Fluxos demonstrativos claramente identificados; não usar dados de pacientes reais.

## Requisitos gerais

- Node.js LTS e npm.
- Para Windows: Rust estável com toolchain MSVC, Microsoft C++ Build Tools e WebView2.
- Para Android: Android Studio/SDK, JDK suportado e toolchain Android.
- Para iOS: **um Mac com Xcode instalado e configurado**, ferramentas de linha de comando Xcode, Rust com os targets Apple necessários e assinatura/configuração de desenvolvimento Apple para instalar em dispositivo físico ou distribuir. O build iOS não é suportado nativamente em Windows.

Consulte os pré-requisitos oficiais do Tauri 2: https://v2.tauri.app/start/prerequisites/

## Executar a interface no navegador

```bash
npm install
npm run dev
```

## Windows desktop

```bash
npm run desktop
npm run desktop:build
```

Os instaladores são gerados em `src-tauri/target/release/bundle/`.

## Android

Inicialize uma vez e execute:

```bash
npm run android:init
npm run android:dev
npm run android:build
```

A configuração Android requer Android Studio/SDK e as dependências do Tauri. O pacote final precisa ser compilado e testado no ambiente configurado.

## iOS (iPhone)

A configuração inicial do alvo iOS deve ser feita em um Mac com Xcode:

```bash
npm install
npm run ios:init
npm run ios:dev
```

Para gerar a build iOS:

```bash
npm run ios:build
```

A geração de build não equivale a um app pronto para a App Store. Antes de instalar em iPhone ou distribuir, configure Bundle ID, equipe de assinatura, certificados/provisioning conforme o método de distribuição, permissões de privacidade, ícones e metadados; valide em simulador e dispositivo real. Publicação na App Store exige conta e revisão da Apple. Nenhum arquivo IPA assinado é prometido por este repositório neste momento.

## Backend

A configuração inicial aponta para `http://localhost:8080`. Em Configurações, é possível guardar outra URL localmente. Isso **não significa que todos os módulos estejam integrados**: os dados das telas são fictícios até cada fluxo ser ligado à API com autenticação e autorização reais.

Em dispositivos físicos, `localhost` refere-se ao próprio dispositivo, não ao computador. Use um endereço de API acessível na rede de desenvolvimento ou HTTPS em ambiente seguro. Não exponha a API à internet sem TLS, autenticação e controles de acesso.

## Segurança antes de produção

- Não inserir dados de pacientes ou dados clínicos reais nesta versão demonstrativa.
- Não colocar tokens, senhas, chaves de provedores ou credenciais no frontend.
- Implementar sessão segura e armazenamento nativo protegido antes de autenticação real.
- Confirmar autorização no backend em cada endpoint, inclusive por organização, unidade e contexto assistencial.
- Validar CSP, CORS, atualizações, assinatura dos instaladores e tratamento de logs antes de distribuir.
- Pagamentos, NFS-e, WhatsApp, push e IA externa não são integrações operacionais nesta base visual.
