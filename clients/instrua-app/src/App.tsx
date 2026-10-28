import { useEffect, useMemo, useState, type FormEvent, type ReactNode } from "react";

type Page = "Início" | "Agenda" | "Pacientes" | "Orientações" | "Financeiro" | "Protege" | "Configurações";
const nav: { label: Page; icon: string; group: string }[] = [
  { label: "Início", icon: "⌂", group: "VISÃO GERAL" },
  { label: "Agenda", icon: "▦", group: "ATENDIMENTO" },
  { label: "Pacientes", icon: "♙", group: "ATENDIMENTO" },
  { label: "Orientações", icon: "▤", group: "ATENDIMENTO" },
  { label: "Financeiro", icon: "◈", group: "GESTÃO" },
  { label: "Protege", icon: "⬡", group: "GESTÃO" },
  { label: "Configurações", icon: "⚙", group: "SISTEMA" }
];
type Appointment = { id: string; date: string; time: string; name: string; detail: string; status: "Confirmado" | "Aguardando" | "Cancelado" };
const todayISO = () => {
  const now = new Date();
  return new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
};
const demoAppointments: Appointment[] = [
  { id: "demo-1", date: todayISO(), time: "08:00", name: "Mariana Oliveira (demo)", detail: "Consulta de retorno", status: "Confirmado" },
  { id: "demo-2", date: todayISO(), time: "08:30", name: "Carlos Henrique (demo)", detail: "Primeira consulta", status: "Aguardando" },
  { id: "demo-3", date: todayISO(), time: "09:00", name: "Beatriz Santos (demo)", detail: "Dermatologia", status: "Confirmado" },
  { id: "demo-4", date: todayISO(), time: "09:30", name: "João Pedro Lima (demo)", detail: "Consulta de retorno", status: "Confirmado" }
];
const modules = [
  { icon: "▦", title: "Agenda integrada", desc: "Consultas, exames e confirmações", page: "Agenda" as Page },
  { icon: "♙", title: "Pacientes", desc: "Perfis e dados autorizados", page: "Pacientes" as Page },
  { icon: "▤", title: "Orientações", desc: "Preparo e instruções de atendimento", page: "Orientações" as Page },
  { icon: "◈", title: "Financeiro e fiscal", desc: "Acompanhe lançamentos e pendências", page: "Financeiro" as Page },
  { icon: "⬡", title: "Instrua Protege", desc: "Jornada, intervalos e divergências", page: "Protege" as Page },
  { icon: "⚙", title: "Configurações", desc: "Organização, equipe e preferências", page: "Configurações" as Page }
];
function App() {
  const [page, setPage] = useState<Page>("Início");
  const [query, setQuery] = useState("");
  const [appointments, setAppointments] = useState<Appointment[]>(() => {
    try { const saved = localStorage.getItem("instrua-appointments-v1"); return saved ? JSON.parse(saved) as Appointment[] : demoAppointments; }
    catch { return demoAppointments; }
  });
  const [showAppointmentForm, setShowAppointmentForm] = useState(false);
  const [appointmentDate, setAppointmentDate] = useState(todayISO());
  const [appointmentTime, setAppointmentTime] = useState("10:00");
  const [appointmentName, setAppointmentName] = useState("");
  const [appointmentDetail, setAppointmentDetail] = useState("");
  const [notice, setNotice] = useState("");
  const [showMenu, setShowMenu] = useState(false);
  const [apiUrl, setApiUrl] = useState(localStorage.getItem("instrua-api-url") || "http://localhost:8080");
  const [apiSaved, setApiSaved] = useState(false);
  const today = useMemo(() => new Intl.DateTimeFormat("pt-BR", { weekday: "long", day: "numeric", month: "long" }).format(new Date()), []);
  useEffect(() => { localStorage.setItem("instrua-appointments-v1", JSON.stringify(appointments)); }, [appointments]);
  const todaysAppointments = appointments.filter(a => a.date === todayISO()).sort((a,b) => a.time.localeCompare(b.time));
  const filteredAppointments = appointments.filter(a => (a.name + " " + a.detail + " " + a.date + " " + a.status).toLowerCase().includes(query.toLowerCase())).sort((a,b) => (a.date + a.time).localeCompare(b.date + b.time));
  function openAppointmentForm() { setAppointmentDate(todayISO()); setAppointmentTime("10:00"); setAppointmentName(""); setAppointmentDetail(""); setShowAppointmentForm(true); }
  function createAppointment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const conflict = appointments.some(a => a.date === appointmentDate && a.time === appointmentTime && a.status !== "Cancelado");
    if (conflict) { setNotice("Já existe um agendamento nesse dia e horário. Escolha outro horário."); return; }
    const next: Appointment = { id: crypto.randomUUID(), date: appointmentDate, time: appointmentTime, name: appointmentName.trim(), detail: appointmentDetail.trim(), status: "Aguardando" };
    setAppointments(current => [...current, next]); setShowAppointmentForm(false); setPage("Agenda"); setNotice("Agendamento salvo neste dispositivo. Ainda não foi enviado ao servidor.");
  }
  function updateAppointmentStatus(id: string, status: Appointment["status"]) { setAppointments(current => current.map(a => a.id === id ? { ...a, status } : a)); }
  function removeAppointment(id: string) { if (window.confirm("Excluir este agendamento deste dispositivo?")) setAppointments(current => current.filter(a => a.id !== id)); }
  function saveApi() {
    localStorage.setItem("instrua-api-url", apiUrl.replace(/\/$/, ""));
    setApiSaved(true);
    setNotice("Endereço da API salvo neste dispositivo. A conexão real depende dos endpoints e da autenticação do backend.");
  }
  function navigate(p: Page) { setPage(p); setShowMenu(false); setNotice(""); }
  return <div className="app-shell">
    <aside className={showMenu ? "sidebar open" : "sidebar"}>
      <div className="brand"><div className="brand-mark">i<span>✓</span></div><div><strong>instrua</strong><small>Orientação, conexão e confiança.</small></div><button className="close-menu" onClick={() => setShowMenu(false)} aria-label="Fechar menu">×</button></div>
      <div className="workspace"><div className="workspace-icon">C</div><div><strong>Clínica demonstração</strong><small>Unidade principal</small></div><span className="chevron">⌄</span></div>
      {["VISÃO GERAL","ATENDIMENTO","GESTÃO","SISTEMA"].map(group => <div className="nav-group" key={group}><p>{group}</p>{nav.filter(n => n.group === group).map(n => <button key={n.label} className={page === n.label ? "nav-item active" : "nav-item"} onClick={() => navigate(n.label)}><span className="nav-icon">{n.icon}</span>{n.label}{n.label === "Agenda" && <span className="nav-count">{todaysAppointments.length}</span>}</button>)}</div>)}
      <div className="sidebar-bottom"><div className="help-card"><span>✦</span><strong>Precisa de ajuda?</strong><small>O Dante pode orientar você na plataforma.</small><button onClick={() => setNotice("Dante é assistivo nesta versão inicial. A integração com um provedor de IA ainda precisa ser configurada.")}>Conversar com Dante ↗</button></div><div className="profile-mini"><div className="avatar">AD</div><div><strong>Alexia Demo</strong><small>Administradora</small></div><span>•••</span></div></div>
    </aside>
    {showMenu && <button className="scrim" onClick={() => setShowMenu(false)} aria-label="Fechar menu" />}
    <main className="main">
      <header className="topbar"><button className="hamburger" onClick={() => setShowMenu(true)} aria-label="Abrir menu">☰</button><div className="breadcrumb">Clínica demonstração <span>/</span> <strong>{page}</strong></div><div className="top-actions"><span className="today">{today}</span><button className="icon-button" aria-label="Notificações" onClick={() => setNotice("Você está em uma demonstração local. Notificações externas ainda não estão conectadas.")}>♧<i /></button><div className="avatar">AD</div></div></header>
      <div className="content">
        {notice && <div className="notice"><span>ⓘ</span>{notice}<button onClick={() => setNotice("")} aria-label="Fechar aviso">×</button></div>}
        {page === "Início" && <>
          <div className="welcome-row"><div><div className="eyebrow">PAINEL DA UNIDADE</div><h1>Bom dia, Alexia <span>✳</span></h1><p>Um resumo do que está acontecendo na sua clínica hoje.</p></div><button className="primary-btn" onClick={openAppointmentForm}>＋ Novo agendamento</button></div>
          <div className="status-banner"><div className="status-symbol">✦</div><div><strong>Seu dia começa por aqui</strong><p>Confira a agenda, acompanhe confirmações e mantenha a equipe alinhada.</p></div><span className="banner-tag">VISÃO GERAL</span></div>
          <div className="stats-grid"><Stat icon="▦" label="Agendamentos hoje" value={String(todaysAppointments.filter(a=>a.status!=="Cancelado").length)} delta={`${todaysAppointments.filter(a=>a.status==="Confirmado").length} confirmados`} tone="green"/><Stat icon="♙" label="Pacientes aguardados" value={String(todaysAppointments.filter(a=>a.status==="Aguardando").length)} delta="aguardando confirmação" tone="sand"/><Stat icon="◷" label="Próximo atendimento" value={todaysAppointments[0]?.time ?? "—"} delta={todaysAppointments[0]?.name ?? "Sem atendimentos hoje"} tone="purple"/><Stat icon="✓" label="Taxa de confirmação" value={`${todaysAppointments.length ? Math.round(todaysAppointments.filter(a=>a.status==="Confirmado").length / todaysAppointments.length * 100) : 0}%`} delta="dos atendimentos de hoje" tone="blue"/></div>
          <div className="dashboard-grid"><section className="panel agenda-panel"><div className="panel-head"><div><h2>Agenda de hoje</h2><p>Próximos atendimentos da unidade</p></div><button className="text-btn" onClick={() => navigate("Agenda")}>Ver agenda completa ↗</button></div><div className="appointment-list">{todaysAppointments.slice(0,3).map((a,i)=><div className="appointment" key={a.id}><div className="time">{a.time}</div><div className={"timeline-dot dot-"+i}/><div className="appointment-info"><strong>{a.name}</strong><small>{a.detail}</small></div><span className={a.status==="Confirmado"?"pill confirmed":"pill waiting"}>{a.status}</span><button className="more-btn" onClick={() => setNotice("Ações do agendamento demonstrativas. A gestão real exige API conectada.")}>•••</button></div>)}</div><div className="panel-foot"><span><span className="green-dot"/> Dados salvos neste dispositivo</span><button onClick={() => navigate("Agenda")}>Abrir agenda →</button></div></section>
          <section className="panel quick-panel"><div className="panel-head"><div><h2>Acesso rápido</h2><p>O que você precisa fazer?</p></div></div><div className="quick-actions"><button onClick={() => navigate("Agenda")}><span className="quick-icon olive">＋</span><span><strong>Agendar atendimento</strong><small>Criar um novo horário</small></span><b>›</b></button><button onClick={() => navigate("Pacientes")}><span className="quick-icon terra">♙</span><span><strong>Cadastrar paciente</strong><small>Adicionar novo perfil</small></span><b>›</b></button><button onClick={() => navigate("Orientações")}><span className="quick-icon blue">▤</span><span><strong>Enviar orientação</strong><small>Preparar instruções</small></span><b>›</b></button></div></section></div>
          <section className="panel modules-panel"><div className="panel-head"><div><h2>Seus módulos</h2><p>Ferramentas para uma rotina mais organizada</p></div><span className="subtle-tag">PLATAFORMA INTEGRADA</span></div><div className="modules-grid">{modules.map(m=><button className="module-card" key={m.title} onClick={() => navigate(m.page)}><span className="module-icon">{m.icon}</span><span><strong>{m.title}</strong><small>{m.desc}</small></span><b>↗</b></button>)}</div></section>
        </>}
        {page === "Agenda" && <><PageHeading title="Agenda" subtitle="Organize consultas, exames e confirmações da unidade." action={<button className="primary-btn" onClick={openAppointmentForm}>＋ Novo agendamento</button>}/><section className="panel"><div className="panel-head"><div><h2>Atendimentos de hoje</h2><p>Busca e alterações são salvas localmente neste dispositivo; ainda não sincronizam com o servidor.</p></div><input className="search-input" value={query} onChange={e=>setQuery(e.target.value)} placeholder="Buscar atendimento…" /></div>{filteredAppointments.map(a=><div className="appointment full" key={a.id}><div className="time">{a.time}<small className="date-label">{a.date}</small></div><div className="appointment-info"><strong>{a.name}</strong><small>{a.detail}</small></div><select aria-label={`Status de ${a.name}`} className="status-select" value={a.status} onChange={e=>updateAppointmentStatus(a.id,e.target.value as Appointment["status"])}><option>Confirmado</option><option>Aguardando</option><option>Cancelado</option></select><button className="outline-btn" onClick={()=>removeAppointment(a.id)}>Excluir</button></div>)}
        {filteredAppointments.length===0&&<div className="empty-state"><strong>Nenhum agendamento encontrado</strong><p>Altere a busca ou crie um novo agendamento.</p><button className="primary-btn" onClick={openAppointmentForm}>＋ Novo agendamento</button></div>}</section></>}
        {page === "Pacientes" && <GenericPage title="Pacientes" subtitle="Perfis e dados acessíveis conforme autorização." icon="♙" items={["Cadastro e perfil do paciente","Histórico de agendamentos","Preferências de comunicação","Solicitações de documentos"]} onAction={()=>setNotice("Cadastro demonstrativo. Não utilize informações pessoais ou clínicas reais.")}/>}
        {page === "Orientações" && <GenericPage title="Orientações" subtitle="Instruções de preparo e informações de atendimento." icon="▤" items={["Biblioteca de orientações","Instruções por tipo de exame","Confirmação de leitura","Histórico e versões"]} onAction={()=>setNotice("Envio externo ainda não conectado. Configure consentimento, canal oficial e templates antes de produção.")}/>}
        {page === "Financeiro" && <GenericPage title="Financeiro e fiscal" subtitle="Acompanhe operações financeiras da organização." icon="◈" items={["Lançamentos e recebimentos","Pagamentos a profissionais","Recibos e documentos fiscais","Integrações de pagamento"]} onAction={()=>setNotice("Modo demonstrativo: não processa pagamentos nem emite NFS-e.")}/>}
        {page === "Protege" && <GenericPage title="Instrua Protege" subtitle="Jornada, intervalos e solicitações de correção." icon="⬡" items={["Registros de entrada e saída","Intervalos e jornada prevista","Divergências para análise","Histórico de correções"]} onAction={()=>setNotice("Módulo demonstrativo. Não substitui sistema oficial de ponto nem valida registros legais.")}/>}
        {page === "Configurações" && <><PageHeading title="Configurações" subtitle="Personalize a conexão da aplicação e as preferências locais."/><section className="panel settings-panel"><h2>Conexão com o backend</h2><p>Informe a URL da API Instrua. Não coloque senhas ou chaves secretas neste campo.</p><label htmlFor="api-url">Endereço base da API</label><input id="api-url" className="settings-input" value={apiUrl} onChange={e=>{setApiUrl(e.target.value);setApiSaved(false)}} placeholder="https://api.seudominio.com"/><div className="settings-hint">Por padrão, usa <code>http://localhost:8080</code> para desenvolvimento local.</div><button className="primary-btn" onClick={saveApi}>Salvar configuração</button>{apiSaved&&<p className="success-text">✓ Endereço salvo neste dispositivo.</p>}<hr/><h2>Sobre esta versão</h2><p>Cliente multiplataforma inicial do Instrua. As telas atuais usam dados de demonstração; a URL configurada ainda não transforma automaticamente todos os módulos em operações reais.</p><div className="info-row"><span>Versão</span><strong>0.1.0 · Fundação visual</strong></div><div className="info-row"><span>Cliente</span><strong>Web · Android · iOS · Desktop (base inicial)</strong></div><div className="info-row"><span>Segurança</span><strong>Não utilizar dados reais nesta etapa</strong></div></section></>}
        {showAppointmentForm && <div className="modal-backdrop" role="presentation" onMouseDown={e=>{if(e.target===e.currentTarget)setShowAppointmentForm(false)}}><section className="appointment-modal" role="dialog" aria-modal="true" aria-labelledby="appointment-title"><div className="modal-heading"><div><span className="eyebrow">AGENDA · NOVO REGISTRO</span><h2 id="appointment-title">Novo agendamento</h2><p>Este registro ficará salvo apenas neste dispositivo.</p></div><button className="modal-close" onClick={()=>setShowAppointmentForm(false)} aria-label="Fechar">×</button></div><form onSubmit={createAppointment} className="appointment-form"><label>Nome para identificação<input required maxLength={100} value={appointmentName} onChange={e=>setAppointmentName(e.target.value)} placeholder="Nome de demonstração" /></label><div className="form-two"><label>Data<input required type="date" min={todayISO()} value={appointmentDate} onChange={e=>setAppointmentDate(e.target.value)} /></label><label>Horário<input required type="time" value={appointmentTime} onChange={e=>setAppointmentTime(e.target.value)} /></label></div><label>Tipo de atendimento<input required maxLength={120} value={appointmentDetail} onChange={e=>setAppointmentDetail(e.target.value)} placeholder="Ex.: consulta de retorno" /></label><div className="form-note">Não use dados reais de pacientes nesta versão. A agenda ainda não possui autenticação nem sincronização com o backend.</div><div className="modal-actions"><button type="button" className="outline-btn" onClick={()=>setShowAppointmentForm(false)}>Cancelar</button><button type="submit" className="primary-btn">Salvar agendamento</button></div></form></section></div>}
        <footer className="footer"><span>instrua <b>·</b> Orientação, conexão e confiança.</span><span>Versão 0.1.0 <b>·</b> Ambiente demonstrativo</span></footer>
      </div>
    </main>
  </div>;
}
function Stat({icon,label,value,delta,tone}:{icon:string;label:string;value:string;delta:string;tone:string}){return <div className="stat-card"><div className="stat-top"><span className={"stat-icon "+tone}>{icon}</span><span className="stat-menu">•••</span></div><span className="stat-label">{label}</span><strong className="stat-value">{value}</strong><small className="stat-delta">{delta}</small></div>}
function PageHeading({title,subtitle,action}:{title:string;subtitle:string;action?:ReactNode}){return <div className="welcome-row page-heading"><div><div className="eyebrow">INSTRUA · ÁREA DE TRABALHO</div><h1>{title}</h1><p>{subtitle}</p></div>{action}</div>}
function GenericPage({title,subtitle,icon,items,onAction}:{title:string;subtitle:string;icon:string;items:string[];onAction:()=>void}){return <><PageHeading title={title} subtitle={subtitle} action={<button className="primary-btn" onClick={onAction}>＋ Nova ação</button>}/><section className="panel generic-panel"><div className="generic-hero"><span>{icon}</span><div><h2>{title}</h2><p>Estrutura inicial pronta para conectar aos serviços do Instrua.</p></div><span className="subtle-tag">BASE INICIAL</span></div>{items.map((item,i)=><button className="setting-item" key={item} onClick={onAction}><span className="setting-item-icon">{["▦","♙","▤","◈"][i%4]}</span><span><strong>{item}</strong><small>Área preparada para implementação e integração</small></span><b>↗</b></button>)}</section></>}
export default App;