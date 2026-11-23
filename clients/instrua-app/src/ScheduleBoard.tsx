import {Fragment,useMemo,useState} from "react";
import type {Appointment,AppointmentStatus,Professional} from "./production-api";
import "./schedule-board.css";

type View="day"|"week"|"month";
type Column={id:string;name:string};
const statusText:Record<AppointmentStatus,string>={SCHEDULED:"Agendado",CONFIRMED:"Confirmado",COMPLETED:"Concluído",CANCELLED:"Cancelado",NO_SHOW:"Não compareceu"};
const dateInput=(d:Date)=>[d.getFullYear(),String(d.getMonth()+1).padStart(2,"0"),String(d.getDate()).padStart(2,"0")].join("-");
const localDate=(day:string)=>new Date(day+"T12:00:00");
const addDays=(day:string,amount:number)=>{const d=localDate(day);d.setDate(d.getDate()+amount);return dateInput(d);};
const startOfWeek=(day:string)=>addDays(day,-((localDate(day).getDay()+6)%7));
const startOfMonth=(day:string)=>day.slice(0,7)+"-01";
const addMonths=(day:string,amount:number)=>{const d=localDate(startOfMonth(day));d.setMonth(d.getMonth()+amount);return dateInput(d);};
const appointmentDay=(date:string)=>dateInput(new Date(date));
const minutesAt=(date:string)=>{const d=new Date(date);return d.getHours()*60+d.getMinutes();};
const clockAt=(minutes:number)=>String(Math.floor(minutes/60)).padStart(2,"0")+":"+String(minutes%60).padStart(2,"0");
const format=(day:string,options:Intl.DateTimeFormatOptions)=>new Intl.DateTimeFormat("pt-BR",options).format(localDate(day));
const timeText=(date:string)=>new Intl.DateTimeFormat("pt-BR",{hour:"2-digit",minute:"2-digit"}).format(new Date(date));
const active=(a:Appointment)=>a.status==="SCHEDULED"||a.status==="CONFIRMED";

type Props={appointments:Appointment[];professionals:Professional[];canManage:boolean;busy:boolean;onStatus?:(id:string,status:AppointmentStatus)=>void};

