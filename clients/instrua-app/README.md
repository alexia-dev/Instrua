# Instrua — aplicativos desktop e Android

Este projeto compartilha a mesma interface React + TypeScript entre **Windows desktop** e **Android** através do Tauri 2. A adaptação ao celular é feita por layout responsivo; regras de negócio e dados reais devem continuar no backend central.

## O que já está neste primeiro pacote

- Interface adaptável para telas grandes e celulares.
- Navegação inicial por Início, Agenda, Pacientes, Orientações, Financeiro, Protege e Configurações.
- Identidade visual oficial Instrua.
- Tela de configuração da URL da API.
- Base Tauri para desktop Windows e inicialização de alvo Android.
- Fluxos demonstrativos claramente identificados; sem usar dados reais.

## Requisitos

- Node.js LTS e npm.
- Para Windows: Rust estável com toolchain MSVC, Microsoft C++ Build Tools e WebView2.
- Para Android: Android Studio/SDK, JDK suportado pelo toolchain instalado e dependências Android do Tauri. Consulte os pré-requisitos oficiais do Tauri 2: https://v2.tauri.app/start/prerequisites/

## Executar a interface no navegador

```bash
cd clients/instrua-app
npm install
npm run dev
```

## Executar o desktop

```bash
cd clients/instrua-app
npm install
npm run desktop
``

Gerar instalador Windows:

```bash
npm run desktop:build
```

Os instaladores são gerados em `src-tauri/target/release/bundle/`.

## Preparar e executar Android

Execute uma vez para gerar o projeto Android nativo a partir da configuração Tauri:

```bash
cd clients/instrua-app
npm install
npm run android:init
npm run android:dev
```

Gerar o pacote Android:

```bash
npm run android:build
```

A primeira configuração Android pode exigir downloads do SDK/Gradle/Rust e configuração do ambiente. O projeto usa o mesmo frontend nos dois clientes, mas os artefatos finais precisam ser compilados no ambiente de build.

## Backend

A configuração inicial aponta para `http://localhost:8080`. Em Configurações, é possível guardar outra URL localmente. Isso **não significa que todos os módulos já estejam integrados**: os dados mostrados nas telas são fictícios até a ligação de cada fluxo à API, autenticação e autorização reais.

Para usar no celular físico, `localhost` refere-se ao próprio celular, não ao computador. Configure um endereço de API acessível pela rede de desenvolvimento ou um endpoint HTTPS de ambiente. Não exponha a API à internet sem TLS, autenticação e regras de acesso.

## Segurança antes de produção

- Não inserir dados de pacientes ou dados clínicos reais nesta versão demonstrativa.
- Não colocar tokens, senhas, chaves de provedores ou credenciais no frontend.
- Implementar sessão segura e armazenamento nativo protegido antes de autenticação real.
- Confirmar autorização no backend em cada endpoint, inclusive por organização, unidade e contexto assistencial.
- Validar CSP, CORS, política de atualizações, assinatura de instaladores e tratamento de logs antes de distribuir.
- Pagamentos, NFS-e, WhatsApp, push e IA externa não são conectados por esta base visual.
