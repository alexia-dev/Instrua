# Instrua — Inventário de telas Stitch Organic Modern

Origem: pacote enviado em 24/11/2026, `stitch_instrua_design_system_platform.zip`, com **27 páginas HTML, 27 capturas PNG e 1 DESIGN.md**. Este inventário é um contrato de referência VISUAL e não uma confirmação de que cada função existe no backend.

## Regras obrigatórias de integração

1. **Um único Instrua**: aproveitar o app de produção `clients/instrua-app/src/ProductionApp.tsx`, a API Spring Boot, PostgreSQL, o empacotamento Android/Tauri e as permissões existentes. Não publicar um segundo website separado nem trocar telas reais por HTML que simula respostas com JavaScript.
2. Manter a identidade Stitch **Organic Modern**: musgo `#414A35`, musgo profundo `#2B3320`, superfícies creme `#FCF9F0`, `#F6F3EB`, `#F0EEE5`, sálvia `#7E8B6D`, terracota `#C86446`, fontes **Newsreader + Plus Jakarta Sans**, curvas orgânicas assimétricas, foco visível, responsividade e contraste.
3. **Contextos separados por papéis e vínculo real**: pessoal/cliente; profissional clínico (se autorizado); recepção; gestão de estabelecimento; financeiro; administração de plataforma. Troca de contexto só para vínculos reais obtidos da API, e proteção em TODOS os endpoints.
4. **Sem dados simulados em produção**. Substituir personagens “Beatriz”, “Clara”, Clínica Alvorada, métricas e retornos de exemplo por dados reais, estados vazios ou avisos “em desenvolvimento”. Nenhuma notificação, assinatura, cobrança ou prescrição pode ser confirmada sem operação real.
5. **Saúde e LGPD**: prontuários por estabelecimento e profissional autorizado; fichas cadastrais não equivalem a acesso irrestrito a dados sensíveis. Sem alegações `100% LGPD`, `ISO 27001`, `AES-256`, `ICP-Brasil`, biometria, identidade soberana ou autenticação Gov.br sem verificação concreta.
6. **Funcionalidade de verdade antes de anúncio**: interface responsiva + API/banco + autorização + fluxos de erro/vazio + testes + implantação. O protótipo do Stitch isoladamente não comprova nenhuma funcionalidade.
7. **Não oferecer condutas diagnósticas/prescrições médicas geradas por IA como prontas**; implementação clínica requer revisão, trilha de auditoria, profissional habilitado, assinatura eletrônica apropriada, legislação aplicável e consentimento/base legal.
8. **Sem fragmentação**: as quatro telas de prescrição, duas de orientações e duas de social são variações do mesmo módulo, não produtos ou permissões distintos.

## 27 telas, destinos e status de implementação

A marcação abaixo informa o destino correto e o estado geral do código existente; funcionalidades específicas dentro das telas ainda exigem testes.

