# Instrua — identidade orgânica e experiência multiplataforma

## Entrega nesta branch

Esta atualização reorganiza a **mesma aplicação React + TypeScript**, servida no navegador e empacotada pelo Tauri para Android, desktop e, futuramente, iOS. Não existe um frontend paralelo de produção. A API Java/Spring Boot com PostgreSQL permanece a fonte de verdade dos dados.

### Rotas compartilhadas (hash, compatível com hospedagem estática)

- `/#/` — homepage pública com a identidade Instrua, apresentação e acessos.
- `/#/entrar` — login em tela própria, com transição de carregamento durante autenticação.
- `/#/cadastro/pessoa` — criação de conta pessoal/paciente.
- `/#/cadastro/empresa` — criação de conta de estabelecimento.
- `/#/app` — área autenticada; o servidor determina os dados e permissões.
- `?view=visual-demo` e `?view=gemini-preview` — legado de QA opt-in, **não o produto principal**.

### Uma marca em todas as plataformas

Fonte vetorial oficial: `clients/instrua-app/src-tauri/icons/instrua.svg` e cópia para favicon/site em `clients/instrua-app/public/brand-icon.svg`. Uma bolinha verde-musgo com símbolo de confirmação off-white, fundo/transições com tons naturais. Ícones rasterizados para Android e Windows são derivados do SVG pela etapa `npx tauri icon` nos workflows. O manifesto web usa a mesma marca. Para iOS, gerar e testar os ícones de forma equivalente quando o projeto iOS estiver provisionado.

Tokens de referência: `#414A35` musgo, `#96977B` sage, `#D6C9AD` areia, `#A66F4B` terracota, `#F5F1E8` off-white. Organic design: curvas assimétricas, blocos arredondados, espaços generosos, tipografia editorial e contraste acessível.

### Módulos e honestidade de status

O **Hub de Módulos** lista os recursos conversados desde a origem do projeto em cinco áreas: operação, automações, pessoas/cuidados, comunidade, gestão/tecnologia. Quatro módulos podem abrir os formulários ligados à API atual: agenda, clientes, equipe e serviços. Todos os demais são identificados como em desenvolvimento e não têm ações fictícias. A lista é uma referência visual e de escopo, **não comprova backend implementado**.

### Teste contínuo e feedback

1. Validar o site na tela de celular e desktop; testar todos os links de navegação, formulários e retorno.
2. Verificar compilação via GitHub Actions `Instrua client build` e `Android APK QA`.
3. Compilar o APK QA **depois das alterações** para que home, logo e login novos apareçam no telefone.
4. Usar a área "Todos os módulos" → "Registrar feedback" ou criar issue em https://github.com/alexia-dev/Instrua/issues/new para bugs e ideias, sem dados pessoais reais.
5. Conferir CORS e URL da API no navegador e no app nativo. A build traz uma URL pública da API, mas a autenticação só funcionará quando servidor e banco estiverem realmente conectados.
6. Conferir acessibilidade: teclado, leitura de tela, contraste, alvos de toque, modo paisagem, fontes maiores, preferência por movimento reduzido.

### Pendências reais, não demos

- No Render, o site está hospedado, mas o backend depende da **variável privada `NEON_DATABASE_URL` configurada pelo titular no painel do Render**. Nunca registrar a URL com usuário/senha no GitHub nem em arquivos de configuração públicos.
- Não inserir prontuários ou outros dados de pacientes até validar a autorização de todos os endpoints, a segurança e backup/restauração.
- Criar implantação de produção com autenticação forte, recuperação de senha e permissões completas.
- Construir e testar módulos não entregues (antecipação, fila, radar, mensagens oficiais, instruções, calendários, prontuários, Social, Protege, Financeiro/Fiscal, Aprenda, Dante IA, plano mestre etc.) como funções reais e verificáveis.
- Habilitar atualização Android com chave de assinatura persistente. O APK QA atual utiliza certificado temporário **novo em cada execução**, portanto **pode ser necessário desinstalar a versão de teste anterior para atualizar**. Não usar o APK QA para distribuição comercial e não perder dados importantes em instalação de teste.
- Validar binários iOS em ambiente Apple e desktop Windows em execução real antes de declarar suporte final.

## Comandos

```sh
cd clients/instrua-app
npm ci
npm run build
# Gerar ícones nativos com Tauri:
npx tauri icon src-tauri/icons/instrua.svg --output src-tauri/icons
```

**Princípio:** toda melhoria de UI deve chegar aos clientes de produção usando o mesmo código-fonte, mas não deve ser confundida com disponibilidade de integração externa ou API.
