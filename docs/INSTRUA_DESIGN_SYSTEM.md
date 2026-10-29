# Instrua Design System

**Fonte da identidade:** PDF de produto do Instrua.

## Paleta oficial

| Token | Nome | Hex |
|---|---|---|
| `moss` | Verde-musgo | `#414A35` |
| `olive` | Oliva | `#69704B` |
| `sage` | Sálvia | `#96977B` |
| `sand` | Areia | `#D6C9AD` |
| `terra` | Terracota | `#A66F4B` |
| `amber` | Âmbar | `#C18A43` |
| `paper` | Off-white | `#F5F1E8` |

## Tokens semânticos auxiliares

| Token | Hex | Uso |
|---|---|---|
| `surface` | `#FFFCF6` | Cartões e superfícies |
| `text` | `#34382D` | Texto principal |
| `muted` | `#686858` | Texto secundário |
| `border` | `#D6C9AD` | Bordas/divisores |
| `soft` | `#E9EDDF` | Fundo de estado selecionado |
| `danger` | `#8E4038` | Erros e ações destrutivas, com rótulo textual |
| `ok` | `#356A4C` | Sucesso, sempre com rótulo/ícone |

## Regras

1. Usar verde-musgo como principal em botões de ação e marca.
2. Reservar âmbar para avisos suaves/foco e terracota para destaques secundários.
3. Não usar a cor como único meio de transmitir status; incluir rótulo, ícone ou texto.
4. Garantir contraste, foco visível, navegação por teclado, escala de texto e estados desabilitados.
5. Usar os mesmos tokens semânticos no site, no app mobile e no app desktop.
6. Não introduzir paletas separadas para Saúde, Social, Financeiro/Fiscal ou Protege; os módulos compartilham a identidade.
7. Aplicar layout responsivo e acessível, mantendo os mesmos princípios de interação entre dispositivos.

## Clientes previstos

- Site: React + TypeScript.
- App Android: Kotlin + Jetpack Compose.
- App desktop: Tauri + React + TypeScript.
- Todos usam a API/backend único, uma conta por pessoa e as mesmas permissões server-side.

## Estado atual

O `frontend/demo.html` continua sendo o protótipo interativo existente, agora atualizado com a paleta oficial. O `frontend/styles.css` contém tokens reutilizáveis. O site React, cliente Android e aplicativo desktop ainda são trabalho de implementação futuro; esta documentação não afirma que já foram construídos.
