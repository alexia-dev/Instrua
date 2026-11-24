import {useEffect,useMemo,useState} from "react";
import {api,type Appointment,type Session} from "./production-api";
import "./personal-dashboard-organic.css";

type Profile={name?:string;phone?:string|null;document_number?:string|null;birth_date?:string|null;postal_code?:string|null;street?:string|null;city?:string|null;state?:string|null};
type Health={allergies?:string|null;height_cm?:string|number|null};
type Share={company_id:string;company_name:string;permitted:boolean};
type Post={id:string;body:string;created_at:string};
type Destination="agenda"|"buscar"|"oportunidades"|"eventos"|"comunidade"|"calendario"|"configuracoes";
type Props={session:Session;appointments:Appointment[];onOpen:(destination:Destination)=>void};
const fmt=(value:string)=>new Intl.DateTimeFormat("pt-BR",{dateStyle:"medium",timeStyle:"short"}).format(new Date(value));
const upcoming=(a:Appointment)=>a.status!=="CANCELLED"&&a.status!=="NO_SHOW"&&new Date(a.startsAt).getTime()>=Date.now();
const labels:Record<string,string>={SCHEDULED:"Agendado",CONFIRMED:"Confirmado",COMPLETED:"Concluído",CANCELLED:"Cancelado",NO_SHOW:"Não compareceu"};
const errors=(e:unknown)=>e instanceof Error?e.message:"Não foi possível carregar seus dados.";
function I({name}:{name:string}){return <span aria-hidden="true" className="ipo-icon material-symbols-outlined">{name}</span>}
function InfoCard({icon,kicker,title,value,onClick}:{icon:string;kicker:string;title:string;value:string;onClick?:()=>void}){
 return <div className="ipo-metric"><div className="ipo-metric-head"><span className="ipo-icon-pill"><I name={icon}/></span><span className="ipo-metric-value">{value}</span></div><span className="ipo-kicker">{kicker}</span><strong>{title}</strong>{onClick&&<button type="button" onClick={onClick}>Abrir <I name="arrow_forward"/></button>}</div>;
}
export default function PersonalDashboardOrganic({session,appointments,onOpen}:Props){
 const [profile,setProfile]=useState<Profile|null>(null);
 const [health,setHealth]=useState<Health|null>(null);
 const [shares,setShares]=useState<Share[]>([]);
 const [posts,setPosts]=useState<Post[]>([]);
 const [profileError,setProfileError]=useState("");
 const [postsError,setPostsError]=useState("");
 const [loading,setLoading]=useState(true);
 const [revealHealth,setRevealHealth]=useState(false);
 const sorted=useMemo(()=>[...appointments].sort((a,b)=>a.startsAt.localeCompare(b.startsAt)),[appointments]);
 const future=useMemo(()=>sorted.filter(upcoming),[sorted]);
 const next=future[0];
 const todayCount=useMemo(()=>sorted.filter(a=>a.status!=="CANCELLED"&&new Date(a.startsAt).toDateString()===new Date().toDateString()).length,[sorted]);
 const approved=shares.filter(s=>s.permitted);
 const filled=profile?["name","phone","document_number","birth_date","postal_code","street","city","state"].filter(k=>Boolean(profile[k as keyof Profile])).length:0;
 const complete=Math.round(filled/8*100);
 useEffect(()=>{
   let active=true;setLoading(true);setProfileError("");setPostsError("");
   Promise.allSettled([
     api<Profile>("/me/profile",session.accessToken),
     api<Health>("/me/profile/private-health",session.accessToken),
     api<Share[]>("/me/profile/shares",session.accessToken),
     api<Post[]>("/social/posts",session.accessToken)
   ]).then(([p,h,sh,social])=>{
     if(!active)return;
     if(p.status==="fulfilled")setProfile(p.value);else setProfileError(errors(p.reason));
     if(h.status==="fulfilled")setHealth(h.value);
     if(sh.status==="fulfilled")setShares(sh.value);
     if(social.status==="fulfilled")setPosts(social.value);else setPostsError("Comunidade temporariamente indisponível.");
     setLoading(false);
   });
   return()=>{active=false;};
 },[session.accessToken]);
 return <div className="ipo-home">
  <section className="ipo-hero">
   <div><span className="ipo-eyebrow"><I name="favorite"/> INSTRUA · MINHA JORNADA</span><h1>Olá, {session.name.split(" ")[0]}. <em>Seu espaço, seu ritmo.</em></h1><p>Agendamentos, ficha pessoal, orientações e conexões — cada coisa no seu lugar.</p></div>
   <div className="ipo-hero-right"><span><I name="shield_person"/> Seus dados, suas escolhas</span><time>{new Intl.DateTimeFormat("pt-BR",{weekday:"long",day:"numeric",month:"long"}).format(new Date())}</time></div>
  </section>
  <section className="ipo-metrics" aria-label="Resumo pessoal">
    <InfoCard icon="calendar_month" kicker="PRÓXIMO COMPROMISSO" title={next?next.serviceName:"Nenhum agendamento futuro"} value={next?fmt(next.startsAt):"—"} onClick={()=>onOpen("agenda")}/>
    <InfoCard icon="badge" kicker="MINHA FICHA" title={loading?"Carregando…":profile?complete===100?"Dados essenciais preenchidos":"Completar meus dados":"Não disponível"} value={profile?complete+"%":"—"} onClick={()=>onOpen("configuracoes")}/>
    <InfoCard icon="verified_user" kicker="PRIVACIDADE" title="Estabelecimentos autorizados" value={String(approved.length)} onClick={()=>onOpen("configuracoes")}/>
    <InfoCard icon="event_available" kicker="AGENDA DE HOJE" title={todayCount===1?"Um compromisso registrado":"Compromissos registrados"} value={String(todayCount)} onClick={()=>onOpen("calendario")}/>
  </section>
  <div className="ipo-columns">
   <div className="ipo-main-column">
    <section className="ipo-panel ipo-next" aria-labelledby="ipo-next-title">
      <div className="ipo-panel-heading"><div><span className="ipo-eyebrow">ORGANIZAÇÃO PESSOAL</span><h2 id="ipo-next-title">Meu próximo atendimento</h2></div><button type="button" className="ipo-soft-button" onClick={()=>onOpen("agenda")}>Minha agenda <I name="arrow_forward"/></button></div>
      {next?<div className="ipo-next-card"><div className="ipo-next-top"><span className="ipo-icon-pill"><I name="medical_services"/></span><div><strong>{next.serviceName}</strong><small>{fmt(next.startsAt)} · {next.employeeName||"Profissional a confirmar"}</small></div><span className="ipo-status">{labels[next.status]||next.status}</span></div><div className="ipo-next-steps"><span><I name="check_circle"/> Agendamento registrado</span><span><I name="description"/> Confira as orientações com o estabelecimento</span></div><p className="ipo-notice">Pré-check-in e QR Code de chegada serão apresentados aqui quando o estabelecimento habilitar e o serviço estiver integrado.</p></div>:<div className="ipo-empty"><I name="event_available"/><strong>Você não tem agendamentos futuros.</strong><p>Explore os estabelecimentos disponíveis para encontrar um atendimento.</p><button type="button" onClick={()=>onOpen("buscar")} className="ipo-primary-button">Encontrar atendimento <I name="arrow_forward"/></button></div>}
    </section>
    <section className="ipo-panel" aria-labelledby="ipo-profile-title"><div className="ipo-panel-heading"><div><span className="ipo-eyebrow">SUAS INFORMAÇÕES</span><h2 id="ipo-profile-title">Minha ficha reutilizável</h2></div><button type="button" className="ipo-soft-button" onClick={()=>onOpen("configuracoes")}>Editar ficha <I name="edit"/></button></div>
     <p className="ipo-panel-desc">Os dados da sua conta podem ser atualizados por você e compartilhados separadamente com estabelecimentos autorizados.</p>
     {profileError&&<p role="alert" className="ipo-warning">{profileError}</p>}
     <div className="ipo-profile-grid">
       <div className="ipo-profile-tile"><div><I name="person"/> <strong>Dados cadastrais</strong></div><p>{profile?.name||session.name}</p><small>{profile?.city&&profile?.state?profile.city+" · "+profile.state:"Endereço ainda não informado"} · {profile?.phone?"Telefone cadastrado":"Telefone pendente"}</small><button onClick={()=>onOpen("configuracoes")}>Revisar informações <I name="arrow_forward"/></button></div>
       <div className="ipo-profile-tile"><div><I name="contact_emergency"/> <strong>Dados importantes</strong></div><p>{profile?.document_number?"CPF cadastrado":"CPF não informado"}</p><small>Dados pessoais disponíveis apenas conforme sua autorização.</small><button onClick={()=>onOpen("configuracoes")}>Completar minha ficha <I name="arrow_forward"/></button></div>
       <div className="ipo-profile-tile ipo-health-tile"><div><I name="health_and_safety"/> <strong>Minhas anotações privadas</strong></div>{revealHealth?<><p>{health?.allergies||"Nenhuma alergia informada"}</p><small>{health?.height_cm?String(health.height_cm)+" cm de altura informados":"Altura não informada"} · Essas notas não são exibidas à recepção na busca de clientes.</small></>:<><p>Informações pessoais ocultas</p><small>Você escolhe quando revelar suas anotações nesta tela.</small></>}<button type="button" onClick={()=>setRevealHealth(v=>!v)}>{revealHealth?"Ocultar dados":"Ver minhas notas"} <I name={revealHealth?"visibility_off":"visibility"}/></button></div>
       <div className="ipo-profile-tile"><div><I name="lock_person"/> <strong>Compartilhamentos</strong></div><p>{approved.length===0?"Nenhum estabelecimento autorizado":approved.length+" estabelecimento(s) autorizado(s)"}</p><small>{approved.length?approved.map(s=>s.company_name).slice(0,2).join(" · "):"Sua ficha não fica pública por telefone ou e-mail."}</small><button onClick={()=>onOpen("configuracoes")}>Gerenciar autorizações <I name="arrow_forward"/></button></div>
     </div>
    </section>
    <section className="ipo-panel" aria-labelledby="ipo-guides-title"><div className="ipo-panel-heading"><div><span className="ipo-eyebrow">ANTES E DEPOIS DO ATENDIMENTO</span><h2 id="ipo-guides-title">Guias e orientações</h2></div><span className="ipo-pill">Integração em desenvolvimento</span></div><div className="ipo-guide-placeholder"><I name="auto_stories"/><div><strong>Suas orientações vão aparecer aqui.</strong><p>Preparo de exames, checklists, vídeos e arquivos serão exibidos quando forem enviados pelo estabelecimento e vinculados ao atendimento.</p></div></div></section>
    <section className="ipo-panel" aria-labelledby="ipo-history-title"><div className="ipo-panel-heading"><div><span className="ipo-eyebrow">MEUS REGISTROS</span><h2 id="ipo-history-title">Linha do tempo de atendimentos</h2></div><button type="button" className="ipo-soft-button" onClick={()=>onOpen("agenda")}>Ver todos <I name="arrow_forward"/></button></div>
      {sorted.length===0?<p className="ipo-panel-desc">Seus atendimentos registrados aparecerão aqui quando você agendar.</p>:<div className="ipo-timeline">{sorted.slice().reverse().slice(0,4).map(a=><article key={a.id}><span className="ipo-icon-pill"><I name="event_note"/></span><div><small>{fmt(a.startsAt)}</small><strong>{a.serviceName}</strong><p>{a.employeeName||"Profissional a confirmar"} · {labels[a.status]||a.status}</p></div></article>)}</div>}
      <p className="ipo-caption">Esta linha do tempo mostra agendamentos, não prontuários de diferentes clínicas. O histórico clínico requer integração e controle de acesso próprios.</p>
    </section>
   </div>
   <aside className="ipo-side-column">
     <section className="ipo-panel ipo-assistant"><span className="ipo-icon-pill"><I name="psychology"/></span><span className="ipo-eyebrow">ASSISTENTE INSTRUA</span><h2>Dante</h2><p>Um assistente contextual para ajudar a organizar a rotina. As ações automáticas e triagens estão em desenvolvimento.</p><div className="ipo-guide-placeholder"><I name="lightbulb"/><div><strong>Seu próximo passo</strong><p>{profile&&complete<100?"Complete sua ficha para facilitar agendamentos.":next?"Confira os detalhes do seu próximo atendimento.":"Encontre seu primeiro atendimento no Instrua."}</p></div></div><button type="button" className="ipo-soft-button" onClick={()=>onOpen(profile&&complete<100?"configuracoes":next?"agenda":"buscar")}>Abrir <I name="arrow_forward"/></button></section>
     <section className="ipo-panel"><div className="ipo-panel-heading"><div><span className="ipo-eyebrow">OPORTUNIDADES</span><h2>Antecipação</h2></div><I name="history_toggle_off"/></div><p className="ipo-panel-desc">A Central de Antecipação inteligente ainda está sendo integrada. Seus agendamentos atuais não são monitorados automaticamente para remarcação nesta tela.</p><button type="button" className="ipo-soft-button" onClick={()=>onOpen("oportunidades")}>Explorar oportunidades <I name="arrow_forward"/></button></section>
     <section className="ipo-panel"><div className="ipo-panel-heading"><div><span className="ipo-eyebrow">PRIVACIDADE</span><h2>Quem pode usar meus dados?</h2></div><I name="shield_person"/></div><p className="ipo-panel-desc">Você tem {approved.length} autorização(ões) ativa(s) de compartilhamento da ficha administrativa.</p><div className="ipo-shares">{approved.length===0?<p>Nenhuma autorização ativa.</p>:approved.slice(0,3).map(s=><div key={s.company_id}><I name="verified_user"/><span>{s.company_name}</span></div>)}</div><button type="button" className="ipo-soft-button" onClick={()=>onOpen("configuracoes")}>Revisar consentimentos <I name="arrow_forward"/></button><p className="ipo-caption">Não exibe histórico de acesso clínico; auditoria detalhada ainda será implementada.</p></section>
     <section className="ipo-panel"><div className="ipo-panel-heading"><div><span className="ipo-eyebrow">CONEXÕES</span><h2>Instrua Social</h2></div><I name="forum"/></div>{postsError&&<p className="ipo-caption">{postsError}</p>}{posts.length===0?<p className="ipo-panel-desc">Nenhuma publicação disponível por enquanto.</p>:<div className="ipo-social-preview">{posts.slice(0,2).map(p=><p key={p.id}>{p.body.length>140?p.body.slice(0,140)+"…":p.body}</p>)}</div>}<button type="button" className="ipo-soft-button" onClick={()=>onOpen("comunidade")}>Abrir comunidade <I name="arrow_forward"/></button></section>
   </aside>
  </div>
 </div>;
}
