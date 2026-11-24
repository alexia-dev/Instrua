import { useMemo, useState } from "react";
import "./gemini-preview.css";

/**
 * Protótipo visual baseado no código recebido do Gemini (23/11/2026).
 * A fonte de verdade continua sendo a API Java/Spring do Instrua.
 * Nenhuma ação aqui registra dados clínicos, envia WhatsApp ou altera produção.
 */

type Role = "cliente" | "profissional" | "estabelecimento" | "recepcao";
type Tab = "inicio" | "agenda" | "antecipacao" | "fila" | "oportunidades" | "calendario" | "social";
type DemoStatus = "agendado" | "confirmado" | "aguardando_fila" | "concluido" | "cancelado";
type Appointment = {
  id: number;
  client: string;
  professionalId: string;
  service: string;
  date: string;
  start: string;
  duration: number;
  status: DemoStatus;
};
type Offer = { id: number; appointmentId: number; date: string; start: string; status: "pendente" | "aceita" };
const professionals = [
  { id: "p1", name: "Profissional demonstrativo 01", specialty: "Consulta", start: "08:00", end: "18:00" },
  { id: "p2", name: "Profissional demonstrativo 02", specialty: "Exame", start: "08:00", end: "18:00" },
  { id: "p3", name: "Profissional demonstrativo 03", specialty: "Terapia", start: "08:00", end: "18:00" },
  { id: "p4", name: "Profissional demonstrativo 04", specialty: "Consulta", start: "08:00", end: "18:00" },
  { id: "p5", name: "Profissional demonstrativo 05", specialty: "Aula", start: "08:00", end: "18:00" },
  { id: "p6", name: "Profissional demonstrativo 06", specialty: "Serviço", start: "08:00", end: "18:00" },
  { id: "p7", name: "Profissional demonstrativo 07", specialty: "Consulta", start: "08:00", end: "18:00" },
  { id: "p8", name: "Profissional demonstrativo 08", specialty: "Exame", start: "08:00", end: "18:00" }
];
const roles: Array<{ id: Role; label: string }> = [
  { id: "cliente", label: "Cliente / Paciente" },
  { id: "profissional", label: "Profissional" },
  { id: "estabelecimento", label: "Estabelecimento" },
  { id: "recepcao", label: "Recepção" }
];
const tabLabels: Record<Tab, string> = {
  inicio: "Início", agenda: "Agenda", antecipacao: "Antecipação", fila: "Minha vez",
  oportunidades: "Oportunidades", calendario: "Calendário", social: "Social"
};
function dateAt(offset: number): string {
  const date = new Date();
  date.setHours(12, 0, 0, 0);
  date.setDate(date.getDate() + offset);
  return [date.getFullYear(), String(date.getMonth() + 1).padStart(2, "0"), String(date.getDate()).padStart(2, "0")].join("-");
}
function minutes(time: string): number {
  const [hours, min] = time.split(":").map(Number);
  return hours * 60 + min;
}
function toTime(value: number): string {
  return String(Math.floor(value / 60)).padStart(2, "0") + ":" + String(value % 60).padStart(2, "0");
}
function formatDate(value: string): string {
  const [y, m, d] = value.split("-");
  return d + "/" + m + "/" + y;
}
const initialAppointments: Appointment[] = [
  { id: 101, client: "Pessoa demonstrativa A", professionalId: "p1", service: "Consulta demonstrativa", date: dateAt(0), start: "09:00", duration: 30, status: "confirmado" },
  { id: 102, client: "Pessoa demonstrativa B", professionalId: "p2", service: "Exame demonstrativo", date: dateAt(0), start: "10:00", duration: 45, status: "agendado" },
  { id: 103, client: "Pessoa demonstrativa C", professionalId: "p2", service: "Exame demonstrativo", date: dateAt(7), start: "11:00", duration: 45, status: "confirmado" },
  { id: 104, client: "Pessoa demonstrativa D", professionalId: "p3", service: "Terapia demonstrativa", date: dateAt(0), start: "11:30", duration: 50, status: "aguardando_fila" }
];
const statusLabel: Record<DemoStatus, string> = {
  agendado: "Aguardando confirmação", confirmado: "Confirmado",
  aguardando_fila: "Na fila", concluido: "Concluído", cancelado: "Cancelado"
};
const primaryTabs: Tab[] = ["inicio", "agenda", "fila", "oportunidades"];
const operations: Tab[] = ["antecipacao", "calendario", "social"];

