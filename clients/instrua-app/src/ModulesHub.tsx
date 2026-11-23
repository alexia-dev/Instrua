import "./modules-hub.css";
type ReadyTab="agenda"|"clientes"|"equipe"|"servicos";
type Area="Operação"|"Automações"|"Pessoas e cuidados"|"Comunidade"|"Gestão e tecnologia";
type Module={name:string;description:string;icon:string;area:Area;tab?:ReadyTab};
export const modules:Module[]=[
{name:"Agenda Inteligente",description:"Agenda real com profissionais, procedimentos, confirmação e cancelamento.",icon:"◷",area:"Operação",tab:"agenda"},
{name:"Gestão de clientes e fichas",description:"Cadastro de clientes e dados básicos com acesso por estabelecimento.",icon:"♙",area:"Operação",tab:"clientes"},
{name:"Equipe e convites",description:"Cadastre profissionais e convide colaboradores com código individual.",icon:"♧",area:"Operação",tab:"equipe"},
{name:"Serviços e procedimentos",description:"Configure duração, especialidade, nome e preço dos seus serviços.",icon:"✳",area:"Operação",tab:"servicos"},
{name:"Agenda por sala e profissional",description:"Colunas, semanas, filtros, encaixes, horários e disponibilidade configurável.",icon:"▦",area:"Operação"},
{name:"Pré-check-in e autorizações",description:"QR Code, pré-cadastro, elegibilidade e autorizações de consultas e exames.",icon:"◈",area:"Operação"},
{name:"Posição na fila",description:"Mostrar senhas, atendimentos anteriores e estimativas reais.",icon:"↗",area:"Operação"},
{name:"Central de Antecipação",description:"Oferecer vagas livres a quem deseja antecipar atendimento.",icon:"↺",area:"Automações"},
{name:"Fila de espera dinâmica",description:"Organizar interessados e encaixar conforme disponibilidade.",icon:"≋",area:"Automações"},
{name:"Oportunidades",description:"Divulgar horários liberados a pessoas elegíveis.",icon:"✧",area:"Automações"},
{name:"Radar de Imprevistos",description:"Detectar indisponibilidade, cancelamentos e conflitos antes de afetar a agenda.",icon:"⌁",area:"Automações"},
{name:"Confirmação e remarcação 48h",description:"Automatizar lembretes e propostas de remarcação conforme regras aprovadas.",icon:"⏱",area:"Automações"},
{name:"Mensagens e notificações",description:"Integração autorizada por WhatsApp oficial, e-mail e push, com histórico.",icon:"✉",area:"Automações"},
{name:"Instruções interativas",description:"Preparo de exames, PDFs, vídeos, checklists e confirmação de leitura.",icon:"☰",area:"Pessoas e cuidados"},
{name:"Calendário pessoal e cuidados",description:"Lembretes de exames, medicamentos, retornos, tarefas e horários.",icon:"◫",area:"Pessoas e cuidados"},
{name:"Solicitações clínicas",description:"Pedir documentos, receitas, relatórios e informações com fluxo profissional.",icon:"▤",area:"Pessoas e cuidados"},
{name:"Prontuários protegidos",description:"Registros específicos por unidade, especialidade e permissões clínicas.",icon:"♙",area:"Pessoas e cuidados"},
{name:"Mapa e acessibilidade",description:"Como chegar, local do atendimento, entrada e informações de mobilidade.",icon:"⌖",area:"Pessoas e cuidados"},
{name:"Instrua Social",description:"Feed, perfis, publicações, eventos, notícias e entretenimento com moderação.",icon:"✿",area:"Comunidade"},
{name:"Comunidades e aprendizado",description:"Discussões temáticas, cursos, instrutores e conteúdos do Instrua Aprenda.",icon:"✎",area:"Comunidade"},
{name:"Sugestões e votação",description:"Ideias da comunidade, votos, roadmap e explicações de atualizações.",icon:"♡",area:"Comunidade"},
{name:"Financeiro e assinaturas",description:"Planos, cobranças, recebimentos, indicadores, conciliação e relatórios.",icon:"◈",area:"Gestão e tecnologia"},
{name:"Fiscal e faturamento",description:"Notas, regras por jurisdição e integração com convênios e TISS.",icon:"▧",area:"Gestão e tecnologia"},
{name:"Instrua Protege",description:"Escalas, jornada, intervalos, justificativas e auditorias trabalhistas.",icon:"⚑",area:"Gestão e tecnologia"},
{name:"Dante AI",description:"Assistente para rotinas e orientações autorizadas, sem misturar dados.",icon:"✳",area:"Gestão e tecnologia"},
{name:"Administração da plataforma",description:"Painel mestre com segurança, unidades, versões e monitoramento sem acesso clínico automático.",icon:"◎",area:"Gestão e tecnologia"},
{name:"Gestão multissetorial",description:"Organizações, unidades e serviços para diferentes ocupações e profissões.",icon:"⋈",area:"Gestão e tecnologia"},
{name:"Auditoria e privacidade",description:"Trilhas de acesso, permissões, exportação e governança LGPD.",icon:"☑",area:"Gestão e tecnologia"}
];
const areaOrder:Area[]=["Operação","Automações","Pessoas e cuidados","Comunidade","Gestão e tecnologia"];
export default function ModulesHub({openTab}:{openTab:(tab:ReadyTab)=>void}){
return <section className="ins-modules">
  <div className="ins-modules-intro"><div><span className="ins-module-eyebrow">ECOSSISTEMA INSTRUA</span><h2>Um universo de possibilidades, <em>sem perder a simplicidade.</em></h2></div><p>Todos os módulos usam o mesmo projeto. Os disponíveis podem ser abertos aqui; os demais seguem no planejamento e serão ativados somente quando forem seguros e funcionais.</p></div>
  <div className="ins-modules-legend"><span><i className="ins-status-dot is-ready"/>Base conectada à API</span><span><i className="ins-status-dot is-planned"/>Em desenvolvimento</span></div>
  {areaOrder.map(area=><div key={area} className="ins-module-group"><h3>{area}</h3><div className="ins-module-grid">{modules.filter(m=>m.area===area).map(m=><article key={m.name} className="ins-module-item"><span className="ins-module-symbol" aria-hidden="true">{m.icon}</span><span className="ins-module-tag">{m.tab?"Base conectada":"Em desenvolvimento"}</span><h4>{m.name}</h4><p>{m.description}</p>{m.tab&&<button type="button" onClick={()=>openTab(m.tab!)}>Abrir módulo <span aria-hidden="true">↗</span></button>}</article>)}</div></div>)}
  <p className="ins-module-disclaimer">Os módulos apresentados como “Em desenvolvimento” não simulam dados nem prometem uma integração que ainda não está disponível. O objetivo é manter a arquitetura e a identidade do Instrua alinhadas enquanto cada função é entregue.</p>
</section>;
}
