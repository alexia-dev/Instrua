# Instrua — escopo de produto e hipótese comercial

## 1. Tese do produto

O Instrua deve ser construído primeiro para saúde. A oportunidade central não é “mais um app para estabelecimentos”, mas resolver problemas da jornada do paciente e da operação assistencial: agendamento, confirmação, preparo para exames, lembretes, comunicação, agenda, registros autorizados, coordenação entre equipes e continuidade do atendimento.

O produto deve atender:
1. Pacientes/clientes e, quando autorizado, responsáveis/cuidadores.
2. Profissionais de saúde de diversas disciplinas e especialidades.
3. Equipes assistenciais: enfermagem, técnicos e auxiliares, fisioterapia, psicologia, nutrição, farmácia, laboratório, imagem, reabilitação, odontologia, equipes multiprofissionais e outros papéis.
4. Equipes administrativas: recepção, atendimento, call center, autorização, faturamento, financeiro, auditoria, gestão de leitos/fluxo quando aplicável, RH, TI, qualidade, coordenação e direção.
5. Consultórios, clínicas, centros de diagnóstico, laboratórios, unidades de atendimento, hospitais e redes.
6. Outros estabelecimentos, como mercado de expansão — sem fazer deles o centro da proposta.

“Todos os cargos possíveis” deve ser suportado por um catálogo de funções configurável, não por uma lista fechada de nomes. A autorização precisa considerar papel, unidade, equipe, vínculo, contexto de atendimento e necessidade de acesso. Um cargo não concede automaticamente acesso a todos os prontuários.

## 2. Princípios de experiência e segurança

- A jornada do paciente deve ser clara, acessível e gratuita no uso básico.
- Não vender prioridade clínica nem colocar instruções críticas, confirmação básica ou acesso aos próprios dados atrás de paywall.
- Separar identidade pública, conta de acesso, vínculo profissional e permissões.
- Prontuário e registros clínicos exigem controle de acesso robusto, auditoria, segurança, política de retenção e revisão jurídica antes de produção.
- Conteúdos de preparo devem ter autoria, versão, serviço relacionado e validação por responsável habilitado.
- Notificações por WhatsApp/SMS/e-mail precisam de consentimento/preferências, política de privacidade e custos transparentes.
- A IA deve ser assistiva, limitada e supervisionada; não se apresentar como diagnóstico nem substituir decisão clínica.
- A demo atual não é prontuário de produção, sistema hospitalar integrado, sistema oficial de ponto ou emissor fiscal.

## 3. Arquitetura de produto por módulos

### Núcleo gratuito do paciente
- Cadastro e gestão de preferências de contato.
- Busca de profissionais, especialidades, serviços e locais.
- Solicitação, confirmação, cancelamento/remarcação segundo política do prestador.
- Lembretes essenciais e instruções de preparo.
- Histórico pessoal de agendamentos, documentos e orientações disponibilizados.
- Acessibilidade básica, privacidade e exercício dos direitos do titular.

### Profissional individual
- Perfil profissional, agenda e disponibilidade.
- Gestão de consultas/atendimentos e instruções.
- Comunicação e lembretes com limites transparentes.
- Indicadores básicos e lista de espera.
- Recursos pagos opcionais para produtividade, múltiplas agendas e relatórios avançados.

### Equipe assistencial e administrativa
- Visão de tarefas e agenda conforme função.
- Orientações e protocolos aprovados.
- Registros autorizados com autoria e trilha de auditoria.
- Escalonamento, encaminhamentos e comunicação interna, se aprovados no escopo.
- Administração de acessos e separação de funções.
- Usuários vinculados a instituições; evitar cobrar individualmente cada funcionário como padrão.

### Clínica, hospital e rede
- Multiunidade, múltiplas especialidades, agendas e recursos.
- Recepção, call center e confirmação de atendimento.
- Exames e procedimentos, orientações e preparo.
- Permissões por papel, unidade, equipe e contexto.
- Indicadores operacionais, auditoria e integrações.
- Onboarding, migração, suporte e SLA por contrato.

### Expansão para outros setores
- Reutilizar agenda, serviços, equipe, comunicação, pagamentos e operações.
- Modelos de dados e interface específicos por setor quando necessários.
- Não forçar terminologia de saúde a estabelecimentos não clínicos, nem reduzir fluxos de saúde a um app genérico de reservas.

## 4. Hipótese inicial de preços (Brasil)

Valores de teste, não preço final nem cotação de concorrentes. Validar com entrevistas, custo de infraestrutura, suporte, mensageria, integrações, impostos e disposição a pagar.

