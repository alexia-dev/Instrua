# Clientes do Instrua

O Instrua é um produto integrado com experiências adaptadas ao dispositivo, não aplicativos isolados com contas diferentes.

- [Cliente compartilhado desktop/mobile](./instrua-app/README.md): React + TypeScript + Tauri 2, com base inicial para Windows e scripts de inicialização/build Android e iOS.
- [Arquitetura multiplataforma](../docs/PLATFORM_CLIENTS_ARCHITECTURE.md): estratégia para web, iPhone, iPad, Mac, Apple Watch (futuro) e outras plataformas.

## Escopo Apple

- **iPhone e iPad:** alvo iOS/iPadOS a validar em Mac com Xcode e em dispositivos reais. O layout deve ser testado em tamanhos, orientações e modos multitarefa relevantes.
- **Mac:** exige alvo macOS, configuração, build, assinatura e testes próprios; não é considerado pronto só porque existe uma build Windows.
- **Apple Watch:** não implementado nesta fase. Planejado como possível app complementar para notificações e ações rápidas, sem prontuário completo.
- **Apple Vision Pro:** sem suporte prometido; avaliar apenas se houver caso de uso e validação técnica.

A base atual é inicial: usa dados demonstrativos e ainda precisa ser conectada ao backend com autenticação/autorização reais antes de uso operacional. Nenhuma build assinada ou publicação em loja é garantida neste momento.
