import {useEffect,useMemo,useState} from "react";
import {api,type Appointment,type Session} from "./production-api";
import "./clinical-workspace.css";

type SoapFields={subjective:string;objective:string;assessment:string;plan:string};
type Draft=SoapFields&{id:string;encounterId:string;status:"DRAFT";updatedAt:string};
type EncounterSnapshot={
 appointmentId:string;clientName:string;patientName:string|null;serviceName:string;
 professionalName:string;startsAt:string;appointmentStatus:string;
 patientLinked:boolean;encounterId:string|null;encounterStatus?:string;soapDraft:Draft|null
};
const empty:SoapFields={subjective:"",objective:"",assessment:"",plan:""};
const sections:{key:keyof SoapFields;letter:string;name:string;hint:string;placeholder:string}[]=[
 {key:"subjective",letter:"S",name:"Subjetivo · Anamnese",hint:"Queixas, sintomas e informações relatadas pelo paciente",placeholder:"Registre aqui a história e as queixas relatadas durante o atendimento."},
 {key:"objective",letter:"O",name:"Objetivo · Avaliação",hint:"Achados observáveis e medidas verificadas",placeholder:"Registre somente os achados observados e exames efetivamente realizados."},
 {key:"assessment",letter:"A",name:"Avaliação profissional",hint:"Hipóteses e conclusões de responsabilidade do profissional",placeholder:"Descreva sua avaliação, diferenciando hipóteses de achados confirmados."},
 {key:"plan",letter:"P",name:"Plano e acompanhamento",hint:"Condutas, acompanhamento e recomendações estabelecidas",placeholder:"Registre o plano assistencial definido no atendimento."}
];
const time=(d:string)=>new Intl.DateTimeFormat("pt-BR",{dateStyle:"medium",timeStyle:"short"}).format(new Date(d));
const message=(e:unknown)=>e instanceof Error?e.message:"Não foi possível concluir a operação.";
function Icon({name}:{name:string}){return <span className="icw-icon material-symbols-outlined" aria-hidden="true">{name}</span>}

