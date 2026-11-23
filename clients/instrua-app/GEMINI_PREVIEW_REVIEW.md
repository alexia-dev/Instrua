# Prévia mobile derivada do protótipo Gemini — 23/11/2026

## Situação

O arquivo recebido do Gemini era um único componente React/JSX (4.371 linhas), com dados fixos, importações do Firebase e uma chamada direta à API Gemini. **Não é um projeto Android nativo nem um backend de produção.** Esta integração **não substitui** os clientes e o backend já presentes no repositório e **não conclui** os requisitos da [especificação mestre](../../docs/INSTRUA_MASTER_REQUIREMENTS.md).

A prévia foi reescrita como um componente React + TypeScript isolado em `src/GeminiMobilePreview.tsx`, com CSS próprio, dentro da base Tauri 2 usada para testar Android. O cliente existente segue sendo a entrada principal.

## Como abrir

A partir de `clients/instrua-app`:

```bash
npm install
npm run dev
```

Abra `http://localhost:5173/` e toque em **Prévia mobile Gemini**, ou acesse `http://localhost:5173/?gemini-preview=1`. No Android/Tauri, a entrada padrão exibe um atalho para a prévia. Use o menu superior da prévia para voltar ao Instrua atual.

## O que a prévia demonstra

- Marca circular com check, cores oficiais, navegação inferior mobile e design responsivo.
- Perfis simulados de cliente, profissional, estabelecimento e recepção, sem conceder acessos reais.
- Agenda de oito profissionais, filtro por data, limite de 20 atendimentos/dia, bloqueio de sobreposição e intervalo de teste.
- Confirmar, fazer check-in e cancelar atendimentos **fictícios**; os estados só duram na sessão.
- Central de antecipação local: procura candidato pelo mesmo serviço/profissional, com detecção básica de conflito na troca.
- Exibição de posição de fila sem revelar nomes a quem visualiza a versão de cliente.
- Oportunidades, calendário e Social como interações ilustrativas.
- Controle de escala de texto e estados de foco.

## Falhas de segurança e lógica identificadas no código recebido

1. **Autorização falsa**: a simples troca de um `activeRole` no estado concedia interface de profissional, inclusive acesso clínico. Permissões devem vir da sessão e ser verificadas pelo backend para cada recurso, organização, unidade e atendimento.
2. **Banco inadequado para dados compartilhados**: o Firestore gravava um único JSON com o estado de toda a plataforma em caminho por usuário, sem modelagem multi-tenant, transações e regras auditáveis. Não usar isso para prontuários, agenda concorrente ou informações financeiras.
3. **Chave de IA no navegador e envio de nomes**: a chamada à API Gemini era feita diretamente pelo cliente, com chave vazia e inclusão de nomes na montagem do contexto. O Dante deve chamar serviço controlado no backend, com minimização de dados, autorização e proteção de segredos.
4. **Mensagens não enviadas**: notificações exibiam “WhatsApp”, “push” ou “recepção notificada” sem realizar entrega real. O sistema final deve registrar estado de fila, envio, falha, idempotência, preferência e retorno de provedores.
5. **Agendamento e antecipação sujeitos a corrida**: validações apenas em React e gravações independentes permitem dupla reserva; o encaixe também podia ignorar conflitos/intervalo. Mover validação, reserva e remanejamento para transações no servidor.
6. **Dados clínicos e atendimento**: o protótipo continha instruções médicas fixas e prontuários fictícios. Instruções clínicas dependem de aprovação do responsável habilitado, versão do protocolo e vínculo do serviço.
7. **Data e horário estáticos**: datas e `hoursUntil` eram codificados. Usar instantes e zonas por unidade, recomputar limites no servidor e tratar fusos.
8. **Confirmação e aceite indevidos**: existia botão capaz de “confirmar aceite do cliente” sem prova de consentimento. Diferenciar oferta, entrega, aceite, reserva e execução; validar elegibilidade antes de confirmar.
9. **Escopo incompleto**: verificação de identidade, QR real, autorização por convênio, prontuários isolados, autorização de procedimentos, auditoria, API versionada e integração com Android ainda precisam ser implementados.

## Regra de não regressão

- Não copiar o protótipo original sobre `App.tsx`, não colocar credenciais de IA/Firebase no cliente e não publicar dados médicos no repositório.
- O app principal, o backend Java e as branches existentes permanecem inalterados nesta proposta.
- Módulos de `Financeiro`, `Fiscal`, `Protege`, `Aprenda`, `Social` e os nove papéis continuam sob o contrato principal; a prévia **não** implica remoção ou substituição desses requisitos.
- Para produção, integrar cada fluxo à API Spring com testes de tenant e permissão. Só então remover as fixtures.

## Testes manuais recomendados

1. Abrir o cliente normal e a prévia, testar voltar; confirmar que o aplicativo normal não foi substituído.
2. Verificar layout em celular pequeno, tablet, teclado, leitor de tela e fonte ampliada.
3. No perfil Recepção, mudar data, escolher um profissional e tentar criar duas consultas no mesmo horário: a segunda deve ser recusada.
4. Tentar registrar atendimento fora do expediente e atravessando o intervalo: deve recusar.
5. Cancelar o exemplo de exame fictício do profissional 02 e conferir se uma oferta é criada para o atendimento de data futura; aceitar a oferta e verificar a agenda.
6. Alternar para Cliente e confirmar que não existem botões de cancelamento operacional de outros usuários e que a fila não revela nomes de terceiros.
7. Confirmar no tráfego de rede que **nenhuma** chamada de IA, Firebase, WhatsApp, pagamentos ou prontuário é iniciada por esta prévia.

**Estado de validação:** arquivos integrados ao GitHub em branch de revisão. Build TypeScript/Vite, empacotamento Android e testes em dispositivo não foram verificados por esta documentação.
