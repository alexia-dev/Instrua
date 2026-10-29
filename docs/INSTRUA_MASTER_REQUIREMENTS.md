# INSTRUA — Especificação Mestre de Desenvolvimento Frontend + Backend

**Status:** especificação normativa do produto. Este documento descreve requisitos, não certifica que estejam implementados. A issue [#9 — implementação integral com paridade Web, Android, iOS e Desktop](https://github.com/alexia-dev/Instrua/issues/9) acompanha a execução e deve permanecer aberta enquanto houver lacunas.

## 1. Objetivo e método

Continuar o projeto existente `alexia-dev/Instrua`, preservando arquitetura e código funcional. Antes de alterar, inspecionar frontend, backend, banco, APIs, integrações, segurança e testes. Mapear lacunas, implementar em incrementos pequenos, executar testes e registrar precisamente o que passou e o que permanece pendente. Não substituir integrações reais por demonstrações.

## 2. Visão central

Plataforma internacional e multissetorial para pessoas, profissionais, estabelecimentos, empresas, clínicas, hospitais e prestadores de serviços. Uma única identidade pessoal pode acumular papéis legítimos e atuar em diferentes organizações sem misturar permissões, dados ou históricos.

Papéis previstos: cliente/paciente; profissional de saúde ou de outras áreas; autônomo; proprietário, sócio ou administrador; funcionário/colaborador; professor, estudante ou instrutor; responsável autorizado; outros papéis configuráveis por setor.

## 3. Identidade, perfil e vínculos

### 3.1 Identidade única e múltiplos papéis
- Uma conta pessoal com múltiplos papéis, qualificações e vínculos.
- Alternância de contexto pessoal, profissional, organização e unidade sem sair da conta.
- Verificação das credenciais necessárias para atividades regulamentadas.
- Permissões por papel, organização, unidade e módulo.

### 3.2 Perfil pessoal mantido pelo usuário
- Dados pessoais, email, telefone, endereço, documentos quando necessários, dados fiscais, preferências de comunicação/acessibilidade, dados pertinentes ao serviço, pendências e histórico de atualização.
- Organizações podem solicitar informações complementares, mas não alterar silenciosamente o perfil pessoal.
- Minimização de dados conforme finalidade.

### 3.3 Convites profissionais
- Criar convite com função, serviços, unidade, horários, condições e permissões propostas.
- Notificar convidado; permitir visualizar, aceitar, recusar ou pedir alterações.
- Verificar requisitos antes de ativar vínculo.
- Manter versões, decisões, alterações, encerramento e revogação.
- Expor a cada parte somente os dados necessários.

### 3.4 Estabelecimentos
Distinguir pessoa, estabelecimento, organização empresarial, unidade/filial, vínculo profissional, convite, papel/permissão e qualificação. Permitir cadastrar estabelecimento próprio, configurar serviços e convidar colaboradores.

## 4. Prontuário, privacidade e segurança
- Isolamento rigoroso de prontuários, documentos, anexos e observações por organização e contexto.
- Acesso somente por pessoas autorizadas e limitado ao necessário.
- Autorização validada no backend em toda operação; não confiar apenas na interface.
- Auditoria de acesso e alterações com autor, data/hora e valores anteriores/posteriores quando aplicável.
- Exportação, retenção, correção e acesso do titular conforme a lei.
- Proteção de credenciais, dados sensíveis e arquivos; testes automatizados de isolamento entre organizações.
- Respeitar direitos do titular e obrigações legais de guarda.

## 5. Agenda e remarcação
- Visões diária, semanal e mensal; filtros por profissional, unidade, serviço, sala e status.
- Criar, editar, cancelar e reagendar; disponibilidade, intervalos, recorrências, encaixes, lista de espera, conflitos, duração e regras por serviço.
- Cancelamento do dia pelo profissional: identificar atendimentos e pessoas afetadas; detectar alternativas válidas; notificar imediatamente pelos canais autorizados; permitir escolha de nova data e contato humano; registrar mensagens, decisões, mudanças e histórico original.
- Não transferir automaticamente para outro profissional sem verificar disponibilidade, qualificações e autorização.
- Intervalo mínimo entre atendimentos configurável por serviço, profissional e estabelecimento, com exceções autorizadas e validação server-side.
- Confirmação imediata, lembretes configuráveis, prazo de confirmação, política de liberação, lista de espera, prevenção de mensagens duplicadas e logs de entrega/falha.

## 6. Preparação do profissional
Reunir, para cada atendimento, somente identificação necessária, informações fornecidas para o atendimento, histórico pertinente da organização atual, instruções/documentos, pendências, alertas autorizados, formulário/prontuário apropriado e tarefas posteriores. IA não mistura dados de organizações.

## 7. Instruções interativas
Texto formatado, imagens, vídeos, PDFs/documentos, checklists, contato com estabelecimento, perguntas de preparo, orientações antes/durante/depois, modelos, personalização por cliente/serviço/setor, idiomas, aprovação profissional, confirmação de leitura/compreensão e logs de entrega/dúvidas/falhas. A IA pode rascunhar, mas orientação clínica exige os controles profissionais aplicáveis.

## 8. Mapa e localização
Endereço cadastrado, mapa, rota por serviço compatível, chegada, acessibilidade/entrada, contatos, horários e unidade correta. Nunca inventar endereços ou coordenadas.

## 9. Agenda de cuidados do paciente
Reunir agendamentos autorizados, instruções, lembretes, recomendações profissionais, cuidados prescritos/recomendados pelo responsável, preparo de exames, retornos, documentos compartilhados e solicitações pendentes. Diferenciar recomendações clínicas, conteúdo educativo e publicidade.

## 10. Solicitações clínicas
Solicitar atestados, receitas, relatórios, cópias, esclarecimentos e outros itens habilitados. Registrar protocolo, encaminhar ao profissional autorizado, notificar, permitir análise/aprovação/recusa/pedido de informação, solicitar consulta adicional quando necessário e registrar decisão/entrega. Não emitir automaticamente documentos dependentes de decisão profissional.

## 11. Prontuários configuráveis e saúde
Modelos de prontuário, anamnese, evolução, formulários por especialidade, exames/resultados, documentos, planos de cuidado, fluxos, permissões e auditoria. Planejar expansão hospitalar (admissão/alta, leitos/transferências, enfermagem, procedimentos, exames/resultados, filas/capacidade e integrações). Avaliar HL7 FHIR/interoperabilidade. Não declarar capacidades hospitalares prontas sem implementação e validação próprias.

## 12. Radar de Imprevistos
Detectar riscos operacionais, atrasos/conflitos e indisponibilidade de profissionais/recursos; sugerir alternativas, reorganização, preenchimento por lista de espera e simulação antes de mudanças; comparar cenários, registrar decisões, medir resultados comprovados e permitir regras/limites. Explicar origem, limitações e impacto estimado; não alegar problemas evitados sem evidência.

## 13. Dante AI
Assistência contextual para agendas/tarefas, rascunhos de instruções, resumos autorizados, pendências, configuração de serviços, explicação de alertas, relatórios, adaptação de linguagem/idioma, ações autorizadas e encaminhamento humano. Personalização transparente e controlável; isolamento entre organizações; confirmação/aprovação para ações sensíveis.

## 14. Rede Social
Publicações com fotos/texto, comentários/respostas, reações/votações, eventos, comunidades, perfis configuráveis, busca/descoberta, denúncias/moderação, privacidade, notificações e medidas anti-spam/abuso. Não expor automaticamente informações de saúde, agendamentos ou vínculos privados.

## 15. Sugestões e votação
Enviar, consultar, pesquisar/filtrar, votar e comentar sugestões. Estados: recebida, em análise, planejada, em desenvolvimento, em testes, implementada ou recusada. Mostrar atualizações, notas de versão e instruções de uso; moderar abuso e não publicar dados pessoais/confidenciais.

## 16. Financeiro e fiscal
Contas a pagar/receber, pagamentos/conciliação, comissões/repasses, faturamento, relatórios, dados fiscais, preparação/emissão de documentos e integrações por jurisdição. Validar prestador/tomador/serviço/valores/parâmetros e só marcar emissão após confirmação confiável.

## 17. Instrua Protege
Escalas, entradas/saídas, intervalos, divergências, justificativas, ajustes, aprovações, relatórios e auditoria. Transparência, conformidade legal e proibição de vigilância oculta.

## 18. Comunicação
Email, push, WhatsApp Business Platform, SMS e outros provedores configurados. Validar destinatários, respeitar preferências/regras de canal, controlar modelos/limites, registrar resultados, detectar falhas, evitar duplicações, permitir handoff humano e distinguir operação de marketing.

## 19. Privacidade, segurança e conformidade
Controle de acesso por papéis/permissões; isolamento entre organizações; proteção de dados sensíveis; auditoria; consentimentos quando aplicáveis; direitos de exportação/correção/exclusão conforme lei; retenção; incidentes; segredos; arquivos; backups e testes de restauração; antiabuso; LGPD e normas profissionais/jurisdicionais.

## 20. Arquitetura
Preservar, quando possível, Java 21 + Spring Boot, React + TypeScript, PostgreSQL, REST, migrações versionadas, Docker/infraestrutura existente, integrações desacopladas e serviço de IA separado/protegido. Inspecionar/reutilizar módulos existentes de users, companies, employees, patients, services, instructions, notifications, integrations, reports, audit e tenant; evitar duplicações.

## 21. Execução obrigatória
1. Inspecionar frontend/backend e inventariar o que existe, está incompleto ou falta.
2. Mapear requisitos a arquivos, módulos, telas, APIs, tabelas, integrações e testes.
3. Implementar em etapas pequenas, com migrações quando necessárias.
4. Conectar frontend às APIs reais.
5. Adicionar testes unitários, integração, autorização e isolamento multi-tenant.
6. Executar builds/testes disponíveis e corrigir falhas.
7. Atualizar documentação/configuração.
8. Reportar commits, arquivos, testes executados, resultados, limitações e pendências.
9. Não marcar função como concluída sem testes adequados; não usar dados fictícios para simular integração real.

## 22. Primeira prioridade
Começar pela fundação de identidade única, papéis, vínculos profissionais, isolamento por organização e auditoria. Em seguida, agenda, remarcação, notificações, instruções e solicitações clínicas. Produzir inventário de lacunas, plano priorizado, arquivos a alterar e riscos técnicos/de segurança.

## Critério permanente de paridade
Cada requisito solicitado deve ser entregue no backend/persistência quando aplicável e nos clientes Web, Android, iOS e Desktop, adaptado à tecnologia e UX de cada plataforma. Registrar exceções técnicas reais e alternativas; nunca omitir uma plataforma silenciosamente. Um mockup, classe, tabela ou endpoint isolado não comprova conclusão.


## 23. Padrão único visual, de produto e de backend — obrigatório em todas as branches

Esta seção é normativa para todas as branches e todas as versões do Instrua. Não pode haver uma identidade ou regra de negócio paralela apenas para a demonstração.

### 23.1 Fonte única de verdade
- Este repositório mantém um único produto Instrua. A branch de demonstração é uma apresentação do mesmo produto, não um produto independente.
- Antes de implementar uma feature, comparar a branch com a especificação mestre, identificar divergências e preservar alterações específicas que ainda sejam válidas.
- Não copiar cegamente código entre branches nem sobrescrever trabalho existente. Aplicar a regra e adaptar a implementação à arquitetura real daquela branch.
- Requisitos e contratos compartilhados devem ser versionados no repositório; divergências intencionais precisam ser documentadas e aprovadas.

### 23.2 Sistema visual compartilhado
- Manter marca, símbolo circular com check, escrita “instrua”, slogan “Orientação, conexão e confiança.” e a mesma linguagem editorial em todas as superfícies.
- Tokens de referência: moss #414A35, olive #69704B, sage #96977B, mist #C2C3AA, sand #D6C9AD, beige #B5A17F, clay #A66F4B, amber #C18A43, off-white #F5F1E8, card #FCFAF4, text #34382D, secondary #686858, border #DDD7C8.
- Centralizar tokens e componentes equivalentes para cores, tipografia, espaçamento, bordas, botões, campos, cartões, navegação, estados de carregamento/erro/vazio, diálogos, notificações e acessibilidade.
- Responsividade e padrões de interação devem ser adaptados à Web, Android, iOS e Desktop; não precisam ser pixel a pixel idênticos, mas devem manter a mesma marca, hierarquia, linguagem e comportamento funcional.
- A tela de carregamento de login deve usar a mesma marca e padrão de animação em todas as plataformas. O estado de carregamento deve refletir uma operação real quando houver autenticação real; demonstrações devem ser identificadas como demonstração.
- Não inventar telas de sucesso, dados ou estados que não correspondam ao backend real.

### 23.3 Contrato único do backend
- Backend/API é a fonte de verdade para identidade, papéis, permissões, organizações, unidades, agendamentos, status, validações, auditoria e dados compartilhados.
- Todos os clientes usam contratos de API versionados e autenticados; não duplicar regras críticas no cliente nem guardar segredos em aplicativos.
- Padronizar nomes, formatos de datas/horas com timezone, paginação, filtros, erros, validação, idempotência, logs de correlação e versões de API.
- Regras de autorização e isolamento multi-tenant são verificadas no servidor em toda operação. Uma demonstração pode usar fixtures locais explicitamente marcadas, mas nunca apresentá-las como dados sincronizados ou operações reais.
- Migrações de banco devem ser versionadas e compatíveis com dados existentes. Integrações externas devem ter configuração, estados de falha, observabilidade e instruções documentadas.
- Clientes Web, Android, iOS e Desktop devem consumir o mesmo backend de produção quando configurados para produção; ambientes demo/staging precisam ser claramente separados e identificados, não forks de regras de negócio.

### 23.4 Critérios de aceite por alteração
- [ ] Identificar todas as branches existentes afetadas e comparar os arquivos relacionados antes de editar.
- [ ] Aplicar o mesmo requisito e os mesmos tokens/contratos a todas as branches aplicáveis, preservando funcionalidades locais.
- [ ] Web, Android, iOS e Desktop cobertos, ou limitação técnica documentada com alternativa.
- [ ] Backend/API/persistência e segurança atualizados quando aplicável.
- [ ] Testes e builds executados por alvo; relatar claramente o que passou e o que não foi possível executar.
- [ ] A branch demo/cloud segue a mesma especificação visual e funcional; somente dados e serviços de demonstração podem variar, claramente identificados.
- [ ] Não declarar deploy nem paridade completa sem evidência.

A existência deste documento padroniza os requisitos, mas não prova que todo o código de cada branch já esteja alinhado. A paridade real exige revisão e implementação do código, seguida de testes.
