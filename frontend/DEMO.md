# Instrua — demo interativa

## Posicionamento do produto
O Instrua é uma plataforma de saúde e coordenação de atendimentos em primeiro lugar — não um produto genérico de estabelecimentos. A jornada central é paciente/cliente, profissionais de saúde, equipes assistenciais e administrativas, clínicas, hospitais, centros de exames e redes. Outros tipos de estabelecimentos podem usar a plataforma depois, sobre a mesma base de agenda, comunicação, equipe e operação.

## Abrir
A entrada do frontend redireciona para [demo.html](./demo.html). Para executar localmente, abra `frontend/demo.html` em um navegador moderno.

## Perfis e fluxos demonstrativos
- Cliente/paciente: busca, consultas e exames, oportunidades, orientações, notificações e perfil.
- Profissional de saúde: agenda clínica, disponibilidade, atendimentos, instruções, registros autorizados e perfil profissional.
- Equipe de saúde: experiência própria para rotina, tarefas, protocolos e registros conforme permissão; representa funções clínicas e administrativas.
- Clínica/hospital/serviço de saúde: agenda integrada, equipes, especialidades, pacientes, exames, oportunidades, orientações, financeiro/fiscal, jornada, registros, relatórios e configurações.
- Outro estabelecimento/empresa: módulos gerais de agenda e operação para setores além da saúde.
- Administração da plataforma: visão de governança e módulos globais.
- Planos e recursos: princípios de gratuidade para pacientes, planos profissionais/institucionais e faixas de preço sugeridas para validação. Os valores são hipóteses, não uma tabela comercial aprovada.

## Saúde e módulos
- Jornada do paciente: agendamento, confirmação, cancelamento/remarcação conforme regras, instruções de preparo, lembretes e histórico.
- Agenda e oportunidades de horário, sem leilão nem disputa de preço.
- Equipe e permissões; na versão real, permissões devem ser granulares por função, unidade, vínculo e contexto.
- Instrua Social: publicações de clínicas/profissionais, curtidas, comentários e moderação.
- Central de Moderação: fila de aprovação/rejeição, respostas a críticas, denúncias e registros de ocorrência.
- Instrua Evidências: preservação local de conteúdo e contexto e exportação de dossiê JSON demonstrativo.
- Biblioteca de orientações com autoria, versão e estado de leitura.
- Instrua Financeiro e Fiscal: lançamentos e rascunhos demonstrativos; não processa pagamentos nem emite NFS-e real.
- Instrua Protege: jornada, entrada, saída, intervalos e divergências demonstrativas; não é ponto oficial.
- Registros, relatórios, auditoria, notificações e Dante com respostas locais.
- Acessibilidade: tema, alto contraste e escala do texto.
- Persistência local, exportação JSON e CSV de auditoria.

## Modelo comercial — princípios
- Gratuito para pacientes: busca, agendamento básico, confirmação, instruções essenciais, lembretes essenciais e acesso aos próprios dados.
- Profissional individual: plano gratuito de entrada e Pro opcional.
- Clínicas/consultórios: assinatura por unidade/faixa de capacidade, sem cobrar por cada paciente ou ação essencial.
- Centros multidisciplinares e de exames: planos por escala, unidades e necessidades operacionais.
- Hospitais/redes: contrato Enterprise com implantação, integrações, governança, suporte e SLA definidos.
- Equipes assistenciais e administrativas: acesso incluído no contrato institucional conforme permissões; não cobrar cada profissional individualmente se o sistema é contratado pela instituição.
- Extras possíveis: automação com cotas transparentes, agenda avançada/lista de espera, indicadores, Protege, Financeiro/Fiscal, integrações, migração, implantação e IA assistiva.
- Não transformar acesso a orientações críticas, acessibilidade básica, privacidade ou dados do próprio paciente em upsell.
- Preços no protótipo são hipóteses para pesquisa e não devem ser anunciados como preços finais.

## Limites conhecidos
Esta é uma demo frontend: dados fictícios e alterações persistem apenas no `localStorage` deste navegador. Não há autenticação real, banco/API, isolamento de dados no servidor, concorrência de reservas ou notificações externas. Os perfis de equipe/instituição são demonstrativos e não substituem autorização real por cargo, unidade e contexto. Não inserir dados reais ou sensíveis.

Moderação e dossiês são demonstrações locais, não serviços de preservação forense. Exportações JSON não certificam autenticidade nem garantem validade jurídica. Antes de produção, implementar autorização e isolamento no backend, trilha de auditoria resistente a alterações, política de retenção, segurança de arquivos, revisão jurídica, testes automatizados, acessibilidade e integrações clínicas/fiscais validadas. A moderação não deve tratar toda crítica como abuso nem presumir culpa com base em uma denúncia.

