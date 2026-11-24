import {useEffect, useMemo, useState} from "react";
import {api, type Appointment, type Session} from "./production-api";
import "./instrua-home.css";

export type DiscoveryView = "inicio" | "oportunidades" | "eventos" | "comunidade";
export type HomeDestination = DiscoveryView | "agenda" | "modulos" | "equipe";
type Opportunity = {
  id:string; company_id:string; company_name:string; title:string;
  description:string|null; opportunity_type:string; price:number|null;
  currency:string; starts_at:string|null; ends_at:string|null;
};
type SocialPost = {id:string; body:string; created_at:string; audience:string};
type Props = {
  view:DiscoveryView; session:Session; appointments:Appointment[];
  isPatient:boolean; onOpen:(destination:HomeDestination)=>void;
};
const formatDate=(value:string)=>new Intl.DateTimeFormat("pt-BR",{day:"2-digit",month:"short",hour:"2-digit",minute:"2-digit"}).format(new Date(value));
const typeLabel:Record<string,string>={SLOT:"Horário disponível",VACANCY:"Vaga",EVENT:"Evento",SERVICE:"Serviço",PRODUCT:"Produto"};
function isToday(value:string) {const day=new Date(value);const now=new Date();return day.getFullYear()===now.getFullYear()&&day.getMonth()===now.getMonth()&&day.getDate()===now.getDate();}
function whenValid(value:string|null|undefined) {if(!value)return null;const d=new Date(value);return Number.isNaN(d.getTime())?null:d;}
function prices(price:number|null,currency:string){if(price===null||price===undefined)return null;try{return new Intl.NumberFormat("pt-BR",{style:"currency",currency:currency||"BRL"}).format(Number(price));}catch{return String(price);}}

