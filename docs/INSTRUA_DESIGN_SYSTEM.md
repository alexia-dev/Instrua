# Instrua Design System

**Fonte da identidade:** PDF de produto do Instrua.

## Complemento aprovado — Stitch Organic Modern (24/11/2026)

O pacote de 27 telas enviado nesta data é a **referência mais recente para aparência e experiência** da plataforma, além da marca já registrada acima. Mapeamento completo em [STITCH_ORGANIC_27_TELAS_INTEGRACAO.md](./STITCH_ORGANIC_27_TELAS_INTEGRACAO.md). Os HTMLs originais são protótipos e não podem substituir a aplicação de produção.

**Fontes:** Newsreader (títulos editoriais e subtítulos) e Plus Jakarta Sans (conteúdo, formulários, legendas e botões).

**Tokens Stitch para a interface:**

| Token semântico | Cor | Aplicação |
|---|---|---|
| `primary` | `#2B3320` | Contraste alto e ações primárias |
| `primary-container` | `#414A35` | Marca, cabeçalhos e foco |
| `secondary` | `#566247` | Texto/ação secundária |
| `sage-accent` | `#7E8B6D` | Sucesso e detalhes suaves |
| `tertiary-accent` | `#C86446` | Ênfase controlada, aviso contextual |
| `surface` | `#FCF9F0` | Fundo geral |
| `surface-container-low` | `#F6F3EB` | Painéis e cartões |
| `surface-container` | `#F0EEE5` | Campos, elementos agrupados |
| `surface-container-high` | `#EBE8DF` | Relevo/seleção suave |
| `on-surface` | `#1C1C17` | Texto principal |
| `outline-variant` | `#C6C7BD` | Separadores sutis |

**Composição:** curvas assimétricas nos cartões (ex.: 32px 16px 30px 18px), espaçamento orgânico de 4/8/16/28/40px, bordas delicadas, sombras difusas com opacidade baixa e áreas amplas para leitura. Barra de navegação lateral apenas onde a função justificar; celular com fluxo de uma coluna ou abas inferiores acessíveis.

**Breakpoints Stitch:** desktop >= 1024px (grid 12), tablet 768–1023px (grid 8), celular < 768px (grid 4). Respeitar fonte ampliada, alto contraste, preferências de movimento reduzido, foco por teclado e leitores de tela.

**Diferenciação por pessoa:** cliente tem jornada pessoal; profissional tem agenda e atendimento autorizado; recepção tem agenda, lista e orientações operacionais; gestão tem visão da organização; financeiro e plataforma acessam apenas seus dados/poderes. Uma única conta pode alternar entre contextos vinculados pelo servidor.

**Regra de veracidade:** não reproduzir dados fictícios “Clara”, “Beatriz”, clínicas ilustrativas, números apresentados no protótipo, assinatura ICP-Brasil, Gov.br, biometria, integração FHIR, alegações ISO/LGPD ou respostas simuladas como se fossem recursos ativos.

---

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