export default function GeminiMobilePreview() {
  const [tab, setTab] = useState<Tab>("inicio");
  const [role, setRole] = useState<Role>("cliente");
  const [appointments, setAppointments] = useState<Appointment[]>(initialAppointments);
  const [offers, setOffers] = useState<Offer[]>([]);
  const [selectedDate, setSelectedDate] = useState(dateAt(0));
  const [selectedProfessional, setSelectedProfessional] = useState("p1");
  const [newClient, setNewClient] = useState("Pessoa demonstrativa");
  const [newTime, setNewTime] = useState("14:00");
  const [menuOpen, setMenuOpen] = useState(false);
  const [largeText, setLargeText] = useState(false);
  const [message, setMessage] = useState("");
  const [events, setEvents] = useState([{ id: 1, name: "Lembrete demonstrativo", date: dateAt(1), hour: "08:00" }]);
  const [eventName, setEventName] = useState("");
  const [socialLiked, setSocialLiked] = useState(false);

  const isStaff = role !== "cliente";
  const countToday = appointments.filter(a => a.date === dateAt(0) && a.status !== "cancelado").length;
  const queue = appointments.filter(a => a.status === "aguardando_fila" && a.date === dateAt(0));
  const remainingOffers = offers.filter(o => o.status === "pendente");

  const visibleAppointments = useMemo(() =>
    appointments.filter(a => a.date === selectedDate && (isStaff || a.client === "Pessoa demonstrativa A"))
      .sort((a, b) => a.start.localeCompare(b.start)),
    [appointments, selectedDate, isStaff]);

  function notify(text: string) { setMessage(text); }

  function changeStatus(id: number, status: DemoStatus) {
    if (role === "cliente" && appointments.find(a => a.id === id)?.client !== "Pessoa demonstrativa A") {
      notify("Ação indisponível neste perfil de demonstração."); return;
    }
    setAppointments(previous => previous.map(a => a.id === id ? { ...a, status } : a));
    notify("Estado alterado somente no protótipo. Nenhuma mensagem foi enviada.");
  }

  function book() {
    if (!isStaff) { notify("Agendamento de terceiros requer vínculo e autorização reais."); return; }
    const p = professionals.find(item => item.id === selectedProfessional);
    if (!p || !newClient.trim() || !/^[0-2][0-9]:[0-5][0-9]$/.test(newTime)) {
      notify("Preencha os campos do agendamento."); return;
    }
    const start = minutes(newTime);
    const end = start + 30;
    if (start < minutes(p.start) || end > minutes(p.end) || start < 13 * 60 && end > 12 * 60) {
      notify("Horário fora do expediente ou no intervalo demonstrativo (12h–13h)."); return;
    }
    const existing = appointments.filter(a => a.professionalId === selectedProfessional &&
      a.date === selectedDate && a.status !== "cancelado");
    if (existing.length >= 20) { notify("Limite demonstrativo: 20 atendimentos por profissional/dia."); return; }
    if (existing.some(a => start < minutes(a.start) + a.duration && end > minutes(a.start))) {
      notify("Este horário já está ocupado. Escolha outro."); return;
    }
    setAppointments(prev => [...prev, {
      id: Date.now(), client: newClient.trim(), professionalId: p.id,
      service: "Consulta demonstrativa", date: selectedDate, start: newTime,
      duration: 30, status: "agendado"
    }]);
    notify("Agendamento fictício criado localmente, sem integração com a clínica.");
  }

  function cancelWithOffer(id: number) {
    if (!isStaff) { notify("Cancelamentos operacionais exigem autorização."); return; }
    const cancelled = appointments.find(a => a.id === id);
    if (!cancelled || cancelled.status === "cancelado") return;
    const candidate = appointments.find(a => a.id !== id && a.service === cancelled.service &&
      a.professionalId === cancelled.professionalId && a.date > cancelled.date &&
      a.status !== "cancelado" && !offers.some(o => o.appointmentId === a.id && o.status === "pendente"));
    setAppointments(prev => prev.map(a => a.id === id ? { ...a, status: "cancelado" } : a));
    if (candidate) {
      setOffers(prev => [...prev, { id: Date.now(), appointmentId: candidate.id, date: cancelled.date, start: cancelled.start, status: "pendente" }]);
      notify("Simulação: sugestão de antecipação criada. Aceite real do cliente não verificado.");
    } else notify("Horário liberado no protótipo. Não há candidato compatível nos dados fictícios.");
  }

  function acceptOffer(id: number) {
    if (!isStaff) { notify("A confirmação de aceite exige consentimento real do titular."); return; }
    const offer = offers.find(o => o.id === id && o.status === "pendente");
    const appointment = appointments.find(a => a.id === offer?.appointmentId);
    if (!offer || !appointment) return;
    const start = minutes(offer.start), end = start + appointment.duration;
    const overlapping = appointments.some(a => a.id !== appointment.id && a.professionalId === appointment.professionalId &&
      a.date === offer.date && a.status !== "cancelado" &&
      start < minutes(a.start) + a.duration && end > minutes(a.start));
    if (overlapping) { notify("Vaga indisponível: conflito detectado, alteração não realizada."); return; }
    setAppointments(prev => prev.map(a => a.id === appointment.id ?
      { ...a, date: offer.date, start: offer.start, status: "confirmado" } : a));
    setOffers(prev => prev.map(o => o.id === id ? { ...o, status: "aceita" } : o));
    notify("Antecipação simulada. Em produção, depende do aceite autenticado do cliente e validação no servidor.");
  }

  function currentProfessional(id: string) { return professionals.find(p => p.id === id)?.name || "Profissional"; }

  return <div className="gmp" style={{ fontSize: largeText ? "112.5%" : "100%" }}>
    <header className="gmp-header">
      <div className="gmp-brand"><img className="gmp-symbol ins-brand-asset" src="/brand-icon.svg" alt="" /><div><strong>instrua</strong><small>Orientação, conexão e confiança.</small></div></div>
      <button type="button" className="gmp-icon-button" aria-label="Abrir todas as seções" aria-expanded={menuOpen} onClick={() => setMenuOpen(!menuOpen)}>☰</button>
    </header>
    <div className="gmp-demo-note" role="note">PRÉVIA GEMINI · Apenas dados fictícios · Sem conexão com sistemas reais</div>
    <main className="gmp-main">
      <div className="gmp-tools">
        <label>Visualizar perfil fictício
          <select aria-label="Papel simulado" value={role} onChange={event => setRole(event.target.value as Role)}>
            {roles.map(r => <option key={r.id} value={r.id}>{r.label}</option>)}
          </select>
        </label>
        <button type="button" onClick={() => setLargeText(v => !v)} aria-pressed={largeText} className="gmp-outline">Texto {largeText ? "normal" : "maior"}</button>
      </div>

      {message && <div className="gmp-message" role="status">{message}<button type="button" onClick={() => setMessage("")} aria-label="Dispensar aviso">×</button></div>}
      {menuOpen && <nav className="gmp-more" aria-label="Todas as seções">{operations.map(t =>
        <button key={t} type="button" onClick={() => { setTab(t); setMenuOpen(false); }}>{tabLabels[t]}</button>)}
        <a href={window.location.pathname}>Voltar ao Instrua atual</a></nav>}

      {tab === "inicio" && <>
        <section className="gmp-welcome"><p className="gmp-eyebrow">Seu espaço Instrua</p><h1>Olá! Como podemos facilitar seu dia?</h1><p>Agendamento, orientação e conexão em um único lugar.</p></section>
        <div className="gmp-kpis">
          <div><span>Atendimentos hoje</span><strong>{countToday}</strong></div>
          <div><span>Na fila</span><strong>{queue.length}</strong></div>
          <div><span>Antecipações</span><strong>{remainingOffers.length}</strong></div>
        </div>
        <section className="gmp-panel"><h2>Acesso rápido</h2><div className="gmp-shortcuts">
          {(["agenda", "antecipacao", "fila", "oportunidades", "calendario", "social"] as Tab[]).map(t =>
            <button type="button" key={t} onClick={() => setTab(t)}>{tabLabels[t]} <span aria-hidden="true">›</span></button>)}
        </div></section>
        <section className="gmp-panel"><h2>Orientações e ficha protegida</h2><p>O Gemini sugeriu fichas completas, documentos e preparos interativos. Nesta prévia, esses dados não são carregados: a autorização clínica e as orientações aprovadas precisam vir do backend.</p><p className="gmp-muted">Somente profissionais autorizados poderão consultar cada registro na organização correspondente.</p></section>
      </>}

      {tab === "agenda" && <>
        <div className="gmp-title"><h1>Agenda inteligente</h1><p>Calendário de demonstração com oito profissionais, sem dados de pacientes reais.</p></div>
        <label className="gmp-field">Data <input aria-label="Data da agenda" type="date" value={selectedDate} onChange={e => setSelectedDate(e.target.value)} /></label>
        <div className="gmp-professionals" role="group" aria-label="Selecionar profissional">{professionals.map(p => {
          const count = appointments.filter(a => a.professionalId === p.id && a.date === selectedDate && a.status !== "cancelado").length;
          return <button type="button" key={p.id} aria-pressed={selectedProfessional === p.id} className={selectedProfessional === p.id ? "selected" : ""} onClick={() => setSelectedProfessional(p.id)}><b>{p.name.replace("Profissional demonstrativo ", "Prof. ")}</b><small>{p.specialty} · {count}/20</small></button>;
        })}</div>
        {isStaff && <section className="gmp-panel"><h2>Novo agendamento de teste</h2>
          <div className="gmp-form"><label>Nome fictício<input value={newClient} onChange={e => setNewClient(e.target.value)} maxLength={80}/></label>
          <label>Hora<input type="time" value={newTime} onChange={e => setNewTime(e.target.value)}/></label>
          <button type="button" className="gmp-primary" onClick={book}>Criar no protótipo</button></div>
        </section>}
        <section className="gmp-panel"><h2>Agendamentos na data</h2>{visibleAppointments.length === 0 && <p>Nenhum atendimento fictício nessa data.</p>}
          {visibleAppointments.map(a => <article className="gmp-appointment" key={a.id}>
            <div><b>{a.start} — {toTime(minutes(a.start) + a.duration)}</b><span className="gmp-status">{statusLabel[a.status]}</span></div>
            <h3>{a.client}</h3><p>{a.service} · {currentProfessional(a.professionalId)}</p>
            {a.status !== "cancelado" && <div className="gmp-actions">
              {!["confirmado", "concluido", "aguardando_fila"].includes(a.status) && <button type="button" onClick={() => changeStatus(a.id, "confirmado")}>Confirmar</button>}
              {isStaff && <button type="button" onClick={() => changeStatus(a.id, "aguardando_fila")}>Check-in</button>}
              {isStaff && <button type="button" onClick={() => cancelWithOffer(a.id)}>Cancelar + buscar vaga</button>}
            </div>}
          </article>)}
        </section>
      </>}

      {tab === "antecipacao" && <><div className="gmp-title"><h1>Central de antecipação</h1><p>Após um cancelamento simulado, identifique outra pessoa com o mesmo serviço e profissional.</p></div>
        <section className="gmp-panel"><h2>Ofertas fictícias</h2>
          {offers.length === 0 && <p>Nenhuma oferta criada. Vá à Agenda e cancele um horário de teste.</p>}
          {offers.map(o => { const a = appointments.find(item => item.id === o.appointmentId); return <article key={o.id} className="gmp-appointment">
            <h3>{a?.client || "Candidato"}</h3><p>{a?.service} · {formatDate(o.date)} às {o.start}</p><span className="gmp-status">{o.status}</span>
            {o.status === "pendente" && <button type="button" className="gmp-primary" onClick={() => acceptOffer(o.id)} disabled={!isStaff}>Simular aceite validado</button>}
          </article>; })}
        </section><p className="gmp-muted">Na versão real, a vaga deve ser reservada de forma transacional, com aceite comprovado, compatibilidade de preparo, convênio, sala e permissões.</p></>}

      {tab === "fila" && <><div className="gmp-title"><h1>Minha vez na fila</h1><p>Posição de demonstração, sem atualização de clínica real.</p></div>
        <section className="gmp-panel"><h2>Fila de hoje</h2>{queue.length === 0 && <p>Ninguém aguardando neste cenário.</p>}
          {queue.map((a, i) => <article key={a.id} className="gmp-appointment"><b>Posição {i + 1}</b><h3>{isStaff ? a.client : "Pessoa aguardando"}</h3><p>{a.service} · {currentProfessional(a.professionalId)}</p>
            {isStaff && <button type="button" onClick={() => changeStatus(a.id, "concluido")} className="gmp-outline">Concluir atendimento fictício</button>}
          </article>)}
        </section></>}

      {tab === "oportunidades" && <><div className="gmp-title"><h1>Oportunidades</h1><p>Horários disponíveis para antecipação e encaixe. Sem leilões.</p></div>
        <section className="gmp-panel"><h2>Exemplo de oportunidade</h2><div className="gmp-appointment"><h3>Atendimento demonstrativo</h3><p>Agenda: amanhã, 15h · Unidade fictícia</p><button type="button" className="gmp-primary" onClick={() => notify("Reserva ilustrativa: nenhuma vaga real foi bloqueada.")}>Simular interesse</button></div></section></>}

      {tab === "calendario" && <><div className="gmp-title"><h1>Meu calendário</h1><p>Eventos, aulas e lembretes pessoais em modo local.</p></div>
        <section className="gmp-panel"><h2>Adicionar lembrete de teste</h2><div className="gmp-form"><label>Descrição<input value={eventName} onChange={e => setEventName(e.target.value)} maxLength={100}/></label>
          <button className="gmp-primary" type="button" onClick={() => { if (eventName.trim()) { setEvents(prev => [...prev, { id: Date.now(), name: eventName.trim(), date: dateAt(1), hour: "09:00" }]); setEventName(""); } }}>Adicionar para amanhã</button></div>
          {events.map(e => <article className="gmp-appointment" key={e.id}><b>{formatDate(e.date)} · {e.hour}</b><h3>{e.name}</h3></article>)}
        </section><p className="gmp-muted">Não há notificações de medicamentos ou sincronização do calendário neste protótipo.</p></>}

      {tab === "social" && <><div className="gmp-title"><h1>Instrua Social</h1><p>Comunidade, conteúdos e conexões com privacidade por padrão.</p></div>
        <article className="gmp-panel"><span className="gmp-eyebrow">CONTEÚDO DE DEMONSTRAÇÃO</span><h2>Seu espaço para aprender e compartilhar</h2><p>Publicações, vídeos, comentários, denúncias e moderação fazem parte do plano do Instrua. Este exemplo não publica conteúdo na internet.</p><button className="gmp-outline" type="button" aria-pressed={socialLiked} onClick={() => setSocialLiked(!socialLiked)}>{socialLiked ? "♥ Curtido" : "♡ Curtir"} (simulado)</button></article></>}
    </main>
    <nav className="gmp-bottom" aria-label="Navegação mobile">{primaryTabs.map(t =>
      <button type="button" key={t} aria-current={tab === t ? "page" : undefined} className={tab === t ? "selected" : ""} onClick={() => { setTab(t); setMenuOpen(false); }}><span aria-hidden="true">{t === "inicio" ? "⌂" : t === "agenda" ? "▦" : t === "fila" ? "◷" : "✳"}</span>{tabLabels[t]}</button>)}
    </nav>
  </div>;
}