import {useMemo,useState} from "react";
import type {Appointment,AppointmentStatus,Professional} from "./production-api";
import "./schedule-board.css";

const statusText:Record<AppointmentStatus,string>={
  SCHEDULED:"Agendado",CONFIRMED:"Confirmado",COMPLETED:"Concluído",
  CANCELLED:"Cancelado",NO_SHOW:"Não compareceu"
};
const timeText=(date:string)=>new Intl.DateTimeFormat("pt-BR",{hour:"2-digit",minute:"2-digit"}).format(new Date(date));
const dateText=(date:string)=>new Intl.DateTimeFormat("pt-BR",{weekday:"long",day:"numeric",month:"long"}).format(new Date(date));
const dateInput=(d:Date)=>[d.getFullYear(),String(d.getMonth()+1).padStart(2,"0"),String(d.getDate()).padStart(2,"0")].join("-");
const sameDay=(iso:string,day:string)=>dateInput(new Date(iso))===day;
const addDays=(day:string,offset:number)=>{const d=new Date(day+"T12:00:00");d.setDate(d.getDate()+offset);return dateInput(d);};

type Props = {
  appointments:Appointment[];
  professionals:Professional[];
  canManage:boolean;
  busy:boolean;
  onStatus?:(id:string,status:AppointmentStatus)=>void;
};
export default function ScheduleBoard({appointments,professionals,canManage,busy,onStatus}:Props){
  const [date,setDate]=useState(()=>dateInput(new Date()));
  const [search,setSearch]=useState("");
  const [view,setView]=useState<"colunas"|"lista">("colunas");
  const [employee,setEmployee]=useState("all");
  const dayItems=useMemo(()=>appointments.filter(a=>sameDay(a.startsAt,date))
    .filter(a=>employee==="all"||a.employeeId===employee)
    .filter(a=>(a.clientName+" "+a.serviceName+" "+(a.employeeName||"")).toLocaleLowerCase("pt-BR").includes(search.toLocaleLowerCase("pt-BR")))
    .sort((a,b)=>new Date(a.startsAt).getTime()-new Date(b.startsAt).getTime()),[appointments,date,search,employee]);
  const cols=useMemo(()=>{
    const seen=new Set<string>();
    const result:{id:string;name:string}[]=[];
    for(const p of professionals){if(employee==="all"||employee===p.id){seen.add(p.id);result.push({id:p.id,name:p.name});}}
    for(const a of dayItems){const key=a.employeeId||"unassigned";if(!seen.has(key)){seen.add(key);result.push({id:key,name:a.employeeName||"Sem profissional"});}}
    return result;
  },[professionals,dayItems,employee]);
  function item(a:Appointment) {
    const done=a.status==="COMPLETED"||a.status==="CANCELLED"||a.status==="NO_SHOW";
    return <article key={a.id} className={"ins-booking-card ins-booking-"+a.status.toLowerCase()}>
      <div className="ins-booking-time"><time>{timeText(a.startsAt)}</time><span>{statusText[a.status]}</span></div>
      <strong>{a.clientName}</strong><p>{a.serviceName}</p>
      <small>{timeText(a.startsAt)}–{timeText(a.endsAt)}</small>
      {canManage&&!done&&onStatus&&<div className="ins-booking-actions">
        {a.status==="SCHEDULED"&&<button type="button" disabled={busy} onClick={()=>onStatus(a.id,"CONFIRMED")}>Confirmar</button>}
        <button type="button" disabled={busy} onClick={()=>onStatus(a.id,"COMPLETED")}>Concluir</button>
      </div>}
    </article>;
  }
  return <div className="ins-schedule">
    <div className="ins-schedule-tools">
      <div className="ins-schedule-day"><button aria-label="Dia anterior" onClick={()=>setDate(d=>addDays(d,-1))}>‹</button><label><span>Data da agenda</span><input type="date" value={date} onChange={e=>setDate(e.target.value)}/></label><button aria-label="Próximo dia" onClick={()=>setDate(d=>addDays(d,1))}>›</button><button className="ins-schedule-today" onClick={()=>setDate(dateInput(new Date()))}>Hoje</button></div>
      <div className="ins-schedule-filters"><label><span>Buscar paciente ou procedimento</span><input type="search" value={search} placeholder="Pesquisar na agenda" onChange={e=>setSearch(e.target.value)}/></label><label><span>Profissional</span><select value={employee} onChange={e=>setEmployee(e.target.value)}><option value="all">Todos os profissionais</option>{professionals.map(p=><option key={p.id} value={p.id}>{p.name}</option>)}</select></label></div>
    </div>
    <div className="ins-schedule-overview"><div><span>AGENDA DO DIA</span><h3>{dateText(date+"T12:00:00")}</h3><p>{dayItems.length} atendimentos na visualização</p></div><div className="ins-schedule-modes"><button className={view==="colunas"?"selected":""} onClick={()=>setView("colunas")}>Por profissional</button><button className={view==="lista"?"selected":""} onClick={()=>setView("lista")}>Lista</button></div></div>
    {view==="colunas"?<div className="ins-schedule-scroll" role="region" aria-label="Agenda em colunas por profissional" tabIndex={0}><div className="ins-schedule-grid">
      {cols.length===0?<p className="ins-schedule-empty">Nenhum profissional cadastrado para esta agenda.</p>:cols.map(p=>{
        const bookings=dayItems.filter(a=>(a.employeeId||"unassigned")===p.id);
        return <section className="ins-schedule-column" key={p.id}><header><h4>{p.name}</h4><span>{bookings.filter(a=>a.status==="SCHEDULED"||a.status==="CONFIRMED").length}/20 horários reservados</span></header><div className="ins-schedule-slots">{bookings.length?bookings.map(item):<p className="ins-schedule-empty">Sem atendimentos neste dia.</p>}</div></section>;
      })}
    </div></div>:<div className="ins-schedule-list">{dayItems.length?dayItems.map(a=><div className="ins-schedule-list-row" key={a.id}><time>{timeText(a.startsAt)}</time><div><strong>{a.clientName}</strong><small>{a.serviceName}</small></div><div><strong>{a.employeeName||"Sem profissional"}</strong><small>{statusText[a.status]}</small></div></div>):<p className="ins-schedule-empty">Nenhum atendimento encontrado neste dia.</p>}</div>}
    <p className="ins-schedule-note">Dados sincronizados com a agenda do estabelecimento. Cada coluna mostra os atendimentos reais do profissional; encaixes e alterações respeitam as regras do servidor.</p>
  </div>;
}
