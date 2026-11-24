# Regra obrigatória: paridade funcional do Instrua em todas as plataformas

**Status:** requisito permanente do produto e critério de aceite para toda mudança.

## Regra principal

Toda solicitação aprovada para o Instrua passa a fazer parte do escopo oficial e deve ser implementada no **frontend, backend, persistência e testes** sempre que aplicável. A funcionalidade deve estar disponível em todas as versões suportadas — **Web, Android, iOS e Desktop** — com uma implementação adaptada às convenções e capacidades de cada plataforma. Não basta documentar, criar uma tela demonstrativa, adicionar apenas o frontend, criar somente uma tabela/endpoint ou implementar em uma única versão.

O backend é a fonte comum de verdade para regras de negócio, permissões, validações e dados compartilhados. Os clientes devem consumir APIs autenticadas e autorizadas, sem duplicar regras críticas nem confiar em controles visuais para segurança.

## Como aplicar a paridade

- **Web:** interface responsiva para navegadores, teclado, mouse, acessibilidade e layouts de gestão.
- **Android:** fluxos adaptados a celular/tablet, navegação e acessibilidade Android, notificações e permissões nativas quando necessárias.
- **iOS:** fluxos adaptados a iPhone/iPad, padrões de navegação e acessibilidade Apple, notificações e permissões nativas quando necessárias.
- **Desktop:** fluxos para teclado/mouse, telas maiores, produtividade e recursos nativos disponíveis no cliente desktop.
- **Backend/API:** regra de negócio central, validação, autorização, isolamento por organização/contexto, persistência, auditoria, idempotência e integração com provedores.
- **Banco de dados:** migrações versionadas, integridade, índices e estratégia compatível com dados existentes.
- **Integrações:** abstrações compartilhadas no servidor quando aplicável; segredos nunca embutidos nos clientes.
- **Testes:** testes de domínio/API, autorização e isolamento entre organizações, testes de interface por cliente e testes de regressão.

Paridade não exige que as quatro interfaces sejam visualmente idênticas. Exige que os mesmos requisitos e resultados de negócio existam em cada plataforma, com UX apropriada. Se uma capacidade realmente não for suportada por um sistema operacional, registrar a limitação, alternativa e critério de aceite; não omitir silenciosamente.

## Escopo funcional oficial

As solicitações do produto devem ser rastreadas e entregues nos seguintes domínios, conforme a especificação principal:

1. Identidade única, múltiplos papéis/contextos, cadastro pessoal, qualificações e vínculos com organizações/unidades.
2. Convites profissionais: criar, enviar, visualizar, aceitar, recusar, pedir alterações, verificar, ativar, versionar e revogar.
3. Isolamento por organização e contexto, prontuários/documentos protegidos, autorização no backend, auditoria, direitos do titular, retenção e segurança de arquivos/segredos.
4. Agenda diária/semanal/mensal, disponibilidade, conflitos, intervalos mínimos, recorrência, encaixes, lista de espera, confirmação, cancelamento e histórico.
5. Cancelamento de dia pelo profissional, identificação de afetados, notificação pelos canais habilitados, opções de nova data, alternativa humana e remarcação sem transferência não autorizada.
6. Preparação contextual do profissional com apenas os dados necessários ao atendimento e à organização autorizada.
7. Instruções interativas (texto, mídia, documentos, checklists, idiomas, aprovação, confirmação de leitura, dúvidas e falhas de entrega).
8. Endereços e unidades corretas, mapa/rotas, acessibilidade, contatos e orientações de chegada.
9. Agenda de cuidados do cliente/paciente, documentos compartilhados, retornos, pendências e distinção entre educação, recomendação profissional e publicidade.
10. Solicitações clínicas com protocolo, análise profissional e entrega registrada; sem emissão autônoma de documentos que exijam decisão profissional.
11. Prontuários configuráveis por especialidade e planejamento validado de capacidades hospitalares/interoperabilidade.
12. Radar de Imprevistos: riscos, alternativas, simulações, impacto explicado e decisões auditáveis.
13. Dante AI: assistência contextual, limites por organização, transparência e confirmação para ações sensíveis.
14. Rede Social: publicações, comentários, reações, eventos, comunidades, descoberta, privacidade, denúncia e moderação.
15. Sugestões e votação de melhorias, estados, comentários, atualizações e proteção contra abuso.
16. Financeiro/fiscal: contas, pagamentos, conciliação, comissões, repasses, relatórios e conectores por jurisdição; nunca afirmar emissão sem confirmação confiável.
17. Instrua Protege: escalas, entradas/saídas, intervalos, divergências, justificativas, aprovação e auditoria.
18. Comunicação por email, push, WhatsApp Business Platform, SMS e outros canais configurados, com preferências, idempotência, entrega/falhas e transferência humana.
19. Segurança, LGPD, consentimento quando aplicável, retenção, direitos do titular, backups/restauração, incidentes e prevenção de abuso.

## Fluxo obrigatório para cada solicitação nova

1. Registrar a solicitação e relacioná-la aos domínios acima.
2. Inspecionar a implementação existente antes de criar novas entidades, tabelas, rotas ou componentes.
3. Mapear arquivos, telas/fluxos de Web, Android, iOS e Desktop, endpoints, regras, tabelas, integrações e testes.
4. Implementar o comportamento real no backend e na persistência, além de todas as interfaces suportadas.
5. Criar/atualizar migrações de banco e documentação de configuração quando necessário.
6. Adicionar testes unitários, integração, API, autorização, isolamento entre organizações e regressão de clientes.
7. Executar builds e testes relevantes para cada alvo que o repositório disponibilizar.
8. Conferir a matriz de paridade antes de concluir.
9. Informar com precisão commit/PR, arquivos, testes executados, resultados, limitações externas e itens que ainda não passaram pela validação.

## Checklist de aceite (não declarar concluído sem evidências)

- [ ] Requisito rastreado na especificação/issue.
- [ ] Backend, API e persistência implementados quando aplicável.
- [ ] Web implementada e conectada à API real.
- [ ] Android implementado e conectado à API real.
- [ ] iOS implementado e conectado à API real.
- [ ] Desktop implementado e conectado à API real.
- [ ] Regras de autorização e isolamento verificadas no servidor.
- [ ] Testes unitários e de integração adicionados e executados.
- [ ] Builds/testes de cada alvo disponível executados; limitações de ambiente explicitadas.
- [ ] Sem dados fictícios usados para simular uma integração real.
- [ ] Documentação e instruções de configuração atualizadas.
- [ ] Evidência de CI/resultado e pendências comunicadas.

**Não marcar um item como concluído só porque existe uma tela, classe, tabela, rota, script ou configuração.** Conclusão significa fluxo implementado, integrado e testado no nível aplicável. Credenciais/provedores externos podem exigir configuração, mas o conector, os estados de falha e as instruções de configuração devem ser implementados e documentados.

## Prioridade de execução

1. Identidade única, papéis/contextos, vínculos, autorização, isolamento por organização e auditoria.
2. Agenda, cancelamento de dia, remarcação, notificações e políticas de confirmação.
3. Instruções, preparação contextual e solicitações clínicas.
4. Demais módulos listados, em incrementos pequenos, com paridade multiplataforma e testes.
5. Distribuição/assinatura de aplicativos só pode ser declarada pronta após build e validação próprios de cada plataforma.