## Revisão manual sugerida
1. Alternar os perfis e verificar que o conteúdo se adapta ao papel.
2. Como paciente, navegar pelo feed, buscar serviços e revisar consultas e instruções.
3. Como profissional, revisar agenda, disponibilidade, registros e planos.
4. Como equipe, revisar rotina, tarefas e protocolos.
5. Como clínica/hospital, explorar agenda integrada, equipes, serviços, relatórios e planos.
6. Como paciente, enviar um comentário e verificar que fica pendente.
7. Como profissional/instituição, aprovar/rejeitar, responder a crítica e denunciar conteúdo.
8. Exportar um dossiê e conferir os limites documentados.
9. Abrir Planos e Recursos e revisar os princípios de preço.

A demo não foi executada em navegador nem submetida a testes automatizados nesta alteração. Não deve ser tratada como pronta para produção.

## Identidade visual oficial — referência do PDF

Use estas cores exatas em todos os clientes do Instrua (site, Android/mobile e desktop). Não crie paletas por perfil ou por módulo; a mesma identidade deve permanecer consistente.

| Token | Cor oficial | Hex | Uso recomendado |
|---|---|---|---|
| `moss` / verde-musgo | Verde-musgo | `#414A35` | Ações principais, identidade, navegação ativa e títulos com ênfase |
| `olive` / oliva | Oliva | `#69704B` | Navegação secundária, rótulos e elementos de apoio |
| `sage` / sálvia | Sálvia | `#96977B` | Elementos secundários, bordas de apoio e estados não críticos |
| `sand` / areia | Areia | `#D6C9AD` | Bordas, divisores e superfícies discretas |
| `terra` / terracota | Terracota | `#A66F4B` | Destaques editoriais e ações secundárias selecionadas; não usar como único indicador de erro |
| `amber` / âmbar | Âmbar | `#C18A43` | Avisos leves, foco e destaques pontuais |
| `paper` / off-white | Off-white | `#F5F1E8` | Fundo principal e telas de leitura |

Superfície padrão sugerida: `#FFFCF6`; texto principal: `#34382D`; texto secundário: `#686858`. Esses tons auxiliares não substituem as sete cores oficiais.

### Regras visuais
- Verde-musgo é a cor primária do produto. Evitar interfaces com grandes áreas terracota/âmbar.
- Preferir superfícies off-white, cartões claros, bordas areia e espaçamento generoso.
- A cor nunca deve ser o único sinal de status; usar texto/ícone e manter foco visível e navegação por teclado.
- Confirmar contraste WCAG, estados de foco, hover, selecionado, desabilitado, erro e sucesso em cada cliente.
- Criar tokens equivalentes no React, Android e desktop. Evitar repetir valores hex literais em componentes; usar tokens semânticos.
- O layout pode mudar com o dispositivo, mas tipografia, cores, semântica dos botões, estados e princípios de acessibilidade continuam compartilhados.

### Arquivos atualizados nesta branch
- `frontend/demo.html`: protótipo interativo com a paleta oficial aplicada.
- `frontend/styles.css`: tokens da identidade visual para novos componentes.
- `docs/INSTRUA_DESIGN_SYSTEM.md`: referência de design multiplataforma.

## Configuração do backend a partir do protótipo

O protótipo contém um bootstrap `window.instruaApi` para facilitar a evolução sem embutir uma URL de produção. Configure a meta `instrua-api-base` em `frontend/demo.html` por ambiente; para teste local também é possível passar `?api=http://localhost:8080` na URL (se o navegador permitir CORS).

Exemplo de uso no navegador:
```js
// A API precisa estar configurada; não coloque segredos aqui.
const status = await window.instruaApi.health();
const session = await window.instruaApi.login(email, password);
```

O token demonstrativo fica em `sessionStorage`, e não deve ser tratado como armazenamento final de produção. Antes de usar dados reais, definir renovação/expiração de sessão, proteção contra XSS, estratégia de logout, políticas de CSP/CORS e controles de autenticação adequados.

Os fluxos atuais da demo continuam usando dados locais para preservar a demonstração existente. O bootstrap apenas prepara um ponto único de integração; ele não significa que todas as telas foram conectadas ao backend. O cliente precisa detectar API não configurada/indisponível e nunca exibir dados fictícios como se fossem dados do servidor.
