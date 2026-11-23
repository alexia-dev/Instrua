import "./site-home-details.css";

type Navigate = (path: string) => void;

const journeys = [
  {symbol:"♡",title:"Clientes e pacientes",kind:"Uma experiência pessoal e acolhedora",description:"Agendamentos, orientações, calendário de cuidados, oportunidades e comunidade no mesmo lugar, sem expor informações de outras pessoas.",items:["Meu dia e compromissos","Confirmações e instruções","Oportunidades e eventos","Conteúdos e comunidade"]},
  {symbol:"▦",title:"Recepção",kind:"Um espaço feito para quem organiza tudo",description:"Agenda visual por profissional, nome do paciente e procedimento, filtros, confirmações e acompanhamento da rotina de atendimento.",items:["Agenda multiprofissional","Cadastros e encaixes","Cancelamentos e confirmações","Fila e antecipações planejadas"]},
  {symbol:"✚",title:"Profissionais",kind:"Foco nos próprios atendimentos",description:"Informações necessárias para a atuação profissional, agenda individual e comunicação com a equipe, sempre dentro das permissões atribuídas.",items:["Minha agenda","Atendimentos e horários","Instruções autorizadas","Notificações relevantes"]},
  {symbol:"◇",title:"Gestão e administração",kind:"Visibilidade sem misturar permissões",description:"Unidades, pessoas, serviços, indicadores e configurações. Cada perfil administrativo vê somente o que está autorizado a gerenciar.",items:["Equipes e acessos","Serviços e unidades","Indicadores operacionais","Privacidade e auditoria"]}
];
const capabilities = [
  {symbol:"▦",name:"Agenda inteligente",desc:"Compromissos, profissionais e serviços conectados ao estabelecimento.",state:"base"},
  {symbol:"✓",name:"Confirmação e cancelamento",desc:"Acompanhe o andamento de cada agendamento, sem perder o histórico.",state:"base"},
  {symbol:"♧",name:"Clientes, equipe e serviços",desc:"Cadastros separados, com vínculo à unidade e permissões de acesso.",state:"base"},
  {symbol:"✳",name:"Oportunidades",desc:"Horários liberados oferecidos a interessados elegíveis, conforme regras de cada serviço.",state:"roadmap"},
  {symbol:"↺",name:"Central de Antecipação",desc:"Propostas de antecipação quando surge uma vaga compatível.",state:"roadmap"},
  {symbol:"≈",name:"Fila de espera dinâmica",desc:"Prioridades e disponibilidade reunidas para reduzir ociosidade.",state:"roadmap"},
  {symbol:"◉",name:"Radar de Imprevistos",desc:"Sinalização de problemas e remanejamentos que merecem atenção.",state:"roadmap"},
  {symbol:"▤",name:"Eventos e calendário",desc:"Atividades, palestras, exames, retornos, lembretes e compromissos.",state:"roadmap"},
  {symbol:"✿",name:"Instrua Social",desc:"Comunidades, publicações, conteúdos e tendências organizados por interesse.",state:"roadmap"},
  {symbol:"◷",name:"Vez na fila",desc:"Acompanhar o andamento do atendimento quando houver dados disponíveis.",state:"roadmap"},
  {symbol:"▣",name:"Pré-check-in e QR Code",desc:"Conferir dados e simplificar a chegada ao estabelecimento.",state:"roadmap"},
  {symbol:"✉",name:"Orientações e avisos",desc:"Preparos, instruções e lembretes, respeitando consentimentos e preferências.",state:"roadmap"},
  {symbol:"♙",name:"Ficha com acesso restrito",desc:"Informações necessárias disponíveis somente para profissionais autorizados.",state:"roadmap"},
  {symbol:"◇",name:"Financeiro e fiscal",desc:"Gestão e integrações especializadas sem perder as fronteiras de acesso.",state:"roadmap"},
  {symbol:"✦",name:"Dante AI",desc:"Apoio às rotinas com revisão humana em atividades sensíveis.",state:"roadmap"},
  {symbol:"⬡",name:"Proteção e acessibilidade",desc:"Permissões, auditoria e telas inclusivas em todos os dispositivos.",state:"roadmap"}
] as const;
const questions=[
  {q:"O Instrua é só um aplicativo de agendamento?",a:"Não. Agendamentos são parte da experiência. A visão do Instrua também reúne oportunidades, eventos, comunidade, orientações, gestão, proteção de dados e automações. Os módulos são entregues e validados por etapas."},
  {q:"Cliente e recepcionista veem a mesma tela?",a:"Não. A conta identifica os vínculos e as permissões de cada pessoa. O cliente tem uma jornada pessoal; recepção, profissionais e gestão usam ferramentas específicas do seu trabalho. O servidor precisa validar as permissões em toda operação."},
  {q:"O site e o Android compartilham informações?",a:"A arquitetura usa uma API e uma base central para os clientes web e Android. A sincronização depende de conexão, autorização e integração efetivamente disponível para cada módulo."},
  {q:"O que já pode ser usado?",a:"A versão comercial possui base integrada de agenda, clientes, profissionais e serviços. As automações e recursos adicionais mostrados como previstos não devem ser tratados como ativos até passarem por testes completos."},
  {q:"O Instrua serve apenas para clínicas?",a:"Não. A estrutura foi pensada para diferentes serviços e estabelecimentos. Dados sensíveis de saúde têm regras adicionais de acesso, segurança e privacidade."}
];