export default function ScheduleBoard({appointments,professionals,canManage,busy,onStatus}:Props){
  const [date,setDate]=useState(()=>dateInput(new Date()));
  const [search,setSearch]=useState("");
  const [view,setView]=useState<View>("week");
  const [employee,setEmployee]=useState("all");
  const weekStart=startOfWeek(date);
  const monthStart=startOfMonth(date);
  const visible=useMemo(()=>appointments.filter(a=>
    (employee==="all"||a.employeeId===employee)&&
    (a.clientName+" "+a.serviceName+" "+(a.employeeName||"")).toLocaleLowerCase("pt-BR").includes(search.toLocaleLowerCase("pt-BR"))
  ),[appointments,employee,search]);
  const period=useMemo(()=>visible.filter(a=>{
    const d=appointmentDay(a.startsAt);
    return view==="day"?d===date:view==="week"?d>=weekStart&&d<addDays(weekStart,7):d.startsWith(monthStart.slice(0,7));
  }).sort((a,b)=>new Date(a.startsAt).getTime()-new Date(b.startsAt).getTime()),[visible,view,date,weekStart,monthStart]);
  const byDay=useMemo(()=>{
    const result=new Map<string,Appointment[]>();
    for(const a of period){const d=appointmentDay(a.startsAt);result.set(d,[...(result.get(d)||[]),a]);}
    return result;
  },[period]);
  const columns=useMemo(()=>{
    const seen=new Set<string>();const result:Column[]=[];
    for(const p of professionals){
      if(employee!=="all"&&employee!==p.id)continue;
      seen.add(p.id);result.push({id:p.id,name:p.name});
    }
    for(const a of byDay.get(date)||[]){
      const id=a.employeeId||"unassigned";
      if(!seen.has(id)){seen.add(id);result.push({id,name:a.employeeName||"Sem profissional"});}
    }
    return result.length?result:[{id:"unassigned",name:"Sem profissional cadastrado"}];
  },[professionals,employee,byDay,date]);
  const slots=useMemo(()=>{
    let first=7*60,last=20*60;
    for(const a of period){
      first=Math.min(first,Math.floor(minutesAt(a.startsAt)/30)*30);
      last=Math.max(last,Math.ceil(minutesAt(a.endsAt)/30)*30);
    }
    return Array.from({length:Math.max(1,Math.ceil((last-first)/30))},(_,i)=>first+i*30);
  },[period]);
  const monthDays=useMemo(()=>{
    const start=startOfWeek(monthStart);
    return Array.from({length:42},(_,i)=>addDays(start,i));
  },[monthStart]);
  const heading=view==="day"?format(date,{weekday:"long",day:"numeric",month:"long",year:"numeric"}):view==="week"?format(weekStart,{day:"2-digit",month:"short"})+" – "+format(addDays(weekStart,6),{day:"2-digit",month:"short",year:"numeric"}):format(monthStart,{month:"long",year:"numeric"});
  const move=(direction:number)=>setDate(d=>view==="day"?addDays(d,direction):view==="week"?addDays(d,7*direction):addMonths(d,direction));
  const chooseDay=(day:string)=>{setDate(day);setView("day");};
  const startingSlot=(a:Appointment,day:string,slot:number)=>appointmentDay(a.startsAt)===day&&Math.floor(minutesAt(a.startsAt)/30)*30===slot;

  function eventCard(a:Appointment,showProfessional:boolean){
    return <article key={a.id} className={"ins-booking-card ins-booking-"+a.status.toLowerCase()}>
      <div className="ins-booking-time"><time dateTime={a.startsAt}>{timeText(a.startsAt)}</time><span>{statusText[a.status]}</span></div>
      <strong>{a.clientName}</strong><p>{a.serviceName}</p>
      {showProfessional&&<small>{a.employeeName||"Sem profissional"} · </small>}
      <small>{timeText(a.startsAt)}–{timeText(a.endsAt)}</small>
      {canManage&&active(a)&&onStatus&&<div className="ins-booking-actions">
        {a.status==="SCHEDULED"&&<button type="button" disabled={busy} onClick={()=>onStatus(a.id,"CONFIRMED")}>Confirmar</button>}
        <button type="button" disabled={busy} onClick={()=>onStatus(a.id,"COMPLETED")}>Concluir</button>
      </div>}
    </article>;
  }

  return <div className="ins-schedule">
    <div className="ins-schedule-tools">
      <div className="ins-schedule-day">
        <button type="button" aria-label="Período anterior" onClick={()=>move(-1)}>‹</button>
        <label><span>Ir para uma data</span><input type="date" value={date} onChange={e=>e.target.value&&setDate(e.target.value)}/></label>
        <button type="button" aria-label="Próximo período" onClick={()=>move(1)}>›</button>
        <button type="button" className="ins-schedule-today" onClick={()=>setDate(dateInput(new Date()))}>Hoje</button>
      </div>
      <div className="ins-schedule-filters">
        <label><span>Buscar paciente ou procedimento</span><input type="search" value={search} placeholder="Pesquisar na agenda" onChange={e=>setSearch(e.target.value)}/></label>
        <label><span>Profissional</span><select value={employee} onChange={e=>setEmployee(e.target.value)}>
          <option value="all">Todos os profissionais</option>
          {professionals.map(p=><option key={p.id} value={p.id}>{p.name}</option>)}
        </select></label>
      </div>
    </div>
    <div className="ins-schedule-overview">
      <div><span>AGENDA INTELIGENTE · {view==="day"?"DIA":view==="week"?"SEMANA":"MÊS"}</span>
        <h3>{heading}</h3><p>{period.length} atendimento{period.length===1?"":"s"} no período</p></div>
      <div className="ins-schedule-modes" role="group" aria-label="Visualizar agenda">
        <button type="button" aria-pressed={view==="day"} className={view==="day"?"selected":""} onClick={()=>setView("day")}>Dia</button>
        <button type="button" aria-pressed={view==="week"} className={view==="week"?"selected":""} onClick={()=>setView("week")}>Semana</button>
        <button type="button" aria-pressed={view==="month"} className={view==="month"?"selected":""} onClick={()=>setView("month")}>Mês</button>
      </div>
    </div>

    {view==="month"?<div className="ins-cal-scroll" role="region" aria-label="Calendário do mês" tabIndex={0}>
      <div className="ins-cal-month">
        {["Seg","Ter","Qua","Qui","Sex","Sáb","Dom"].map(day=><div className="ins-cal-month-heading" key={day}>{day}</div>)}
        {monthDays.map(day=>{
          const bookings=byDay.get(day)||[];
          const current=day.slice(0,7)===monthStart.slice(0,7);
          return <button key={day} type="button" className={"ins-cal-month-day"+(!current?" outside":"")+(day===dateInput(new Date())?" today":"")} onClick={()=>chooseDay(day)} aria-label={"Ver dia "+format(day,{day:"numeric",month:"long"})}>
            <span className="ins-cal-day-number">{localDate(day).getDate()}</span>
            {bookings.slice(0,3).map(a=><span className={"ins-cal-month-event "+a.status.toLowerCase()} key={a.id}>{timeText(a.startsAt)} · {a.clientName}</span>)}
            {bookings.length>3&&<span className="ins-cal-more">+{bookings.length-3} atendimentos</span>}
          </button>;
        })}
      </div>
    </div>:<div className="ins-cal-scroll" role="region" aria-label={view==="week"?"Agenda semanal com horários":"Agenda do dia por profissional"} tabIndex={0}>
      <div className="ins-cal-board" style={{gridTemplateColumns:"72px repeat("+(view==="day"?columns.length:7)+",minmax("+(view==="day"?"230":"175")+"px,1fr))"}}>
        <div className="ins-cal-corner">Horário</div>
        {view==="day"?columns.map(p=><div className="ins-cal-colheading" key={p.id}><strong>{p.name}</strong><small>{period.filter(a=>(a.employeeId||"unassigned")===p.id&&active(a)).length} agendado(s)</small></div>)
          :Array.from({length:7},(_,i)=>addDays(weekStart,i)).map(day=><button type="button" className={"ins-cal-colheading ins-cal-heading-button"+(day===dateInput(new Date())?" today":"")} key={day} onClick={()=>chooseDay(day)}><strong>{format(day,{weekday:"short"})}</strong><small>{format(day,{day:"2-digit",month:"short"})}</small></button>)}
        {slots.map(slot=><Fragment key={slot}>
          <div className="ins-cal-clock">{clockAt(slot)}</div>
          {view==="day"?columns.map(p=><div className="ins-cal-cell" key={p.id+"-"+slot}>
            {(byDay.get(date)||[]).filter(a=>(a.employeeId||"unassigned")===p.id&&startingSlot(a,date,slot)).map(a=>eventCard(a,false))}
          </div>):Array.from({length:7},(_,i)=>addDays(weekStart,i)).map(day=><div className="ins-cal-cell" key={day+"-"+slot}>
            {(byDay.get(day)||[]).filter(a=>startingSlot(a,day,slot)).map(a=>eventCard(a,true))}
          </div>)}
        </Fragment>)}
      </div>
    </div>}
    <p className="ins-schedule-note">O calendário mostra os horários mesmo sem agendamentos. Exibe somente registros fornecidos pelo servidor e respeita as permissões da conta. Horários disponíveis ainda precisam ser confirmados pelas regras de cada profissional e unidade.</p>
  </div>;
}
