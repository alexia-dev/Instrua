# Instrua — Arquitetura de hospedagem segura e plano de implantação

**Status:** proposta; nenhuma infraestrutura de produção foi criada ou validada.  
**Objetivo:** preparar uma plataforma SaaS para clínicas com dados pessoais e potencialmente dados pessoais sensíveis de saúde, começando com orçamento de R$ 0 e crescendo sem reescrever a arquitetura.

## Decisão executiva

- **Agora, com R$ 0:** desenvolver e testar somente com dados fictícios. Não publicar para uso clínico, não coletar dados reais de pacientes e não prometer disponibilidade de produção. Usar GitHub Actions para builds e validações; um preview público da interface, se ativado, é apenas demonstração.
- **Produção futura recomendada:** AWS na região São Paulo (sa-east-1), com frontend estático via CloudFront/S3 ou serviço equivalente, WAF conforme risco, API Java/Spring Boot em ECS Fargate, PostgreSQL gerenciado no RDS em sub-redes privadas, gestão de segredos, logs, monitoramento, backups e recuperação testada.
- **Alternativa de operação simplificada para piloto pago:** considerar um provedor gerenciado com região brasileira e controles contratuais adequados; comparar custos, residência/localização de dados, backups, controles de acesso, suporte a incidentes e contrato de tratamento antes de decidir.
- **Não usar** armazenamento do navegador/localStorage como fonte oficial de dados de clínica. Não usar banco gratuito temporário para dados reais.

## Diagrama lógico

Navegador / app
→ HTTPS / DNS / CDN / proteção web
→ API pública (Spring Boot, autenticação e autorização)
→ rede privada
→ PostgreSQL gerenciado privado

Serviços auxiliares: gestão de segredos, chaves de criptografia, logs de auditoria, alertas, backups, restauração e monitoramento de custos.

## Componentes alvo em AWS

1. **Região:** São Paulo (sa-east-1), após avaliar disponibilidade e custo de cada serviço.
2. **Frontend:** build web React publicado como conteúdo estático; CloudFront como CDN. S3 privado com acesso por origem controlada, não bucket público aberto.
3. **API:** container Java 21/Spring Boot em ECS Fargate, atrás de Application Load Balancer; escalar horizontalmente conforme métricas. Evitar credenciais embutidas na imagem.
4. **Banco:** Amazon RDS for PostgreSQL, sem acesso público, em sub-redes privadas, TLS, criptografia em repouso, backups automáticos e recuperação pontual conforme plano contratado. Multi-AZ quando o orçamento e o objetivo de disponibilidade justificarem.
5. **Segredos:** AWS Secrets Manager ou Parameter Store; IAM com privilégio mínimo; nunca guardar tokens, senhas ou chaves no GitHub, frontend ou logs.
6. **Proteção e observabilidade:** HTTPS, WAF conforme perfil de risco, CloudWatch, alertas de erro/capacidade/custo, trilha de auditoria com acesso restrito.
7. **Arquivos:** se necessário, S3 privado com criptografia, políticas restritas, links temporários e regras de retenção; não usar links públicos para documentos clínicos.
8. **Entrega:** GitHub Actions para testes/build e implantação controlada; separar desenvolvimento, homologação e produção; aprovação explícita antes de produção.

## Segurança e LGPD — bloqueadores de produção

- Definir papéis de controlador e operador por contrato, finalidades, bases legais, retenção, canal para direitos dos titulares e processo de incidentes.
- Mapear dados, fluxo, fornecedores/suboperadores, transferências internacionais e contratos aplicáveis.
- Aplicar minimização de dados; coletar apenas o necessário para a finalidade.
- Autenticação robusta, recuperação de conta segura, MFA para administradores e sessões revogáveis.
- Autorização verificada no backend em toda operação; isolamento por organização/estabelecimento testado no servidor, não apenas na interface.
- Registrar acessos e alterações relevantes sem registrar senhas, tokens ou conteúdo clínico desnecessário.
- Criptografia em trânsito e em repouso; gestão e rotação de chaves/segredos.
- Testes de autorização, validação de entrada, dependências vulneráveis, backups e restauração.
- Plano de resposta a incidentes, contatos, responsabilidades e exercícios.
- Revisão de privacidade/segurança e, quando aplicável, avaliação da necessidade de Relatório de Impacto à Proteção de Dados Pessoais.
- Contratos, aviso de privacidade, termos, política de retenção e processo de atendimento a solicitações antes do uso real.

