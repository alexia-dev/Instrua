# Instrua — demo interativa

## Abrir
A entrada de frontend redireciona para [demo.html](./demo.html). Para executar localmente, abra `frontend/demo.html` em um navegador moderno.

## Fluxos demonstráveis
- Site público, busca de serviços, entrada e cadastro simulados.
- Alternância de perfil: cliente, profissional, estabelecimento e administração.
- Agenda, criação e cancelamento de agendamentos.
- Oportunidades: publicação, edição, prazo, preço fixo, desconto opcional e solicitação de reserva.
- Biblioteca de instruções com autoria, versão e estado de leitura.
- Equipe, serviços e clientes.
- Instrua Financeiro: recebimentos, despesas, repasses e status.
- Instrua Fiscal: rascunhos de documentos fiscais e estados demonstrativos; não emite NFS-e.
- Instrua Protege: jornada, entrada, saída, intervalos e divergências demonstrativas.
- Registros, relatórios, auditoria, notificações e Dante com respostas locais de demonstração.
- Tema claro/escuro, alto contraste e escala global do texto.
- Persistência local por navegador, exportação de dados JSON e exportação CSV de auditoria.

## Limites conhecidos
Esta é uma demo frontend: os dados são fictícios e as alterações ficam no `localStorage` deste navegador. Não há autenticação real, banco/API conectados, concorrência de reservas, envio de WhatsApp/SMS/e-mail, processamento de pagamentos, emissão de NFS-e, ponto oficial ou provedor de IA conectado. Não inserir dados reais ou sensíveis.

## Revisão manual sugerida
1. Alternar os quatro perfis e abrir os módulos disponíveis em cada um.
2. Criar um agendamento, abrir detalhes e cancelar.
3. Publicar uma oportunidade com desconto desativado por padrão; solicitar uma reserva como cliente.
4. Criar lançamento financeiro, rascunho fiscal, evento de jornada e registro.
5. Marcar instruções/notificações como lidas.
6. Alterar tema, contraste e escala de texto; recarregar e confirmar persistência local.
7. Exportar JSON/CSV e restaurar os dados iniciais.

A demo não substitui testes automatizados, revisão de segurança, integração com backend nem validação de acessibilidade antes de produção.
