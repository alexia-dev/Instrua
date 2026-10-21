# Matriz de compatibilidade de dispositivos — Instrua

## Objetivo

Criar uma única plataforma Instrua com interface adaptável, sem favorecer fabricantes. O código compartilhado é uma base; cada plataforma só pode ser declarada suportada depois de build, teste e validação próprios.

## Matriz de alvos

| Categoria | Base de implementação | O que validar antes de declarar suporte |
|---|---|---|
| Web desktop/mobile | React + TypeScript + Vite | Safari, Chrome, Edge; viewports; teclado; acessibilidade |
| Android phones | Tauri 2 + WebView Android | Fabricantes e versões Android, densidades, recortes, gestos/botões, permissões, notificações e retomada |
| Android tablets | Layout responsivo compartilhado | Retrato/paisagem, janela dividida, teclado/ponteiro e espaço ampliado |
| Android foldables | Layout responsivo compartilhado | Abrir/fechar, resize, panes/hinge quando expostos pelo SO, restauração de estado |
| Wear OS | Complemento futuro | Notificações discretas e ações rápidas; sem dados clínicos detalhados |
| iPhone | Tauri iOS + WebView | Build em Mac/Xcode, safe areas, teclado, permissões, VoiceOver, orientação e instalação real |
| iPad | Tauri iOS + WebView | Retrato/paisagem, multitarefa, teclado/ponteiro, safe areas e janelas |
| Mac | Alvo macOS próprio a configurar | Apple Silicon/Intel quando aplicável, assinatura, atalhos, janelas, permissões e atualizações |
| Apple Watch | Complemento futuro | Ações curtas, notificações privadas e integração com app principal |
| Windows desktop | Tauri desktop | Instalador, WebView2, escala, teclado, leitor de tela, assinatura e atualização |
| Linux desktop | Futuro | Distribuições escolhidas, dependências, empacotamento e atualizações |
| Apple Vision Pro | Sem compromisso de suporte | Só iniciar após caso de uso e protótipo de acessibilidade/UX |

## Base de UI adicionada

- Viewport com viewport-fit=cover e tema da marca.
- Espaços seguros com env(safe-area-inset-*) para dispositivos com recortes/áreas reservadas.
- Alvos de toque de pelo menos 44 px na interface comum.
- Foco visível, movimento reduzido e modo de alto contraste/forced colors.
- Breakpoints para celular, tablet e desktop; layouts refluem para evitar overflow.
- Ajustes iniciais para paisagem baixa e dispositivos com segmentos de tela quando o navegador expõe essa informação.

## Política de compatibilidade

1. Não bloquear por marca ou modelo quando o dispositivo atende aos requisitos técnicos.
2. Não presumir que um app Android funciona em todos os fabricantes só porque compila.
3. Manter a mesma identidade, conta, API e regras de autorização em todos os clientes.
4. Adaptar navegação e densidade de informação às convenções de cada plataforma, sem duplicar regras de negócio.
5. Testar teclado, leitor de tela, fonte, contraste, orientação, rede instável, retomada e atualizações.
6. Não mostrar conteúdo clínico sensível em notificações ou telas bloqueadas por padrão.
7. Não usar dados reais de pacientes nos testes de interface; usar fixtures sintéticas.
8. Registrar dispositivos/versões testados por release e documentar limitações conhecidas.

## Próximas etapas técnicas

- [ ] Rodar build web/TypeScript e corrigir regressões.
- [ ] Configurar e testar build Android em emulador e aparelhos físicos.
- [ ] Validar celulares, tablets e dobráveis em uma matriz de tamanhos.
- [ ] Gerar e validar iOS/iPadOS em Mac com Xcode.
- [ ] Criar alvo macOS e testar Apple Silicon/Intel conforme escopo.
- [ ] Configurar armazenamento seguro de sessão e integração real com API.
- [ ] Adicionar testes automatizados de layout/fluxos e checklist de release.
- [ ] Planejar Watch/Wear OS somente após os fluxos principais.