| Segmento | Hipótese | O que inclui |
|---|---:|---|
| Paciente | R$ 0 | Busca, agendamento básico, confirmação, lembretes essenciais, orientações e acesso aos próprios dados |
| Profissional individual — gratuito | R$ 0 | Perfil, agenda básica, presença na busca e limites iniciais de comunicação |
| Profissional Pro | R$ 39–79/mês | Agenda avançada, múltiplas unidades, instruções reutilizáveis, lista de espera e indicadores |
| Consultório/clínica pequena | R$ 149–299/mês | Agenda compartilhada, recepção, equipe/permissões, serviços, orientações e relatórios básicos |
| Clínica multidisciplinar/centro de exames | R$ 399–999/mês | Mais agendas/unidades, fluxos por equipe, lista de espera, auditoria e relatórios avançados |
| Hospital/rede | Sob consulta | Escala, integrações, implantação, migração, governança, SLA e suporte contratual |
| Módulos extras | Testar por pacote | Protege, Financeiro/Fiscal, automações, relatórios avançados, IA assistiva e integrações |

### Princípios de monetização
- Paciente: não cobrar por agendamento, confirmação, instrução essencial ou acesso aos próprios dados.
- Profissional: freemium; cobrar por produtividade e recursos avançados, não por uso clínico básico necessário.
- Instituição: assinatura por unidade e faixa de capacidade/complexidade é mais previsível do que cobrar cada clique ou cada paciente.
- Funcionários: incluídos no contrato institucional, com permissões por função; não cobrar separadamente de cada membro quando o estabelecimento é o contratante.
- Mensagens: incluir cotas e apresentar excedentes com transparência, sem esconder margens e sem interromper lembretes essenciais sem aviso.
- Implantação/migração/integradores: cobrar como projeto com escopo, aceite e responsabilidades definidos.
- Extras pagos não podem comprometer segurança, privacidade, acessibilidade, direitos dos titulares nem cuidado seguro.

## 5. Extras com potencial de cobrança

1. **Agenda Pro e lista de espera** — encaixes, preenchimento de horários vagos, múltiplas agendas e indicadores de ocupação.
2. **Automação de comunicação** — confirmações, lembretes e campanhas operacionais com consentimento, templates e custos por canal transparentes.
3. **Biblioteca avançada de orientações** — modelos por serviço, versões, revisão/aprovação e distribuição segmentada.
4. **Relatórios avançados** — faltas, confirmação, capacidade, origem de agendamento e tendências agregadas.
5. **Instrua Financeiro** — recebimentos, despesas, repasses e conciliação, após validação de escopo e segurança.
6. **Instrua Fiscal** — integração fiscal real apenas após requisitos legais, homologação, suporte e testes.
7. **Instrua Protege** — jornada e divergências operacionais; registros oficiais de ponto requerem análise jurídica e validação técnica.
8. **Dante/IA assistiva** — sumarização administrativa, ajuda para navegação e rascunhos supervisionados; sem diagnóstico automático nem decisão clínica autônoma.
9. **Integrações e API** — sistemas de clínica/hospital, identidade, comunicação e interoperabilidade; escopo por contrato.
10. **Implantação, migração e treinamento** — serviço profissional para organizações com legado.
11. **Governança Enterprise** — SSO, gestão avançada de acessos, logs, retenção, SLA e suporte prioritário quando efetivamente implementados.

## 6. Não cobrar ou não colocar em plano premium

- Acesso a orientações de preparo necessárias.
- Confirmação, cancelamento e lembretes essenciais.
- Acessibilidade básica.
- Acesso aos próprios dados e ferramentas de privacidade.
- Correção de informações e exercício de direitos previstos em lei.
- Segurança, autorização e trilha de auditoria essenciais.
- Acesso de funcionários necessários ao trabalho dentro das permissões contratadas.
- Prioridade em filas clínicas como benefício comprado dentro do app.

## 7. Pesquisa de preço antes de decidir

Entrevistar ao menos pacientes/responsáveis, profissionais individuais, recepção, enfermagem, faturamento, gestão de clínicas, TI/segurança e hospitais. Testar propostas por segmento, custos variáveis e orçamento disponível. Medir ativação, tempo poupado, taxa de confirmação, faltas, ocupação, retenção, suporte e custo de mensagens. Definir preços finais apenas após piloto.

## 8. Etapas de desenvolvimento

1. Jornada confiável do paciente: busca, agendamento, confirmação, instruções e notificações.
2. Perfis profissionais, agenda, disponibilidade e permissões.
3. Fluxos reais de recepção e equipe multidisciplinar.
4. Instituições multiunidade, auditoria e integrações prioritárias.
5. Prontuário/registros clínicos somente com desenho de segurança, governança e requisitos legais adequados.
6. Módulos comerciais avançados e expansão para outros setores.
7. IA assistiva e análises após validação de qualidade, segurança e supervisão humana.

## 9. Estado do protótipo

A branch `feat/instrua-social-moderation` contém uma demo frontend local. Os dados são fictícios e persistem no navegador. Não existe backend de produção, autenticação real, prontuário clínico validado, integração hospitalar, cobrança real ou emissão fiscal. Os preços são apenas hipóteses de produto.