export default function InstruaHome({view,session,appointments,isPatient,onOpen}:Props) {
  const [opportunities,setOpportunities]=useState<Opportunity[]>([]);
  const [posts,setPosts]=useState<SocialPost[]>([]);
  const [pending,setPending]=useState(true);
  const [notice,setNotice]=useState("");
  const [interestId,setInterestId]=useState<string|null>(null);
  const [sent,setSent]=useState<string[]>([]);
  const [refresh,setRefresh]=useState(0);
  useEffect(()=>{
    let active=true;
    setPending(true);
    setNotice("");
    Promise.allSettled([
      api<Opportunity[]>("/opportunities",session.accessToken),
      api<SocialPost[]>("/social/posts",session.accessToken)
    ]).then(([opp,feed])=>{
      if(!active)return;
      if(opp.status==="fulfilled")setOpportunities(opp.value);
      else setNotice("Não foi possível carregar algumas oportunidades.");
      if(feed.status==="fulfilled")setPosts(feed.value);
      else setNotice("Parte do conteúdo da comunidade está indisponível.");
      setPending(false);
    });
    return ()=>{active=false;};
  },[session.accessToken,refresh]);

  const today=useMemo(()=>appointments.filter(a=>isToday(a.startsAt)&&a.status!=="CANCELLED").sort((a,b)=>a.startsAt.localeCompare(b.startsAt)),[appointments]);
  const upcoming=useMemo(()=>appointments.filter(a=>new Date(a.startsAt).getTime()>=Date.now()&&a.status!=="CANCELLED").sort((a,b)=>a.startsAt.localeCompare(b.startsAt)),[appointments]);
  const eventItems=useMemo(()=>opportunities.filter(o=>o.opportunity_type==="EVENT"),[opportunities]);
  const openItems=useMemo(()=>opportunities.filter(o=>o.opportunity_type!=="EVENT"),[opportunities]);
  const trends=useMemo(()=>{
    const counts=new Map<string,number>();
    for(const post of posts){
      const published=whenValid(post.created_at);
      if(!published||!isToday(post.created_at))continue;
      for(const match of post.body.matchAll(/#([\p{L}\p{N}_]{2,40})/gu)){
        const tag=match[1].toLocaleLowerCase("pt-BR");
        counts.set(tag,(counts.get(tag)||0)+1);
      }
    }
    return [...counts].sort((a,b)=>b[1]-a[1]||a[0].localeCompare(b[0])).slice(0,6);
  },[posts]);

  async function expressInterest(id:string){
    if(interestId||sent.includes(id))return;
    setInterestId(id);setNotice("");
    try{
      await api("/opportunities/"+id+"/applications",session.accessToken,"POST",{message:"Tenho interesse nesta oportunidade pelo Instrua."});
      setSent(items=>[...items,id]);
      setNotice("Interesse registrado. O estabelecimento ainda precisa confirmar os próximos passos; isso não cria um agendamento automaticamente.");
    }catch(e){setNotice(e instanceof Error?e.message:"Não foi possível registrar o interesse.");}
    finally{setInterestId(null);}
  }

  const quickLinks:{title:string;sub:string;icon:string;to:HomeDestination}[]=[
    {title:isPatient?"Meus agendamentos":"Agenda e atendimentos",sub:"Seus horários organizados",icon:"◷",to:"agenda"},
    {title:"Oportunidades",sub:"Vagas e horários disponíveis",icon:"✧",to:"oportunidades"},
    {title:"Eventos",sub:"Atividades dos estabelecimentos",icon:"▤",to:"eventos"},
    {title:"Comunidade",sub:"Publicações e tendências",icon:"♡",to:"comunidade"},
    {title:"Todos os módulos",sub:"Conheça o ecossistema",icon:"✳",to:"modulos"}
  ];
  const welcome=isPatient?"Sua jornada, em um só lugar.":"Seu dia, em harmonia.";
  return <div className="ins-dash">
    {view==="inicio"&&<>
      <section className="ins-dash-hero">
        <div>
          <span className="ins-dash-eyebrow">INSTRUA · MEU DIA</span>
          <h1>{welcome}</h1>
          <p>Bem-vinda de volta, {session.name.split(" ")[0]}. Agendamentos, comunidade, eventos e oportunidades — sem trocar de aplicativo.</p>
        </div>
        <div className="ins-dash-date"><span>HOJE</span><strong>{new Intl.DateTimeFormat("pt-BR",{day:"2-digit",month:"long"}).format(new Date())}</strong><small>{today.length} {today.length===1?"atendimento":"atendimentos"} hoje</small></div>
      </section>
      <div className="ins-dash-summary">
        <div><small>Hoje na agenda</small><strong>{today.length}</strong></div>
        <div><small>Próximos atendimentos</small><strong>{upcoming.length}</strong></div>
        <div><small>Oportunidades abertas</small><strong>{openItems.length}</strong></div>
        <div><small>Eventos publicados</small><strong>{eventItems.length}</strong></div>
      </div>
      <section className="ins-dash-shortcuts" aria-labelledby="ins-shortcut-title">
        <div className="ins-dash-heading"><div><span className="ins-dash-eyebrow">EXPLORE O INSTRUA</span><h2 id="ins-shortcut-title">Tudo no seu lugar.</h2></div></div>
        <div className="ins-dash-shortcut-grid">{quickLinks.map(item=><button type="button" className="ins-dash-shortcut" key={item.title} onClick={()=>onOpen(item.to)}><span aria-hidden="true">{item.icon}</span><strong>{item.title}</strong><small>{item.sub}</small><b aria-hidden="true">↗</b></button>)}</div>
      </section>
    </>}
    {(view==="inicio")&&<div className="ins-dash-columns"><section className="ins-dash-panel">
      <div className="ins-dash-heading"><div><span className="ins-dash-eyebrow">ORGANIZAÇÃO</span><h2>Agendamentos de hoje</h2></div><button onClick={()=>onOpen("agenda")}>Abrir agenda →</button></div>
      {today.length===0?<p className="ins-dash-empty">Você ainda não tem atendimentos registrados para hoje.</p>:<div className="ins-dash-agenda">{today.slice(0,6).map(a=><article key={a.id}><time>{formatDate(a.startsAt)}</time><div><strong>{isPatient?a.serviceName:a.clientName}</strong><small>{isPatient?a.employeeName||"Profissional a confirmar":a.serviceName+" · "+(a.employeeName||"Sem profissional")}</small></div><span>{a.status==="CONFIRMED"?"Confirmado":"Agendado"}</span></article>)}</div>}
    </section><section className="ins-dash-panel ins-dash-trends">
      <div className="ins-dash-heading"><div><span className="ins-dash-eyebrow">COMUNIDADE</span><h2>Tendências do dia</h2></div><button onClick={()=>onOpen("comunidade")}>Ver rede social →</button></div>
      {trends.length?<div className="ins-dash-tags">{trends.map(([tag,count])=><span key={tag}>#{tag}<small>{count} {count===1?"publicação":"publicações"}</small></span>)}</div>:<p className="ins-dash-empty">Ainda não há assuntos em alta nas publicações públicas de hoje.</p>}
      <p className="ins-dash-source">Tendências calculadas somente a partir de publicações reais da comunidade Instrua.</p>
    </section></div>}
    {(view==="inicio"||view==="oportunidades")&&<section className="ins-dash-panel">
      <div className="ins-dash-heading"><div><span className="ins-dash-eyebrow">DESCUBRA MAIS</span><h2>Oportunidades</h2></div>{view==="inicio"&&<button onClick={()=>onOpen("oportunidades")}>Ver todas →</button>}</div>
      {pending?<p className="ins-dash-empty">Buscando oportunidades reais…</p>:openItems.length===0?<p className="ins-dash-empty">Nenhuma oportunidade publicada no momento.</p>:<div className="ins-dash-list">{openItems.slice(0,view==="inicio"?3:100).map(item=><article key={item.id}><span className="ins-dash-item-type">{typeLabel[item.opportunity_type]||"Oportunidade"}</span><h3>{item.title}</h3><p>{item.company_name}</p>{item.description&&<small>{item.description}</small>}<div className="ins-dash-item-bottom">{prices(item.price,item.currency)&&<strong>{prices(item.price,item.currency)}</strong>}<button type="button" disabled={!!interestId||sent.includes(item.id)} onClick={()=>void expressInterest(item.id)}>{sent.includes(item.id)?"Interesse enviado":interestId===item.id?"Enviando…":"Tenho interesse"}</button></div></article>)}</div>}
    </section>}
    {(view==="inicio"||view==="eventos")&&<section className="ins-dash-panel">
      <div className="ins-dash-heading"><div><span className="ins-dash-eyebrow">ENCONTROS E ATIVIDADES</span><h2>Eventos</h2></div>{view==="inicio"&&<button onClick={()=>onOpen("eventos")}>Explorar eventos →</button>}</div>
      {pending?<p className="ins-dash-empty">Consultando eventos publicados…</p>:eventItems.length===0?<p className="ins-dash-empty">Ainda não há eventos publicados. O calendário completo dos estabelecimentos está em desenvolvimento.</p>:<div className="ins-dash-list">{eventItems.slice(0,view==="inicio"?3:100).map(item=><article key={item.id}><span className="ins-dash-item-type">Evento do Instrua</span><h3>{item.title}</h3><p>{item.company_name}</p>{item.description&&<small>{item.description}</small>}<div className="ins-dash-item-bottom"><span>Confirme data e local com o organizador</span><button disabled={!!interestId||sent.includes(item.id)} onClick={()=>void expressInterest(item.id)}>{sent.includes(item.id)?"Interesse enviado":"Tenho interesse"}</button></div></article>)}</div>}
    </section>}
    {(view==="inicio"||view==="comunidade")&&<section className="ins-dash-panel">
      <div className="ins-dash-heading"><div><span className="ins-dash-eyebrow">CONEXÕES</span><h2>Instrua Social</h2></div>{view==="inicio"&&<button onClick={()=>onOpen("comunidade")}>Abrir comunidade →</button>}</div>
      {pending?<p className="ins-dash-empty">Buscando publicações públicas…</p>:posts.length===0?<p className="ins-dash-empty">Nenhuma publicação aprovada ainda. O feed mostrará somente conteúdo real e moderado.</p>:<div className="ins-dash-posts">{posts.slice(0,view==="inicio"?3:40).map(post=><article key={post.id}><div><span className="ins-dash-post-icon">✿</span><strong>Comunidade Instrua</strong><time>{formatDate(post.created_at)}</time></div><p>{post.body}</p></article>)}</div>}
      {view==="comunidade"&&<div className="ins-dash-trends"><h3>Tendências do dia</h3>{trends.length?<div className="ins-dash-tags">{trends.map(([tag,count])=><span key={tag}>#{tag} · {count}</span>)}</div>:<p className="ins-dash-empty">Sem tendências identificadas nas publicações de hoje.</p>}</div>}
      <p className="ins-dash-source">A publicação e moderação de conteúdos ainda não estão disponíveis neste painel.</p>
    </section>}
    {notice&&<p className="ins-dash-notice" role="status">{notice}</p>}
    <div className="ins-dash-refresh"><button type="button" onClick={()=>setRefresh(n=>n+1)} disabled={pending}>Atualizar novidades</button><small>Somente dados reais, conforme as permissões da sua conta.</small></div>
  </div>;
}
