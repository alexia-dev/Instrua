# Clientes do Instrua

- [Aplicativos desktop, Android e iOS](./instrua-app/README.md): projeto compartilhado React + TypeScript + Tauri 2 para Windows, Android e iOS (build iOS exige macOS/Xcode).
- [Arquitetura multiplataforma](../docs/PLATFORM_CLIENTS_ARCHITECTURE.md): identidade, permissões e estratégia de clientes.

O desktop e o Android usam o mesmo frontend, com layout responsivo. O backend continua sendo a fonte de verdade para identidade, regras, permissões e dados. A interface inicial contém telas demonstrativas e precisa ser ligada aos endpoints reais antes de uso operacional.