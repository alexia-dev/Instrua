# Clientes do Instrua

O Instrua é um produto integrado com experiências adaptadas ao dispositivo, não aplicativos isolados com contas diferentes.

- [Cliente compartilhado desktop/mobile](./instrua-app/README.md): React + TypeScript + Tauri 2, base inicial para Windows e scripts de inicialização/build Android e iOS.
- [Arquitetura multiplataforma](../docs/PLATFORM_CLIENTS_ARCHITECTURE.md): estratégia para web, iPhone/iPad, Mac, Android, tablets, dobráveis e dispositivos complementares.

## Cobertura planejada

- **Android — celulares:** validar em fabricantes, tamanhos de tela, densidades, versões de sistema, recortes e navegação por gestos/botões.
- **Android — tablets e dobráveis:** layouts adaptáveis, orientação, redimensionamento, continuidade de estado e multitarefa conforme suportados.
- **Wear OS:** não implementado; avaliar futuramente para lembretes e ações rápidas sem exposição de dados clínicos.
- **iPhone/iPad:** exige geração e validação em macOS/Xcode e testes reais.
- **Mac:** exige alvo macOS, configuração, build, assinatura e testes próprios.
- **Apple Watch:** não implementado; possível complemento futuro.
- **Web e desktop Windows:** clientes separados que compartilham conta, backend e regras.

A base atual é inicial, usa dados demonstrativos e ainda precisa de integração real com autenticação/autorização antes de uso operacional. Nenhuma build assinada ou publicação em loja é garantida neste momento.
