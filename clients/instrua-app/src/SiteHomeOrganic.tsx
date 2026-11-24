import {useState,type MouseEvent} from "react";
import "./site-home-organic.css";

type Navigate=(path:string)=>void;
const modules=[
  {icon:"fast_forward",name:"Central de Antecipação",desc:"Um fluxo para oferecer vagas desocupadas a pessoas elegíveis da lista de espera.",note:"Planejado"},
  {icon:"hub",name:"Oportunidades",desc:"Horários liberados, espaços, serviços e oportunidades profissionais em um só lugar.",note:"Em evolução"},
  {icon:"radar",name:"Radar de Imprevistos",desc:"Acompanhe mudanças, atrasos e cancelamentos que exigem reorganização.",note:"Planejado"},
  {icon:"forum",name:"Instrua Social",desc:"Publicações, comunidades e tendências integradas à rotina de cada pessoa.",note:"Em evolução"},
  {icon:"badge",name:"Ficha Reutilizável",desc:"Dados pessoais preenchidos uma vez, com autorização específica para cada estabelecimento.",note:"Base integrada"},
  {icon:"assignment_turned_in",name:"Orientações Interativas",desc:"Guias, checklists e materiais enviados pelos estabelecimentos antes dos atendimentos.",note:"Planejado"},
  {icon:"calendar_month",name:"Calendário Universal",desc:"Compromissos pessoais, profissionais, exames, eventos e lembretes reunidos com clareza.",note:"Em evolução"},
  {icon:"verified_user",name:"Protege e Financeiro",desc:"Áreas específicas de controle operacional, registros financeiros e auditoria.",note:"Em evolução"}
];
const journeys=[
  {icon:"favorite",label:"Para pessoas",name:"Sua rotina, do seu jeito",text:"Uma jornada pessoal para encontrar serviços, acompanhar compromissos e descobrir oportunidades.",items:["Meus agendamentos e histórico","Ficha pessoal e compartilhamento controlado","Orientações, eventos e comunidade"],cta:"Criar conta pessoal",route:"/cadastro/pessoa",tone:"sage"},
  {icon:"clinical_notes",label:"Para profissionais",name:"Liberdade para atender",text:"Agenda própria, vínculos profissionais e acesso apenas aos atendimentos permitidos.",items:["Agenda do profissional","Convites de estabelecimentos","Organização da disponibilidade"],cta:"Conhecer acesso profissional",route:"/cadastro/empresa",tone:"cream"},
  {icon:"domain",label:"Para estabelecimentos",name:"Mais leveza na recepção",text:"Clientes, serviços, equipe e horários reunidos, cada função no seu espaço.",items:["Agenda multiprofissional","Cadastro e busca autorizada de clientes","Gestão e indicadores do dia"],cta:"Cadastrar estabelecimento",route:"/cadastro/empresa",tone:"clay"}
];
const sectors=[
  {icon:"stethoscope",name:"Saúde e clínicas",description:"Agendas de especialidades e atendimento com controle de acesso."},
  {icon:"spa",name:"Beleza e estética",description:"Serviços, profissionais e confirmações em um só fluxo."},
  {icon:"business_center",name:"Consultorias",description:"Reuniões, documentos e disponibilidade organizada."},
  {icon:"school",name:"Educação e mentorias",description:"Horários de aulas, atendimentos e compromissos."},
  {icon:"construction",name:"Serviços técnicos",description:"Agendamento de visitas e acompanhamento do serviço."}
];
const faq=[
 {q:"O Instrua é só um aplicativo de agendamento?",a:"Não. Agendamento é a base de um ecossistema que também inclui gestão, orientações, oportunidades, eventos e comunidade. Os módulos estão sendo desenvolvidos em etapas."},
 {q:"Como funciona a privacidade da ficha?",a:"A pessoa preenche sua ficha, e o estabelecimento só pode consultar os dados compartilhados conforme sua permissão e finalidade. Prontuários clínicos exigem proteções adicionais e não são liberados pela busca de e-mail ou telefone."},
 {q:"Posso usar como pessoa e profissional?",a:"Sim. A estrutura permite alternar entre a jornada pessoal e os vínculos profissionais autorizados. As ferramentas exibidas dependem das permissões daquela conta."},
 {q:"Todos os módulos já funcionam?",a:"Ainda não. A base comercial de cadastro e agenda está em validação; antecipação, automações, integrações de calendários e outros módulos avançados estão em desenvolvimento."},
 {q:"Quais são os preços e condições?",a:"As condições comerciais ainda serão publicadas. Não há uma tabela final de preços nem compromisso de gratuidade ou fidelidade anunciado nesta fase."},
];
function Icon({name,className=""}:{name:string;className?:string}){return <span className={"io-symbol material-symbols-outlined "+className} aria-hidden="true">{name}</span>}
export default function SiteHomeOrganic({onNavigate}:{onNavigate:Navigate}){
 const [mobileNav,setMobileNav]=useState(false);
 function route(e:MouseEvent<HTMLAnchorElement>,path:string){e.preventDefault();setMobileNav(false);onNavigate(path);}
 function go(id:string){setMobileNav(false);document.getElementById(id)?.scrollIntoView({behavior:"smooth",block:"start"});}
 const nav=[["ecossistema","Recursos"],["jornadas","Para quem é"],["modulos","Módulos"],["seguranca","Segurança"],["faq","Perguntas"]];
 const link=(label:string,path:string,style:string)=> <a className={style} href={"#"+path} onClick={e=>route(e,path)}>{label} <Icon name="arrow_forward"/></a>;
 return <div className="io-page" id="inicio">
  <header className="io-header">
   <div className="io-header-inner">
     <a className="io-wordmark" href="#/" onClick={e=>route(e,"/")} aria-label="Instrua, início"><img src="/brand-icon.svg" alt=""/><span><strong>instrua</strong><small>KIYUBI TECHNOLOGIES</small></span></a>
     <nav className={"io-nav "+(mobileNav?"open":"")} aria-label="Navegação institucional">{nav.map(([id,name])=><button type="button" key={id} onClick={()=>go(id)}>{name}</button>)}</nav>
     <div className="io-header-actions">{link("Entrar","/entrar","io-header-login")}{link("Criar conta","/cadastro/pessoa","io-button io-button-moss io-header-join")}<button type="button" className="io-mobile-menu" aria-label={mobileNav?"Fechar menu":"Abrir menu"} aria-expanded={mobileNav} onClick={()=>setMobileNav(v=>!v)}><Icon name={mobileNav?"close":"menu"}/></button></div>
   </div>
  </header>
  <main>
   <section className="io-hero io-section" aria-labelledby="io-hero-title">
    <div className="io-container io-hero-grid">
     <div className="io-hero-copy">
      <span className="io-kicker"><i/> ECOSSISTEMA KIYUBI TECHNOLOGIES</span>
      <h1 id="io-hero-title">Orientação, <em>conexão</em> e confiança.</h1>
      <p>Uma plataforma para organizar atendimentos, aproximar pessoas e simplificar o dia a dia de profissionais e estabelecimentos.</p>
      <div className="io-actions">{link("Começar agora","/cadastro/pessoa","io-button io-button-moss")}{link("Tenho um estabelecimento","/cadastro/empresa","io-button io-button-cream")}</div>
      <div className="io-hero-trust"><span><Icon name="check_circle"/> Uma conta, diferentes jornadas</span><span><Icon name="shield"/> Privacidade por concepção</span></div>
     </div>
     <div className="io-hero-stage" role="img" aria-label="Prévia ilustrativa de orientação, antecipação e confirmação de agendamentos">
      <div className="io-hero-glow"/>
      <article className="io-floating io-floating-top"><span className="io-avatar"><Icon name="psychology"/></span><div><div className="io-floating-heading"><strong>Assistente Dante</strong><small>Visão do produto</small></div><p>Uma vaga ficou livre na agenda. Quem está na lista de espera poderia ser convidado?</p></div></article>
      <article className="io-floating io-floating-main"><div className="io-floating-heading"><strong><span className="io-status-dot"/> Oportunidade de antecipação</strong><span className="io-pill">Exemplo</span></div><div className="io-appointment"><span><Icon name="calendar_month"/></span><div><strong>Consulta de dermatologia</strong><small>Hoje, 15h30 · Exemplo ilustrativo</small></div></div><div className="io-floating-bottom"><span>Agenda organizada</span><span>✓ Confirmações</span></div></article>
      <article className="io-floating io-floating-bottom-card"><Icon name="notifications_active"/><div><strong>Orientação preparada</strong><small>Checklist e informações para o atendimento</small></div></article>
     </div>
    </div>
   </section>
   <section id="ecossistema" className="io-section io-manifesto">
    <div className="io-container">
      <div className="io-heading io-centered"><span className="io-eyebrow">MAIS DO QUE FERRAMENTAS ISOLADAS</span><h2>Não somos apenas uma agenda. <em>Somos uma nova forma de conectar.</em></h2><p>O Instrua nasce para aproximar a autonomia de quem é atendido, o trabalho dos profissionais e a rotina de quem administra, em espaços diferentes e conectados.</p></div>
      <div className="io-manifesto-grid">
       <article><Icon name="event_busy"/><h3>Uma agenda comum</h3><p>Datas e horários isolados, muita procura manual e pouco espaço para a rotina completa.</p><small>Apenas uma parte do problema</small></article>
       <article><Icon name="folder_shared"/><h3>Informações repetidas</h3><p>Cadastros refeitos a cada atendimento, sistemas diferentes e trabalho duplicado.</p><small>Mais esforço do que deveria</small></article>
       <article className="io-manifesto-feature"><Icon name="nature_people"/><h3>O ecossistema Instrua</h3><p>Uma experiência que conecta o que importa, com dados separados por contexto e acesso autorizado.</p><small>Mais fluidez, menos ruído</small></article>
      </div>
    </div>
   </section>
   <section id="jornadas" className="io-section io-soft">
    <div className="io-container"><div className="io-heading io-centered"><span className="io-eyebrow">DESENVOLVIDO PARA DIFERENTES PAPÉIS</span><h2>Três jornadas. <em>Um mesmo cuidado.</em></h2></div>
      <div className="io-journey-grid">{journeys.map(j=><article key={j.label} className={"io-journey io-tone-"+j.tone}><div className="io-journey-top"><span className="io-round-icon"><Icon name={j.icon}/></span><small>{j.label}</small></div><h3>{j.name}</h3><p>{j.text}</p><ul>{j.items.map(s=><li key={s}><Icon name="check"/>{s}</li>)}</ul><a className="io-text-link" href={"#"+j.route} onClick={e=>route(e,j.route)}>{j.cta} <Icon name="arrow_forward"/></a></article>)}</div>
    </div>
   </section>
   <section id="modulos" className="io-section">
    <div className="io-container"><div className="io-heading io-between"><div><span className="io-eyebrow">UM ECOSSISTEMA EM EVOLUÇÃO</span><h2>Módulos pensados para <em>transformar a rotina.</em></h2></div><p>Uma visão completa do Instrua, construída e validada por etapas. Cada função terá sua própria página e permissões.</p></div>
     <div className="io-module-grid">{modules.map(m=><article key={m.name} className="io-module"><span className="io-round-icon"><Icon name={m.icon}/></span><h3>{m.name}</h3><p>{m.desc}</p><span className="io-module-state">{m.note}</span></article>)}</div>
    </div>
   </section>
   <section className="io-section io-soft">
    <div className="io-container io-showcase-grid"><div className="io-showcase-copy"><span className="io-eyebrow">MOBILIDADE COM PROPÓSITO</span><h2>Bonito no computador. <em>Intuitivo no celular.</em></h2><p>O Instrua tem uma identidade visual unificada para a experiência web e Android, com ferramentas adaptadas a cada tamanho de tela.</p><div className="io-showcase-points"><span><i>01</i> Agenda e serviços organizados</span><span><i>02</i> Navegação independente por perfil</span><span><i>03</i> Informações conectadas à mesma API</span></div><small>Prévia ilustrativa. Dados exibidos nesta seção são demonstrativos.</small></div>
      <div className="io-showcase-art"><div className="io-browser"><div className="io-browser-bar"><span>● ● ●</span><small>instrua · painel ilustrativo</small><Icon name="tune"/></div><div className="io-browser-metrics"><span><small>Agendamentos</small><strong>12</strong></span><span><small>Confirmados</small><strong>9</strong></span><span><small>Aguardando</small><strong>3</strong></span></div><div className="io-browser-rows"><div><i>MC</i><span><strong>Consulta agendada</strong><small>Hoje · 9h30</small></span><b>Confirmado</b></div><div><i>RF</i><span><strong>Retorno agendado</strong><small>Hoje · 10h</small></span><b>Pendente</b></div></div></div><div className="io-phone"><div className="io-phone-speaker"/><Icon name="calendar_month"/><small>Próximo compromisso</small><strong>10h · Hoje</strong><span>Orientações na palma da mão</span></div></div>
    </div>
   </section>
   <section id="seguranca" className="io-section"><div className="io-container"><div className="io-heading"><span className="io-eyebrow">PRIVACIDADE DESDE O INÍCIO</span><h2>Confiança também se constrói <em>com limites claros.</em></h2><p>Dados pessoais e clínicos exigem cuidados especiais. O projeto adota acesso por função, separação de estabelecimentos e consentimentos onde aplicáveis. A adequação integral depende de validações técnicas e jurídicas.</p></div><div className="io-security-grid"><article><Icon name="key"/><h3>Proteção dos dados</h3><p>Autenticação, conexão segura e proteção progressiva de registros. Controles técnicos sujeitos a auditoria.</p></article><article><Icon name="how_to_reg"/><h3>Autorização por estabelecimento</h3><p>A ficha administrativa pode ser compartilhada com consentimento específico e revogável.</p></article><article><Icon name="shield_person"/><h3>Prontuários separados</h3><p>O acesso às informações clínicas deve permanecer restrito aos profissionais autorizados de cada estabelecimento.</p></article></div></div></section>
   <section className="io-section io-soft" id="segmentos"><div className="io-container"><div className="io-heading io-centered"><span className="io-eyebrow">FEITO PARA VÁRIAS PROFISSÕES</span><h2>Um Instrua que se adapta <em>ao seu trabalho.</em></h2></div><div className="io-sectors">{sectors.map(s=><article key={s.name}><Icon name={s.icon}/><h3>{s.name}</h3><p>{s.description}</p></article>)}</div></div></section>
   <section id="faq" className="io-section"><div className="io-container io-faq-wrap"><div className="io-heading io-centered"><span className="io-eyebrow">TRANSPARÊNCIA SEM COMPLICAÇÃO</span><h2>Perguntas <em>frequentes.</em></h2></div><div className="io-faq-list">{faq.map(f=><details key={f.q}><summary>{f.q}<Icon name="expand_more"/></summary><p>{f.a}</p></details>)}</div></div></section>
   <section className="io-section io-soft"><div className="io-container"><div className="io-heading io-centered"><span className="io-eyebrow">PLANOS E JORNADAS</span><h2>O espaço certo <em>para começar.</em></h2><p>Os recursos e condições de contratação ainda estão sendo definidos. Conheça as experiências planejadas sem compromisso de preços nesta fase.</p></div><div className="io-plans"><article><small>JORNADA PESSOAL</small><h3>Pessoas e famílias</h3><p>Descubra estabelecimentos, acompanhe seus compromissos e escolha o que deseja compartilhar.</p>{link("Criar conta pessoal","/cadastro/pessoa","io-button io-button-cream")}</article><article className="io-plan-feature"><small>GESTÃO DO DIA A DIA</small><h3>Profissionais e estabelecimentos</h3><p>Organize agenda, equipe, serviços e operação em uma experiência própria.</p>{link("Cadastrar estabelecimento","/cadastro/empresa","io-button io-button-light")}</article><article><small>ESTRUTURA AMPLIADA</small><h3>Redes e unidades</h3><p>Uma arquitetura planejada para crescer com a equipe e incluir diferentes espaços.</p><button type="button" onClick={()=>go("modulos")} className="io-button io-button-cream">Ver os módulos <Icon name="arrow_forward"/></button></article></div></div></section>
   <section className="io-section io-final-cta"><div className="io-container"><div className="io-cta-box"><span className="io-eyebrow">COMECE UMA NOVA JORNADA</span><h2>Sua rotina pode ser <em>mais leve.</em></h2><p>Conheça uma plataforma criada para unir organização, conexão e confiança, sem misturar as necessidades de cada pessoa.</p><div className="io-actions">{link("Criar minha conta","/cadastro/pessoa","io-button io-button-light")}{link("Tenho um estabelecimento","/cadastro/empresa","io-button io-button-outline")}</div></div></div></section>
  </main>
  <footer className="io-footer"><div className="io-container io-footer-grid"><div><a href="#/" onClick={e=>route(e,"/")} className="io-wordmark"><img src="/brand-icon.svg" alt=""/><span><strong>instrua</strong><small>KIYUBI TECHNOLOGIES</small></span></a><p>Orientação, conexão e confiança. Um ecossistema em construção para aproximar pessoas, profissionais e estabelecimentos.</p></div><nav aria-label="Links do rodapé">{nav.map(([id,name])=><button key={id} type="button" onClick={()=>go(id)}>{name}</button>)}</nav><div><strong>Seu Instrua</strong><a href="#/entrar" onClick={e=>route(e,"/entrar")}>Entrar</a><a href="#/cadastro/pessoa" onClick={e=>route(e,"/cadastro/pessoa")}>Conta pessoal</a><a href="#/cadastro/empresa" onClick={e=>route(e,"/cadastro/empresa")}>Estabelecimentos</a></div></div><div className="io-container io-footer-bottom"><span>© {new Date().getFullYear()} Kiyubi Technologies. Todos os direitos reservados.</span><span>Instrua · Produto em desenvolvimento</span></div></footer>
 </div>;
}