| # | Tela no pacote (pasta `instrua_...`) | Contexto/rota alvo | Situação |
|---:|---|---|---|
| 01 | landing_page_institucional | Pública: homepage | Base React orgânica já criada; adaptar detalhes |
| 02 | login_e_sele_o_de_perfil | Pública: login + pós-login | Login real e escolha de contexto já têm base; não copiar credenciais do mock |
| 03 | hub_de_cadastro_onboarding_multi_perfil | Onboarding pessoa/profissional/empresa | Cadastro pessoa/empresa parcial; perfis dependem de convites |
| 04 | minha_sa_de_espa_o_do_paciente | Cliente: Início pessoal | Painel orgânico com dados da API criado; check-in e auditoria não são fictícios |
| 05 | ficha_soberana_governan_a_de_dados_lgpd | Cliente: ficha e consentimentos | Base de perfil/compartilhamento parcial; auditoria avançada pendente |
| 06 | meu_in_cio_profissional | Profissional: Início | Adaptar painel aos agendamentos atribuídos |
| 07 | agenda_multiprofissional_recep_o | Recepção/gestão: Agenda | Agenda real com colunas/filtros criada; sala, check-in e fila completos pendentes |
| 08 | central_de_antecipa_o_radar_de_imprevistos | Operação: antecipação e radar | Projeto de integração; sem alertas ou reagendamentos fingidos |
| 09 | gest_o_de_salas_equipamentos_cl_nicos | Gestão: recursos físicos | Design; exige backend de salas, equipamentos, bloqueios e conflitos |
| 10 | central_de_tarefas_triagens_notifica_es_priorit_rias | Gestão/recepção: fila de tarefas | Design; exige tarefas, autorização e estados persistidos |
| 11 | totem_de_autoatendimento_recep_o_acolhedora | Quiosque/recepção: check-in | Design; requires hardware/session-kiosk safeguards |
| 12 | confirma_o_de_check_in_direcionamento_de_sala_totem | Quiosque/recepção: resultado check-in | Design; precisa QR/token verdadeiro e ausência de exposição de pacientes |
| 13 | prontu_rio_atendimento_cl_nico_dra._beatriz | Profissional clínico: Prontuário | Rascunho SOAP seguro parcialmente implementado; não assinar automaticamente |
| 14 | prescri_o_digital_inteligente_icp_brasil_dra._beatriz_1 | Profissional clínico: Prescrição (variação 1) | Protótipo, sem emissor ICP validado |
| 15 | prescri_o_digital_inteligente_icp_brasil_dra._beatriz_2 | Profissional clínico: Prescrição (variação 2) | Protótipo da mesma função |
| 16 | prescri_o_digital_inteligente_icp_brasil_dra._beatriz_3 | Profissional clínico: Prescrição (variação 3) | Protótipo da mesma função |
| 17 | prescri_o_digital_inteligente_icp_brasil_dra._beatriz_4 | Profissional clínico: Prescrição (variação 4) | Protótipo da mesma função |
| 18 | biblioteca_de_f_rmulas_magistrais_protocolos_dra._beatriz | Profissional clínico: Biblioteca de fórmulas | Design; requer revisão clínica e autorização por tipo profissional |
| 19 | emiss_o_de_laudos_pareceres_atestados_icp_brasil_dra._beatriz | Profissional clínico: Documentos | Design; requer geração real, auditoria, assinatura e modelo legal |
| 20 | orienta_es_interativas_protocolos_cl_nicos_1 | Profissional/recepção: Orientações | Protótipo; biblioteca/entrega real a implementar |
| 21 | orienta_es_interativas_protocolos_cl_nicos_2 | Profissional/recepção: Orientações (alternativa) | Protótipo da mesma função |
| 22 | central_de_intelig_ncia_dante_ai_copiloto_cl_nico | Profissional clínico: Dante | Design; sem triagem/transcrição automáticas ativas |
| 23 | instrua_social_cuidado_multidisciplinar_cl_nica_alvorada_1 | Social: feed e comunidade | Social com base API parcial; aplicar design sem divulgar dados clínicos |
| 24 | instrua_social_cuidado_multidisciplinar_cl_nica_alvorada_2 | Social: feed e comunidade (alternativa) | Protótipo da mesma função |
| 25 | relat_rios_repasses_financeiros_dra._beatriz | Financeiro/gestão: relatórios | Painel parcial; apuração, repasse e faturamento fiscal dependem de backend |
| 26 | portal_de_integra_es_interoperabilidade_hl7_fhir_webhooks | TI/admin: integrações | Design; sem declarar HL7/FHIR/webhooks ativos sem testes |
| 27 | central_de_auditoria_lgpd_trilha_imut_vel_de_logs | Administração autorizada: auditoria | Trilhas parciais; “imutável”, exportação e relatórios ainda carecem de validação |

A transcrição dos slugs retém grafia sem acentos tal como no ZIP. Cada item tem `code.html` e `screen.png` correspondentes no arquivo original.

## Ordem incremental de conversão em produção

**Lote A — identidade e jornada real:** landing, login/cadastro/onboarding, seletor de contexto, painéis pessoais e profissionais, navegação, acessibilidade e tema. Testar login e recarga.

**Lote B — operação diária:** agenda multi-profissional, vista por sala, clientes com ficha administrativa reutilizável, atendimento/filas, check-in, tarefas e salas. Persistir no servidor; não inferir elegibilidade.

**Lote C — redução do trabalho:** central de antecipação e lista de espera, radar, orientações, eventos, oportunidades, calendário e notifications. Envio WhatsApp/push somente com provedor conectado e consentimento configurado.

**Lote D — clínico e documentos:** rascunho SOAP, anexos, guias, laudos, atestados, prescrições e assinatura com conformidade. Acesso mínimo e isolamento entre instituições.

**Lote E — comunidade e negócio:** Social, relatórios e repasses, governança, auditoria, integrações, funções de IA reais e opt-in.

## Critério mínimo de aceite por tela

- Aparência consistente em **web desktop, tablet e Android**, mantendo uma única fonte.
- Dados de API reais e estados de carregamento, sem dados falsos.
- Salvar/editar/consultar/atualizar e tratamento de falha validado em teste automatizado onde a tela exija escrita.
- Permissões **no servidor** e verificação negativa para recepção/gestão quando envolver dados clínicos.
- Acessibilidade: teclado, toque, foco, texto 100–150%, contraste e tema.
- Publicação aferida pelo deploy, build APK e smoke tests — commit isolado não é deploy.

*Documento de implementação. O ZIP enviado nesta conversa ainda não é um diretório versionado no GitHub, e não deve ser presumido disponível no runtime de build.*
