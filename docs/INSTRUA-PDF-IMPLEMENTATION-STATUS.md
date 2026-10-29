# Implementação do PDF oficial — estado do repositório

Este documento separa o que já foi codificado nesta etapa do que ainda depende de backend, persistência, autorização e integração. O PDF oficial de 33 páginas continua sendo a referência visual e funcional.

## Referência de produto

- Páginas 1–3: marca, cores, tipografia Inter, escala de espaçamento, foco visível, estados sem depender apenas de cor, controles de toque e responsividade.
- Páginas 4–9: landing page, entrar, cadastro por perfil, recuperação de senha e escolha de perfil.
- Páginas 10–18: área do cliente, busca e filtros, detalhe de serviço, calendário, reserva de agendamento, histórico, instruções e Oportunidades.
- Páginas 19–22: agenda profissional, disponibilidade, bloqueios, detalhes do atendimento e envio de instruções.
- Páginas 23–27: painel do estabelecimento, agenda geral, equipe, permissões, serviços, funcionamento, publicações e reservas.
- Páginas 28–32: acessibilidade, tema claro/escuro, alto contraste e escala de texto de 100% a 150%.
- Página 33: estados de carregamento, erro, vazio, confirmação, cancelamento e prazo de reserva expirado.

## Entregue nesta etapa

- [x] Criado `frontend/index.html`, `frontend/styles.css` e `frontend/app.js` como interface navegável responsiva.
- [x] Criada navegação por perfil para cliente, profissional, estabelecimento e administração.
- [x] Criadas telas demonstrativas para busca, serviços, agendamento, oportunidades de preço fixo, histórico, instruções, notificações, perfil, agenda, disponibilidade, equipe, serviços, clientes, relatórios e configurações.
- [x] Adicionados controles de tema claro/escuro, alto contraste, escala de texto, navegação por teclado e link para pular ao conteúdo.
- [x] Oportunidades são representadas como horários de preço fixo, com prazo e sem lances — não como leilão.
- [x] Mantida indicação explícita de que os dados e ações no frontend são demonstrativos e não são enviados ao servidor.
- [x] Adicionados os papéis `PROFESSIONAL` e `AUDITOR` ao enum de domínio e habilitada autorização por método Spring Security.
- [x] Aplicadas restrições de papel iniciais ao controlador de agendamentos.

## Ainda não deve ser tratado como pronto para produção

O frontend recém-criado usa dados demonstrativos em memória e não persiste reservas, convites, preferências ou alterações. Botões não integrados avisam que a API precisa ser conectada. O backend também não está completo para todos os módulos apresentados no PDF.

## Próximas entregas obrigatórias de backend

1. **Agenda e reservas**: disponibilidade real, bloqueios, prevenção transacional de conflitos, expiração de prazos, capacidade por oportunidade, idempotência e cancelamento conforme regras do serviço.
2. **Oportunidades**: entidade persistida com empresa, serviço, profissional, horário, valor fixo, quantidade, prazo de reserva, estado e trilha de eventos; publicar, editar, encerrar, reservar e expirar com transação e controle de concorrência.
3. **Instruções e arquivos**: destinatários autorizados, status de leitura, anexos com limites, armazenamento privado e links temporários; nunca expor documentos publicamente.
4. **Notificações**: caixa de entrada persistida, leitura individual/em lote, preferências e adaptadores de e-mail/mensageria configuráveis. Não afirmar envio sem provedor ativo.
5. **Equipe e permissões**: convites com expiração, vínculo a uma organização, permissões granulares no servidor e remoção/revogação de acesso.
6. **Acessibilidade e perfil**: preferências persistidas por usuário; validação dos dados, documentos com finalidade explícita, exportação e solicitação de encerramento de conta.
7. **Estabelecimento**: serviços, responsáveis, salas, horários de funcionamento, agenda agregada e relatórios limitados à organização.
8. **Instrua Protege / Registros / Clínico**: domínios separados, acesso por finalidade, auditoria e minimização; não misturar registros clínicos com logs operacionais.
9. **Financeiro/fiscal**: separar permissões, responsável legal e integração. Nenhuma emissão fiscal ou pagamento pode ser apresentado como real antes de integração e homologação.
10. **Segurança**: validar tenant e propriedade em cada serviço, adicionar testes de autorização, isolamento entre empresas, migrações Flyway e testes de concorrência.
11. **Estados de interface**: ligar loading/erro/vazio/sucesso/expirado a respostas reais da API e preservar filtros/entrada em falhas.

## Critério de conclusão

Uma tela não conta como funcionalidade concluída só por existir no frontend. Para marcar um item como implementado, exigir contrato REST, validação, persistência/migração, autorização no servidor, auditoria quando aplicável, testes automatizados e estado de erro/vazio/sucesso na interface.
