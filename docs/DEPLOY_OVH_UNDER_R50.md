# Infraestrutura comercial escolhida — teto de R$ 50/mês

Decisão em 23/11/2026: **OVHcloud VPS-1 2027, contrato mensal sem fidelidade**, sistema Linux x86_64, 2 vCPU, 4 GB RAM, 40 GB NVMe. O servidor único já suporta a stack `docker/compose.production.yaml`. A escolha fica condicionada ao checkout efetivo, tributação e disponibilidade para conta brasileira; **não contratar plano anual antecipado**.

Referências (consultar antes de pagar):
- https://www.ovhcloud.com/en/vps/
- https://www.ovhcloud.com/en/vps/configurator/
- https://registro.br/dominio
- https://developers.cloudflare.com/r2/pricing/

## Orçamento

- OVHcloud VPS-1 (mensal, sem fidelidade): referência observada **US$ 5,35/mês** na loja americana ou **€ 4,49/mês** no catálogo europeu, antes de tributos. Preço final depende do país de cadastro e checkout.
- Equivalência aproximada (nov/2026): R$ 30–36, sem garantir câmbio e taxas do cartão; reservar até R$ 40 da verba.
- DNS e TLS com Cloudflare DNS Free + Caddy: sem mensalidade adicional.
- Backups de VM diários: incluídos no VPS; **não** substituem backup consistente do PostgreSQL fora do provedor.
- Cloudflare R2 Standard: primeiros 10 GB-mês e operações dentro da franquia gratuitos; ultrapassagens são cobradas.
- Domínio `.com.br`: R$ 40 por **ano, à vista** no Registro.br (R$ 3,33/mês em média), apenas se não houver domínio já registrado. Não confundir com cobrança mensal nem incluir no primeiro pagamento sem orçamento.

**Regra:** só contratar se preço mensal final exibido com tributos/IOF puder caber no teto de R$ 50 com margem. Não abrir contas/ativar cobrança via ferramentas sem aprovação explícita da proprietária.

## Arquitetura

1 VPS OVHcloud executa **Caddy (HTTPS)** + **React (nginx)** + **Spring Boot Java 21** + **PostgreSQL 16** em rede Docker privada. O único acesso externo deve ser SSH limitado, HTTP 80 (redirecionado) e HTTPS 443. PostgreSQL 5432 e Java 8080 **nunca** devem ser publicados na Internet.

- Website e API: `https://DOMINIO` e `https://DOMINIO/api/v1`
- Tauri/Android: compilar com `VITE_INSTRUA_API_URL=https://DOMINIO`; sem isso o aplicativo não se conectará de um aparelho real.
- DNS: apontar domínio a IPv4 do VPS; Cloudflare é opcional como DNS/segurança.
- Domínio da empresa só deve ser utilizado depois de verificar propriedade, registros DNS e delegação.

## Passos para ativar

1. Proprietária cria/verifica conta OVHcloud, escolhe **VPS-1 2027**, **1 mês / sem fidelidade**, Ubuntu 24.04 LTS x64, região disponível adequada, confirma preço final e pagamento. **Nunca** selecionar promoção de 12 ou 24 meses apenas por valor mensal anunciado.
2. Configurar SSH por chave, usuário sem root para administração, firewall/updates. Expor 80,443; SSH restrito por origem sempre que possível. Bloquear 5432 e 8080 externos.
3. Instalar Docker Engine + Compose plugin no VPS. Configurar `ufw` e atualizações de segurança, limitar JVM para a RAM disponível e monitorar `docker stats`.
4. Criar/apontar domínio: registro A para IP do VPS e DNS resolvendo corretamente antes de iniciar Caddy. Confirmar `https://DOMINIO` e certificado TLS.
5. Clonar repositório e usar **somente uma revisão validada** do PR #17, após CI e revisões; nunca implantar a branch demonstrativa como produto.
6. Copiar `docker/.env.production.example` para `docker/.env` (ignorado pelo Git). Definir `INSTRUA_DOMAIN`, `ACME_EMAIL`, `POSTGRES_PASSWORD` forte e `JWT_SECRET` aleatório de no mínimo 48 caracteres. Não compartilhar segredos em chat nem GitHub.
7. Executar:

```sh
cd docker
docker compose --env-file .env -f compose.production.yaml config
docker compose --env-file .env -f compose.production.yaml up -d --build
docker compose --env-file .env -f compose.production.yaml ps
```

8. Verificar `https://DOMINIO/api/v1/health`, cadastro de empresa, login, cadastro de serviço/profissional/cliente, agendamento real, bloqueio de duplicidade, login de paciente e separação entre duas empresas.
9. Ativar backup **consistente** via `pg_dump` e retenção versionada, criptografado fora da VPS (R2 Standard após provisionamento, ou outra conta separada). Fazer **teste real de restauração** antes de inserir clientes reais. Backups do VPS do mesmo datacenter não bastam isoladamente.
10. Testar como usuário real em celular e PC. Gerar novo APK/AAB com a URL final de API, assinar e verificar comunicação HTTPS. Configurar checkout/PIX ou faturamento manual com controle de plano e acesso, **antes** de vender assinatura.

## Checklist de aceite antes de faturar

- [ ] VPS contratada, DNS e HTTPS no ar
- [ ] Logs, monitoramento e backup externo + restauração testados
- [ ] Sessão, senha, permissões, prevenção de abuso e isolamento de estabelecimentos auditados
- [ ] Horários de funcionamento/indisponibilidade e conflitos de agenda testados
- [ ] Cadastro, agendamento, cancelamento, orientação e notificação realmente funcionais
- [ ] App Android conectado à mesma API e testado em aparelho físico
- [ ] Cobrança e habilitação/bloqueio dos planos implementados ou processo manual seguro definido
- [ ] Dados sensíveis/saúde somente após revisão de proteção e autorização adequada
- [ ] Termos, privacidade e direitos do titular revisados antes de processar dados reais

**Importante:** não existe implantação em OVHcloud realizada por este documento. O deploy aguarda compra e acesso autorizado. A meta inicial é vender **gestão de agendamentos**, não todos os módulos ainda em construção.