export default function ClinicalWorkspace({session,companyId,appointments}:{session:Session;companyId:string;appointments:Appointment[]}){
 const [selectedId,setSelectedId]=useState("");
 const [snapshot,setSnapshot]=useState<EncounterSnapshot|null>(null);
 const [fields,setFields]=useState<SoapFields>(empty);
 const [loading,setLoading]=useState(false);
 const [saving,setSaving]=useState(false);
 const [dirty,setDirty]=useState(false);
 const [error,setError]=useState("");
 const [notice,setNotice]=useState("");
 const [search,setSearch]=useState("");
 const [revision,setRevision]=useState(0);
 const relevant=useMemo(()=>[...appointments].sort((a,b)=>a.startsAt.localeCompare(b.startsAt)),[appointments]);
 const filtered=useMemo(()=>relevant.filter(a=>
   [a.clientName,a.serviceName,a.employeeName||""].some(t=>t.toLocaleLowerCase("pt-BR").includes(search.toLocaleLowerCase("pt-BR")))
 ),[relevant,search]);
 useEffect(()=>{
   setSelectedId(old=>relevant.some(a=>a.id===old)?old:(relevant[0]?.id||""));
 },[companyId,relevant]);
 useEffect(()=>{
   if(!selectedId){setSnapshot(null);setFields(empty);setError("");return;}
   let active=true;setLoading(true);setError("");setNotice("");
   void api<EncounterSnapshot>("/companies/"+companyId+"/clinical/appointments/"+selectedId,session.accessToken)
     .then(x=>{if(!active)return;setSnapshot(x);setFields(x.soapDraft?{
       subjective:x.soapDraft.subjective,objective:x.soapDraft.objective,
       assessment:x.soapDraft.assessment,plan:x.soapDraft.plan
     }:empty);setDirty(false);})
     .catch(e=>{if(active){setSnapshot(null);setFields(empty);setError(message(e));setDirty(false);}})
     .finally(()=>{if(active)setLoading(false);});
   return()=>{active=false;};
 },[companyId,selectedId,session.accessToken,revision]);
 function choose(id:string){
   if(id===selectedId)return;
   if(dirty&&!window.confirm("Existem anotações não salvas. Deseja trocar de atendimento e descartá-las?"))return;
   setSelectedId(id);
 }
 async function startEncounter(){
   if(!selectedId||saving||!snapshot?.patientLinked)return;
   setSaving(true);setError("");setNotice("");
   try{
     const saved=await api<EncounterSnapshot>("/companies/"+companyId+"/clinical/appointments/"+selectedId+"/encounter",
       session.accessToken,"POST");
     setSnapshot(saved);setNotice("Atendimento clínico iniciado no servidor.");
   }catch(e){setError(message(e));}finally{setSaving(false);}
 }
 async function save(){
   if(!snapshot?.encounterId||saving||!dirty)return;
   setSaving(true);setError("");setNotice("");
   try{
     const d=await api<Draft>("/companies/"+companyId+"/clinical/encounters/"+snapshot.encounterId+"/soap-draft",
       session.accessToken,"PUT",fields);
     setSnapshot(old=>old?{...old,soapDraft:d}:old);setDirty(false);
     setNotice("Rascunho SOAP salvo no servidor. Ainda não é um documento assinado.");
   }catch(e){setError(message(e));}finally{setSaving(false);}
 }
 function edit(key:keyof SoapFields,value:string){setFields(v=>({...v,[key]:value}));setDirty(true);setNotice("");}
 const selected=relevant.find(a=>a.id===selectedId);
 return <section className="icw-layout" aria-label="Área clínica restrita">
  <div className="icw-intro"><span className="icw-eyebrow"><Icon name="health_and_safety"/> ÁREA CLÍNICA RESTRITA</span><h1>Prontuário e evolução <em>com responsabilidade.</em></h1><p>Registre um rascunho SOAP para seus próprios atendimentos. Dados e acesso são verificados no servidor a cada operação.</p></div>
  <div className="icw-warning"><Icon name="shield_person"/><p>Área em implantação: a gravação do <strong>rascunho SOAP</strong> é real, mas assinatura ICP-Brasil, prescrição digital, anexos e sugestões clínicas por IA <strong>não estão habilitados</strong>. Não utilize para documentação assistencial definitiva enquanto não forem concluídas as verificações de segurança e conformidade.</p></div>
  <div className="icw-columns">
   <aside className="icw-patients" aria-label="Atendimentos sob sua responsabilidade">
     <div className="icw-patients-head"><span className="icw-eyebrow">MINHA AGENDA CLÍNICA</span><h2>Atendimentos</h2><span className="icw-total">{relevant.length}</span></div>
     <label className="icw-search"><Icon name="search"/><input placeholder="Buscar atendimento…" aria-label="Buscar atendimento" value={search} onChange={e=>setSearch(e.target.value)}/></label>
     {filtered.length===0?<p className="icw-empty">Nenhum atendimento atribuído ao seu perfil clínico.</p>:
     <div className="icw-patient-list">{filtered.map(a=><button type="button" key={a.id} onClick={()=>choose(a.id)} aria-current={a.id===selectedId?"true":undefined} className={a.id===selectedId?"active":""}>
       <span className="icw-list-icon"><Icon name="person"/></span><span><strong>{a.clientName}</strong><small>{a.serviceName}</small><time>{time(a.startsAt)}</time></span><Icon name="chevron_right"/></button>)}</div>}
   </aside>
   <div className="icw-workspace">
     {!selected?<div className="icw-panel icw-placeholder"><Icon name="clinical_notes"/><h2>Escolha um atendimento</h2><p>Seus agendamentos clínicos aparecerão aqui quando forem atribuídos à sua conta.</p></div>:<>
       <div className="icw-patient-banner">
         <div className="icw-identity"><span className="icw-circle"><Icon name="person"/></span><div><span className="icw-eyebrow">ATENDIMENTO SELECIONADO</span><h2>{snapshot?.patientName||selected.clientName}</h2><p>{selected.serviceName} · {time(selected.startsAt)}</p></div></div>
         <div className="icw-badges"><span><Icon name="schedule"/> {selected.status==="CONFIRMED"?"Confirmado":selected.status==="COMPLETED"?"Concluído":selected.status==="SCHEDULED"?"Agendado":selected.status}</span>{snapshot?.encounterId&&<span className="icw-draft"><Icon name="verified_user"/> Rascunho clínico</span>}</div>
       </div>
       {loading?<div role="status" className="icw-panel icw-loading">Carregando atendimento autorizado…</div>:<>
         {error&&<p role="alert" className="icw-alert">{error}</p>}
         {notice&&<p role="status" className="icw-success">{notice}</p>}
         {!snapshot?<div className="icw-panel"><h2>O acesso a este atendimento não foi liberado.</h2><p>Confirme seu vínculo clínico com o estabelecimento e com o profissional do agendamento.</p><button type="button" className="icw-secondary" onClick={()=>setRevision(n=>n+1)}>Tentar novamente</button></div>:
          <div className="icw-panel">
            <div className="icw-section-head"><div><span className="icw-eyebrow">REGISTRO CLÍNICO · SOAP</span><h2>Consulta atual e evolução</h2><p>O texto é escrito por você; nenhuma avaliação ou prescrição é gerada automaticamente.</p></div><span className="icw-draft-status">{snapshot.encounterId?"Rascunho em andamento":"Não iniciado"}</span></div>
            {!snapshot.encounterId?
             <div className="icw-init"><Icon name="folder_shared"/><div><strong>{snapshot.patientLinked?"Iniciar atendimento clínico":"Cadastro clínico ainda não vinculado"}</strong><p>{snapshot.patientLinked?"Ao iniciar, o Instrua cria um atendimento exclusivo para este agendamento e profissional.":"A recepção precisa vincular a identidade da pessoa ao cadastro clínico deste estabelecimento. Informações pessoais não são associadas apenas por nome, e-mail ou CPF."}</p></div><button type="button" disabled={!snapshot.patientLinked||saving} onClick={()=>void startEncounter()}>{saving?"Aguarde…":"Iniciar registro"}</button></div>:
             <>
               <div className="icw-soap-grid">{sections.map(item=><label key={item.key} className="icw-soap-field"><span className="icw-soap-title"><b>{item.letter}</b><strong>{item.name}</strong></span><small>{item.hint}</small><textarea rows={item.key==="subjective"?5:4} maxLength={10000} placeholder={item.placeholder} value={fields[item.key]} onChange={e=>edit(item.key,e.target.value)}/><span className="icw-char">{fields[item.key].length.toLocaleString("pt-BR")} / 10.000</span></label>)}</div>
               <div className="icw-save-bar"><div><strong>{dirty?"Alterações ainda não salvas":snapshot.soapDraft?"Rascunho sincronizado":"Nenhum rascunho gravado"}</strong><small>{snapshot.soapDraft?.updatedAt?"Última gravação: "+time(snapshot.soapDraft.updatedAt):"Somente o profissional responsável pode acessar e salvar"}</small></div><button type="button" className="icw-primary" disabled={!dirty||saving} onClick={()=>void save()}><Icon name="save"/>{saving?"Salvando…":"Salvar rascunho no servidor"}</button></div>
             </>}
          </div>}
         <div className="icw-next-modules">
           <article><span className="icw-soft-icon"><Icon name="biotech"/></span><div><h3>Exames e anexos</h3><p>Arquivos clínicos vinculados ao atendimento com autorização e armazenamento seguro.</p><small>Em desenvolvimento · Sem arquivos simulados</small></div></article>
           <article><span className="icw-soft-icon"><Icon name="medication"/></span><div><h3>Prescrições e assinatura</h3><p>Fluxo de receitas, documentos e assinatura eletrônica verificada, separado dos rascunhos.</p><small>Em desenvolvimento · Sem emissão ou assinatura fictícia</small></div></article>
           <article><span className="icw-soft-icon"><Icon name="auto_stories"/></span><div><h3>Guias e orientações</h3><p>Protocolos enviados pelo profissional ou recepção, depois de validação e vinculação ao paciente.</p><small>Em desenvolvimento · Nenhum disparo automático</small></div></article>
           <article><span className="icw-soft-icon"><Icon name="admin_panel_settings"/></span><div><h3>Histórico e auditoria</h3><p>O histórico de cada estabelecimento permanece isolado; não agregamos prontuários de outras clínicas nesta tela.</p><small>Rascunhos possuem registro de autoria e eventos de auditoria</small></div></article>
         </div>
       </>}
     </>}
   </div>
  </div>
 </section>;
}
