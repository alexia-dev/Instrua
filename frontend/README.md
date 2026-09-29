# Instrua Frontend

Carcaça responsiva/PWA do módulo Instrua do NEXA.

- Login JWT consumindo o backend real.
- Lista de pacientes via API.
- Agenda via API.
- Estrutura para instruções.
- Layout responsivo para desktop, tablet e celular.
- PWA instalável quando servido por HTTPS ou localhost.
- URL da API configurável na tela de login.

## Rodar

python -m http.server 5500 -d frontend

Abra http://localhost:5500.

O backend Instrua precisa estar disponível na URL informada no login.

A carcaça é deliberadamente simples, sem framework, para ficar fácil de alterar. Ela pode migrar para React depois sem mudar o contrato da API.