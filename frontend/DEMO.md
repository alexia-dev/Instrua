# Instrua — demo interativa

## Abrir
A entrada do frontend redireciona para [demo.html](./demo.html). Para executar localmente, abra `frontend/demo.html` em um navegador moderno.

## Fluxos demonstráveis
- Site público, busca de serviços, entrada e cadastro simulados.
- Alternância de perfil: cliente, profissional, estabelecimento e administração.
- Instrua Social integrado ao layout e à navegação existentes:
  - perfis institucionais/profissionais publicam conteúdos;
  - clientes curtem e enviam comentários;
  - comentários ficam privados até aprovação;
  - profissionais/estabelecimentos podem responder a críticas publicadas;
  - comentários podem ser denunciados e retidos para análise.
- Central de Moderação com fila de aprovação/rejeição, denúncias e registros de ocorrência.
- Instrua Evidências: preservação local do comentário original, contexto da publicação, metadados de demonstração e exportação de dossiê JSON.
- Agenda, criação e cancelamento de agendamentos.
- Oportunidades: publicação, edição, prazo, preço fixo, desconto opcional e solicitação de reserva.
- Biblioteca de instruções com autoria, versão e estado de leitura.
- Equipe, serviços e clientes.
- Instrua Financeiro: recebimentos, despesas, repasses e status.
- Instrua Fiscal: rascunhos de documentos fiscais e estados demonstrativos; não emite NFS-e.
- Instrua Protege: jornada, entrada, saída, intervalos e divergências demonstrativas.
- Registros, relatórios, auditoria, notificações e Dante com respostas locais.
- Tema claro/escuro, alto contraste e escala global do texto.
- Persistência local por navegador, exportação de dados JSON e exportação CSV de auditoria.

## Limites conhecidos
Esta é uma demo frontend: dados fictícios e alterações persistem apenas no `localStorage` deste navegador. Não há autenticação real, banco/API, isolamento de dados no servidor, concorrência de reservas ou notificações externas. Moderação e dossiês são demonstrações locais, não serviços de preservação forense. Exportações JSON não certificam autenticidade nem garantem validade jurídica. Não inserir dados reais ou sensíveis.

Antes de produção, implementar autorização e isolamento no backend, trilha de auditoria resistente a alterações, política de retenção, segurança de arquivos, revisão jurídica, testes automatizados e validação de acessibilidade. A moderação não deve tratar toda crítica como abuso nem presumir culpa com base em uma denúncia.

## Revisão manual sugerida
1. Alternar entre os perfis e abrir Instrua Social.
2. Como cliente, curtir uma publicação e enviar comentários em publicações diferentes; confirmar que ficam pendentes.
3. Como estabelecimento/profissional, abrir a Central de Moderação, aprovar ou rejeitar comentários e verificar que apenas os aprovados aparecem no feed.
4. Aprovar uma crítica e responder; verificar a resposta pública.
5. Denunciar um comentário e exportar o dossiê JSON; confirmar que o conteúdo original e o contexto estão incluídos.
6. Verificar que clientes não conseguem abrir a Central de Moderação.
7. Revisar agenda, oportunidades, instruções, módulos financeiros/fiscais, acessibilidade e exportações existentes.

A demo não foi executada em navegador nem submetida a testes automatizados nesta alteração. Não deve ser tratada como pronta para produção.
