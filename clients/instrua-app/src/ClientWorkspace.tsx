import {useEffect,useState,type FormEvent} from "react";
import {api,type Client,type Session} from "./production-api";
import "./client-workspace.css";

type ClientDetails=Client&{documentNumber?:string|null;notes?:string|null;rg?:string|null;birthDate?:string|null;
 postalCode?:string|null;street?:string|null;streetNumber?:string|null;complement?:string|null;
 neighborhood?:string|null;city?:string|null;state?:string|null;linkedUserId?:string|null};
type Match={id?:string;user_id?:string;source:"LOCAL"|"SHARED";name:string;email?:string;phone?:string;
 document_number?:string;postal_code?:string;street?:string;city?:string};
type FormData={name:string;email:string;phone:string;documentNumber:string;notes:string;rg:string;birthDate:string;
 postalCode:string;street:string;streetNumber:string;complement:string;neighborhood:string;city:string;state:string};
const initial:FormData={name:"",email:"",phone:"",documentNumber:"",notes:"",rg:"",birthDate:"",postalCode:"",
 street:"",streetNumber:"",complement:"",neighborhood:"",city:"",state:""};
const format=(v:ClientDetails):FormData=>Object.fromEntries(Object.keys(initial).map(k=>[k,String((v as unknown as Record<string,unknown>)[k]??"")])) as FormData;
const message=(e:unknown)=>e instanceof Error?e.message:"Não foi possível concluir.";
export default function ClientWorkspace({session,companyId,clients,onChanged,onSchedule}:{session:Session;companyId:string;clients:Client[];onChanged:()=>void;onSchedule:(clientId:string)=>void}){
 const [email,setEmail]=useState("");const [phone,setPhone]=useState("");
 const [matches,setMatches]=useState<Match[]>([]);
 const [mode,setMode]=useState<"lookup"|"edit">("lookup");
 const [selectedId,setSelectedId]=useState("");
 const [form,setForm]=useState<FormData>(initial);
 const [busy,setBusy]=useState(false);
 const [error,setError]=useState("");const [notice,setNotice]=useState("");
 useEffect(()=>{setMode("lookup");setMatches([]);setSelectedId("");setForm(initial);setEmail("");setPhone("");setError("");setNotice("");},[companyId]);
 const base="/companies/"+companyId+"/clients";
 const edit=(key:keyof FormData,value:string)=>setForm(s=>({...s,[key]:value}));
 async function lookup(e:FormEvent){
   e.preventDefault();if(busy)return;setBusy(true);setError("");setNotice("");
   try{
     const q=new URLSearchParams();
     if(email.trim())q.set("email",email.trim());
     else if(phone.trim())q.set("phone",phone.trim());
     const found=await api<Match[]>(base+"/lookup?"+q.toString(),session.accessToken);
     setMatches(found);setNotice(found.length?found.length+" resultado(s) encontrado(s).":"Nenhuma ficha encontrada para esse contato neste estabelecimento. Você pode cadastrar uma nova.");
   }catch(e){setError(message(e));}finally{setBusy(false);}
 }
 function startNew(){setForm(initial);setSelectedId("");setMode("edit");setError("");setNotice("");}
 async function editExisting(id:string){
   if(busy)return;setBusy(true);setError("");
   try{const result=await api<ClientDetails>(base+"/"+id,session.accessToken);setForm(format(result));setSelectedId(id);setMode("edit");}
   catch(e){setError(message(e));}finally{setBusy(false);}
 }
 async function importShared(userId:string){
   if(busy)return;setBusy(true);setError("");setNotice("");
   try{
     const data=await api<ClientDetails>(base+"/import/"+userId,session.accessToken,"POST");
     setSelectedId(data.id);setForm(format(data));setMode("edit");setNotice("Ficha autorizada vinculada. Confira os dados antes de usar no agendamento.");onChanged();
   }catch(e){setError(message(e));}finally{setBusy(false);}
 }
 async function save(e:FormEvent){
   e.preventDefault();if(busy)return;setBusy(true);setError("");setNotice("");
   try{
     const result=await api<ClientDetails>(base+(selectedId?"/"+selectedId:""),session.accessToken,selectedId?"PUT":"POST",{
       ...form, birthDate:form.birthDate||null,
       email:form.email.trim()||null,phone:form.phone.trim()||null,
       documentNumber:form.documentNumber.trim()||null,rg:form.rg.trim()||null,
       postalCode:form.postalCode.replace(/[^0-9]/g,"")
     });
     setSelectedId(result.id);setNotice("Ficha salva no estabelecimento. Os dados já estão disponíveis na agenda.");onChanged();
   }catch(e){setError(message(e));}finally{setBusy(false);}
 }
 return <section className="ins-clients">
   <header className="ins-clients-hero"><span className="real-eyebrow">RECEPÇÃO · ATENDIMENTO</span><h2>Clientes e fichas</h2>
     <p>Procure um cliente que já existe antes de criar outro cadastro. Cada ficha pertence ao estabelecimento e respeita as permissões de acesso.</p>
     <div><strong>{clients.length}</strong><small>fichas deste estabelecimento</small></div>
   </header>
   {error&&<p className="ins-clients-error" role="alert">{error}</p>}
   {notice&&<p className="ins-clients-success" role="status">{notice}</p>}
   {mode==="lookup"?<div className="ins-clients-cols">
     <section className="real-panel">
       <h3>Encontrar ficha</h3><p>Digite o e-mail ou o telefone completo com DDD.</p>
       <form className="real-form" onSubmit={lookup}>
         <label>E-mail<input type="email" autoComplete="off" value={email} onChange={e=>{setEmail(e.target.value);if(e.target.value)setPhone("");}} placeholder="pessoa@exemplo.com"/></label>
         <span className="ins-clients-or">ou</span>
         <label>Telefone com DDD<input type="tel" autoComplete="off" value={phone} onChange={e=>{setPhone(e.target.value);if(e.target.value)setEmail("");}} placeholder="(81) 99999-9999"/></label>
         <button type="submit" className="real-primary" disabled={busy||(!email.trim()&&!phone.trim())}>{busy?"Procurando...":"Buscar cliente"}</button>
       </form>
       <div className="ins-clients-new"><p>O cliente ainda não tem cadastro?</p><button type="button" className="real-secondary" onClick={startNew}>Criar ficha manualmente</button></div>
       <p className="real-small">A busca por uma conta Instrua só retorna dados pessoais quando o titular autorizou este estabelecimento. Dados clínicos privados nunca aparecem nesta busca.</p>
     </section>
     <section className="real-panel">
       <h3>Resultados</h3>
       {matches.length>0?<div className="ins-clients-results">{matches.map((m,i)=><article key={(m.id||m.user_id||"")+i}>
         <div><strong>{m.name}</strong><small>{m.email||m.phone||"Sem contato"}</small><span>{m.source==="LOCAL"?"Ficha do estabelecimento":"Compartilhada com autorização"}</span></div>
         {m.source==="LOCAL"&&m.id?<button className="real-secondary" onClick={()=>void editExisting(m.id!)} disabled={busy}>Abrir ficha</button>:
           m.user_id?<button className="real-primary" onClick={()=>void importShared(m.user_id!)} disabled={busy}>Vincular ficha</button>:null}
       </article>)}</div>:<p className="ins-clients-empty">Os resultados da busca vão aparecer aqui. Você também pode escolher uma ficha já cadastrada abaixo.</p>}
       <h4>Fichas já vinculadas</h4>
       <div className="ins-clients-existing">{clients.length===0?<p className="ins-clients-empty">Ainda não há fichas cadastradas.</p>:clients.map(c=><button key={c.id} type="button" onClick={()=>void editExisting(c.id)} disabled={busy}><strong>{c.name}</strong><small>{c.phone||c.email||"Sem contato"}</small><span>Ver ficha →</span></button>)}</div>
     </section>
   </div>:<section className="real-panel ins-clients-form-card">
     <div className="ins-clients-form-top"><div><span className="real-eyebrow">{selectedId?"FICHA DO ESTABELECIMENTO":"NOVO CADASTRO"}</span><h3>{selectedId?"Editar ficha":"Cadastrar cliente"}</h3></div><button className="real-secondary" type="button" onClick={()=>{setMode("lookup");setNotice("");}}>← Voltar à busca</button></div>
     <form className="ins-clients-form" onSubmit={save}>
       <h4>Identificação</h4><div className="ins-clients-fields">
         <label className="wide">Nome completo<input required maxLength={255} autoComplete="name" value={form.name} onChange={e=>edit("name",e.target.value)}/></label>
         <label>CPF<input maxLength={100} value={form.documentNumber} onChange={e=>edit("documentNumber",e.target.value)}/></label>
         <label>RG<input maxLength={30} value={form.rg} onChange={e=>edit("rg",e.target.value)}/></label>
         <label>Data de nascimento<input type="date" value={form.birthDate} onChange={e=>edit("birthDate",e.target.value)}/></label>
       </div>
       <h4>Contato</h4><div className="ins-clients-fields">
         <label>E-mail<input type="email" value={form.email} onChange={e=>edit("email",e.target.value)}/></label>
         <label>Telefone com DDD<input type="tel" maxLength={60} value={form.phone} onChange={e=>edit("phone",e.target.value)}/></label>
       </div>
       <h4>Endereço para atendimento e documentos fiscais</h4><div className="ins-clients-fields">
         <label>CEP<input inputMode="numeric" maxLength={12} value={form.postalCode} onChange={e=>edit("postalCode",e.target.value)}/></label>
         <label>UF<input maxLength={2} value={form.state} onChange={e=>edit("state",e.target.value.toUpperCase())}/></label>
         <label className="wide">Rua ou logradouro<input maxLength={255} value={form.street} onChange={e=>edit("street",e.target.value)}/></label>
         <label>Número<input maxLength={30} value={form.streetNumber} onChange={e=>edit("streetNumber",e.target.value)}/></label>
         <label>Complemento<input maxLength={150} value={form.complement} onChange={e=>edit("complement",e.target.value)}/></label>
         <label>Bairro<input maxLength={150} value={form.neighborhood} onChange={e=>edit("neighborhood",e.target.value)}/></label>
         <label>Cidade<input maxLength={150} value={form.city} onChange={e=>edit("city",e.target.value)}/></label>
       </div>
       <h4>Observações administrativas</h4>
       <label>Orientações administrativas <textarea rows={3} maxLength={4000} value={form.notes} onChange={e=>edit("notes",e.target.value)} placeholder="Somente informações necessárias ao atendimento. Não inclua prontuário médico."/></label>
       <p className="real-small">Esta é uma ficha operacional do estabelecimento, não um prontuário clínico. A emissão de nota fiscal ainda requer integração fiscal e informações do prestador.</p>
       <button className="real-primary" type="submit" disabled={busy}>{busy?"Salvando...":selectedId?"Salvar alterações":"Salvar ficha"}</button>
       {selectedId&&<button type="button" className="real-secondary" disabled={busy} onClick={()=>onSchedule(selectedId)}>Agendar atendimento com esta ficha →</button>}
     </form>
   </section>}
 </section>;
}
