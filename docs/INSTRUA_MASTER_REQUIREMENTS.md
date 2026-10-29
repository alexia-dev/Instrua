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


## 23. Padrão único visual e de backend

Esta branch deve seguir a especificação mestre e o documento de paridade: identidade visual única (logo circular com check, slogan “Orientação, conexão e confiança.”, paleta oficial do Instrua), componentes/tokens compartilhados, responsividade e acessibilidade adaptadas a Web, Android, iOS e Desktop. O backend/API é a fonte única de verdade para autenticação, papéis, permissões, regras de negócio, dados e auditoria. Todos os clientes usam contratos versionados e autenticação comum; regras críticas não ficam só no frontend, segredos não ficam nos clientes, e isolamento multi-tenant é validado no servidor. A demo/cloud pode variar apenas em dados/serviços explicitamente demonstrativos, nunca em marca ou regras centrais. Toda alteração deve ser comparada com as outras branches relevantes, preservar código válido, incluir testes/builds por alvo e documentar limitações. A documentação não prova que o código já está alinhado; é necessário implementar e testar.