export default function SiteHomeDetails({onNavigate}:{onNavigate:Navigate}) {
  return <>
    <section id="experiencia" className="ins-detail-experience" aria-labelledby="ins-experience-title">
      <div className="ins-detail-heading">
        <span className="ins-section-eyebrow">UM INSTRUA, VÁRIAS FORMAS DE USAR</span>
        <h2 id="ins-experience-title">Bonito de ver. <em>Simples de entender.</em></h2>
        <p>Uma entrada acolhedora para quem procura um serviço. Ferramentas completas para quem trabalha nos bastidores. Tudo com a mesma identidade orgânica.</p>
      </div>
      <div className="ins-device-layout">
        <div className="ins-desktop-preview" aria-label="Prévia ilustrativa da organização do painel do estabelecimento">
          <div className="ins-preview-toolbar"><span className="ins-preview-dots"><i/><i/><i/></span><strong>♡ instrua</strong><span>Painel do estabelecimento</span></div>
          <div className="ins-preview-workspace">
            <aside><strong>INSTRUA</strong><span className="active">▦ Painel</span><span>◷ Agenda</span><span>♧ Clientes</span><span>✚ Profissionais</span><span>✳ Oportunidades</span><span>▤ Eventos</span></aside>
            <div className="ins-preview-main"><small>BOAS-VINDAS · VISÃO ORGANIZACIONAL</small><h3>Seu dia, bem organizado.</h3><div className="ins-preview-metrics"><span>Agenda do dia</span><span>Confirmações</span><span>Equipe</span></div><div className="ins-preview-board"><div><strong>Agenda por profissional</strong><div className="ins-preview-slot"><b>09:00</b><span>Atendimento · espaço reservado</span></div><div className="ins-preview-slot"><b>10:30</b><span>Procedimentos e horários</span></div><div className="ins-preview-slot"><b>14:00</b><span>Confirmações e status</span></div></div><div><strong>Organização da recepção</strong><p>Confirmações</p><p>Fila de espera</p><p>Oportunidades</p></div></div></div>
          </div>
        </div>
        <div className="ins-mobile-preview" aria-label="Prévia ilustrativa da organização do aplicativo para clientes">
          <div className="ins-mobile-top"><span>9:41</span><span>●●●</span></div><strong className="ins-mobile-brand">♡ instrua</strong><div className="ins-mobile-greeting"><small>SEU ESPAÇO</small><h3>Olá, que bom te ver! <span>🌿</span></h3><p>O que vamos organizar hoje?</p></div><div className="ins-mobile-next"><small>PRÓXIMO COMPROMISSO</small><strong>Seus agendamentos ficam aqui</strong><p>Depois de entrar, consulte os dados da sua conta.</p></div><div className="ins-mobile-options"><span>▦<small>Agenda</small></span><span>✳<small>Oportunidades</small></span><span>♡<small>Social</small></span><span>▤<small>Calendário</small></span></div><div className="ins-mobile-bottom"><span>⌂ Início</span><span>◷ Agenda</span><span>♡ Social</span><span>○ Perfil</span></div>
        </div>
      </div>
      <p className="ins-detail-preview-caption">Prévia conceitual de organização — sem pacientes, métricas ou oportunidades fictícias. Os dados reais só aparecem nas áreas autenticadas e autorizadas.</p>
    </section>

    <section id="jornadas" className="ins-detail-journies" aria-labelledby="ins-journeys-title">
      <div className="ins-detail-heading"><span className="ins-section-eyebrow">CADA PESSOA TEM SUA PRÓPRIA JORNADA</span><h2 id="ins-journeys-title">Não é tudo misturado. <em>É tudo conectado.</em></h2><p>Uma conta pode ter vários vínculos, mas cada espaço apresenta apenas o que faz sentido para aquele papel.</p></div>
      <div className="ins-journey-grid">{journeys.map((item,i)=><article className="ins-journey-card" key={item.title}><div className="ins-journey-top"><span className="ins-journey-symbol" aria-hidden="true">{item.symbol}</span><small>0{i+1}</small></div><h3>{item.title}</h3><strong>{item.kind}</strong><p>{item.description}</p><ul>{item.items.map(s=><li key={s}>{s}</li>)}</ul></article>)}</div>
    </section>

    <section id="ecossistema" className="ins-detail-ecosystem" aria-labelledby="ins-ecosystem-title">
      <div className="ins-detail-heading"><span className="ins-section-eyebrow">UM ECOSSISTEMA COMPLETO, CONSTRUÍDO POR ETAPAS</span><h2 id="ins-ecosystem-title">Tem lugar para <em>cada detalhe.</em></h2><p>Do primeiro agendamento à gestão da operação: conheça as funções que compõem a visão do Instrua. Aqui, a situação de cada recurso aparece de forma transparente.</p></div>
      <div className="ins-detail-legend"><span><i className="ins-legend-base"/> Base ligada à API, ainda em validação</span><span><i className="ins-legend-roadmap"/> Em desenvolvimento</span></div>
      <div className="ins-capability-grid">{capabilities.map(item=><article key={item.name} className="ins-capability"><div className="ins-capability-top"><span aria-hidden="true">{item.symbol}</span><small className={item.state==="base"?"ins-capability-base":"ins-capability-roadmap"}>{item.state==="base"?"Base integrada":"Em desenvolvimento"}</small></div><h3>{item.name}</h3><p>{item.desc}</p></article>)}</div>
    </section>

    <section className="ins-detail-flow" aria-labelledby="ins-flow-title"><div className="ins-detail-heading"><span className="ins-section-eyebrow">COMEÇAR DEVE SER FÁCIL</span><h2 id="ins-flow-title">Do primeiro acesso ao <em>seu dia a dia.</em></h2></div><ol><li><span>01</span><strong>Conheça o Instrua</strong><p>Descubra serviços, jornadas e possibilidades.</p></li><li><span>02</span><strong>Entre ou crie sua conta</strong><p>Identidade e autenticação para acessar seu espaço.</p></li><li><span>03</span><strong>Abra sua área</strong><p>Cliente, profissional, recepção ou gestão, conforme seus vínculos.</p></li><li><span>04</span><strong>Use os recursos disponíveis</strong><p>Acompanhe seus dados e explore módulos habilitados.</p></li></ol><div className="ins-detail-flow-actions"><button type="button" onClick={()=>onNavigate("/cadastro/pessoa")}>Criar conta pessoal ↗</button><button type="button" onClick={()=>onNavigate("/cadastro/empresa")}>Cadastrar estabelecimento →</button></div></section>

    <section id="perguntas" className="ins-detail-faq" aria-labelledby="ins-faq-title"><div><span className="ins-section-eyebrow">PERGUNTAS FREQUENTES</span><h2 id="ins-faq-title">Tudo explicado, <em>sem complicar.</em></h2><p>Informação clara também faz parte do cuidado. Estas respostas distinguem a visão do produto da funcionalidade já disponível.</p></div><div className="ins-faq-items">{questions.map(item=><details key={item.q}><summary>{item.q}<span aria-hidden="true">＋</span></summary><p>{item.a}</p></details>)}</div></section>
  </>;
}