**Hospedagem em nuvem não torna o sistema automaticamente conforme à LGPD.** A configuração, o código, os processos e os contratos também precisam estar adequados.

## Limitações do projeto atual

O repositório contém uma interface React e uma base inicial de API Spring Boot/PostgreSQL, mas a documentação do projeto informa que nem todos os módulos estão implementados ou conectados. A agenda da interface ainda usa armazenamento local. Portanto, não declarar login, persistência central, isolamento entre clínicas, backups ou prontidão clínica como concluídos até implementação e testes verificáveis.

## Fases de entrega

### Fase 0 — custo R$ 0
- Usar apenas dados fictícios.
- Manter o repositório privado se houver material confidencial; nunca incluir dados reais, segredos ou exports de banco.
- Usar GitHub Actions para validar build/testes.
- Publicar preview da interface apenas se necessário, com aviso de demonstração e sem cadastro de pacientes.
- Configurar limites/alertas de gastos em qualquer conta cloud antes de criar recursos faturáveis.

### Fase 1 — base segura
- Implementar autenticação e gestão de sessões.
- Implementar autorização no backend e isolamento multi-tenant com testes.
- Integrar agenda com API e PostgreSQL.
- Implementar auditoria, gestão de segredos, tratamento de erros e política de logs.
- Automatizar testes e build via GitHub Actions.

### Fase 2 — homologação
- Provisionar infraestrutura como código revisada (por exemplo, Terraform/CDK) e deploy de homologação.
- Validar TLS, banco privado, backups e restauração, alertas, limites de custos e procedimento de rollback.
- Fazer revisão independente de segurança e privacidade.

### Fase 3 — produção
- Assinar contratos e concluir documentação LGPD.
- Implantar infraestrutura redundante de acordo com RTO/RPO acordados.
- Fazer teste de carga, vulnerabilidade, autorização entre clínicas e recuperação.
- Migrar somente dados necessários, com plano de retorno e aprovação formal.

## Custos

Com orçamento disponível de **R$ 0**, a meta é desenvolvimento e demonstração, não operação clínica. Não existe promessa responsável de hospedagem de produção segura, escalável, com backups e alta disponibilidade por R$ 0.

A AWS cobra pelos recursos usados; ECS/Fargate, banco RDS, armazenamento, tráfego, logs, WAF e backups podem gerar cobranças separadas. Região São Paulo pode ser mais cara que regiões dos EUA. Consultar o AWS Pricing Calculator antes de criar recursos e ativar alertas de faturamento. Não usar valores fixos sem dimensionamento e cotação atual.

## Procedimento pelo navegador

1. Abra o repositório do Instrua no GitHub e confira a branch de trabalho.
2. Acompanhe os builds em **Actions**; corrija falhas antes de publicar.
3. Para preview da interface, conecte o repositório a um serviço de hospedagem de frontend e configure a raiz como `clients/instrua-app`, comando de build `npm run build` e diretório publicado `dist`. Confirme que o serviço suporta monorepo e configure variáveis somente quando necessárias. Isso publica a interface, não a API nem o banco.
4. Para API e banco, não basta conectar o GitHub a um serviço: é preciso configurar runtime Java 21, construir o JAR, definir variáveis de ambiente/segredos, provisionar PostgreSQL e configurar a rede. Faça isso primeiro em homologação, seguindo a documentação oficial do provedor.
5. Antes de ativar qualquer recurso pago, revisar a estimativa, limites, alertas e forma de pagamento.
6. Confirmar a publicação abrindo o endereço público e testando os fluxos sem dados reais. Só declarar concluído o que foi efetivamente testado.

## Referências oficiais

- AWS regiões: https://docs.aws.amazon.com/ec2/latest/instancetypes/ec2-instance-regions.html
- Amazon ECS pricing: https://aws.amazon.com/ecs/pricing/
- Amazon RDS PostgreSQL: https://docs.aws.amazon.com/pt_br/AmazonRDS/latest/UserGuide/CHAP_PostgreSQL.html
- Segredos no ECS: https://docs.aws.amazon.com/pt_br/AmazonECS/latest/developerguide/specifying-sensitive-data.html
- ANPD — guia de segurança da informação: https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes/guia-vf.pdf
- ANPD — RIPD: https://www.gov.br/anpd/pt-br/canais_atendimento/agente-de-tratamento/relatorio-de-impacto-a-protecao-de-dados-pessoais-ripd
