import {useState,type FormEvent} from "react";
import {api,type Client} from "./production-api";
import "./client-finder.css";

type ClientMatch={
 source:"ESTABLISHMENT"|"AUTHORIZED_ACCOUNT";
 client_id:string|null;user_id:string|null;
 name:string;email:string|null;phone:string|null;document_number:string|null;rg:string|null;
 birth_date:string|null;postal_code:string|null;street:string|null;street_number:string|null;
 complement:string|null;neighborhood:string|null;city:string|null;state:string|null;
};
type Props={token:string;companyId:string;onLinked:()=>void;onSchedule:(id:string)=>void};
const errorText=(err:unknown)=>err instanceof Error?err.message:"Ocorreu um erro.";
const clean=(s:string|null)=>s?.trim()||"—";
export default function ClientFinder({token,companyId,onLinked,onSchedule}:Props){
 const [query,setQuery]=useState("");
 const [matches,setMatches]=useState<ClientMatch[]>([]);
 const [selected,setSelected]=useState<ClientMatch|null>(null);
 const [busy,setBusy]=useState(false);
 const [searched,setSearched]=useState(false);
 const [error,setError]=useState("");
 const [success,setSuccess]=useState("");
 const [manual,setManual]=useState(false);
 const [newClient,setNewClient]=useState({name:"",email:"",phone:"",documentNumber:"",rg:"",
  birthDate:"",postalCode:"",street:"",streetNumber:"",complement:"",neighborhood:"",
  city:"",state:"",notes:""});
 const root="/companies/"+companyId+"/clients";
 async function search(e:FormEvent){e.preventDefault();setBusy(true);setError("");setSelected(null);setSearched(false);
  try{const list=await api<ClientMatch[]>(root+"/lookup?query="+encodeURIComponent(query.trim()),token);setMatches(list);setSearched(true);}
  catch(e){setError(errorText(e));}finally{setBusy(false);}
 }
 async function inspect(match:ClientMatch){setError("");setSuccess("");
  if(match.source==="ESTABLISHMENT"&&match.client_id){
   setBusy(true);try{const full=await api<ClientMatch>(root+"/"+match.client_id+"/details",token);setSelected({...full,source:"ESTABLISHMENT"});}
   catch(e){setError(errorText(e));}finally{setBusy(false);}
  }else setSelected(match);
 }
 async function importProfile(){if(!selected?.user_id)return;setBusy(true);setError("");setSuccess("");
  try{
   const result=await api<ClientMatch>(root+"/import/"+selected.user_id,token,"POST");
   setSelected({...result,source:"ESTABLISHMENT"});setSuccess("Ficha autorizada vinculada ao estabelecimento.");onLinked();
   const items=await api<ClientMatch[]>(root+"/lookup?query="+encodeURIComponent(query.trim()),token);
   setMatches(items);
  }catch(e){setError(errorText(e));}finally{setBusy(false);}
 }
 async function create(e:FormEvent){e.preventDefault();setBusy(true);setError("");setSuccess("");
  try{
   await api<Client>(root,token,"POST",{...newClient,documentNumber:newClient.documentNumber.replace(/\D/g,""),
      birthDate:newClient.birthDate||null,state:newClient.state.toUpperCase()});
   setSuccess("Ficha administrativa cadastrada neste estabelecimento.");
   setManual(false);setNewClient({name:"",email:"",phone:"",documentNumber:"",rg:"",birthDate:"",
    postalCode:"",street:"",streetNumber:"",complement:"",neighborhood:"",city:"",state:"",notes:""});
   onLinked();
  }catch(e){setError(errorText(e));}finally{setBusy(false);}
 }
 const field=(label:string,key:keyof typeof newClient,type="text")=><label className="ins-finder-field">{label}
   <input type={type} value={newClient[key]} onChange={e=>setNewClient(v=>({...v,[key]:e.target.value}))}/></label>;
 return <div className="ins-finder">
  <section className="ins-finder-hero"><span>✳ ATENDIMENTO SEM RETRABALHO</span><h2>Encontre a pessoa. A ficha vem junto.</h2>
   <p>Pesquise por e-mail ou telefone. O Instrua busca os cadastros deste estabelecimento e as fichas que outras pessoas autorizaram compartilhar com ele.</p></section>
  <form className="ins-finder-search" onSubmit={search}>
   <label>Pesquisar cliente por e-mail ou telefone<input type="search" minLength={3} maxLength={100} required placeholder="Digite ao menos 3 caracteres" value={query} onChange={e=>setQuery(e.target.value)}/></label>
   <button type="submit" disabled={busy||query.trim().length<3}>{busy?"Buscando...":"Pesquisar ficha"}</button>
  </form>
  {error&&<p role="alert" className="ins-finder-alert error">{error}</p>}
  {success&&<p role="status" className="ins-finder-alert">{success}</p>}
  {searched&&matches.length===0&&<div className="ins-finder-empty">Nenhum cadastro correspondente nesta unidade e nenhuma ficha autorizada. Você pode solicitar à pessoa que autorize o compartilhamento em Configurações ou cadastrá-la manualmente.</div>}
  {matches.length>0&&<div className="ins-finder-results"><h3>Resultados autorizados ({matches.length})</h3>
   {matches.map((x,i)=><button type="button" className="ins-finder-result" key={x.client_id||x.user_id||i} onClick={()=>void inspect(x)}>
    <span><strong>{x.name}</strong><small>{clean(x.email)} · {clean(x.phone)}</small></span>
    <em>{x.source==="ESTABLISHMENT"?"Ficha da unidade":"Ficha compartilhada"} ↗</em>
   </button>)}
  </div>}
  {selected&&<section className="ins-finder-detail">
   <div className="ins-finder-detail-heading"><div><small>FICHA ADMINISTRATIVA</small><h3>{selected.name}</h3></div><button type="button" onClick={()=>setSelected(null)}>Fechar</button></div>
   <div className="ins-finder-detail-grid">
    <div><span>E-mail</span><strong>{clean(selected.email)}</strong></div>
    <div><span>Telefone</span><strong>{clean(selected.phone)}</strong></div>
    <div><span>CPF</span><strong>{clean(selected.document_number)}</strong></div>
    <div><span>RG</span><strong>{clean(selected.rg)}</strong></div>
    <div><span>Nascimento</span><strong>{clean(selected.birth_date)}</strong></div>
    <div><span>CEP</span><strong>{clean(selected.postal_code)}</strong></div>
    <div><span>Endereço</span><strong>{[selected.street,selected.street_number,selected.complement].filter(Boolean).join(", ")||"—"}</strong></div>
    <div><span>Bairro / Cidade / UF</span><strong>{[selected.neighborhood,selected.city,selected.state].filter(Boolean).join(" · ")||"—"}</strong></div>
   </div>
   <p className="ins-finder-privacy">Informações clínicas, alergias e prontuário não fazem parte desta busca. O acesso exige autorização clínica específica.</p>
   {selected.source==="AUTHORIZED_ACCOUNT"?<button type="button" className="ins-finder-primary" disabled={busy} onClick={()=>void importProfile()}>{busy?"Vinculando...":"Vincular ficha autorizada à unidade"}</button>:
    <button type="button" className="ins-finder-primary" disabled={!selected.client_id} onClick={()=>selected.client_id&&onSchedule(selected.client_id)}>Agendar atendimento para esta pessoa</button>}
  </section>}
  <section className="ins-finder-manual">
   <div><h3>Não encontrou a pessoa?</h3><p>Cadastre uma ficha administrativa completa somente quando necessário. Depois ela poderá ser usada nos agendamentos da unidade.</p></div>
   <button type="button" onClick={()=>setManual(v=>!v)}>{manual?"Fechar cadastro":"Cadastrar manualmente"}</button>
  </section>
  {manual&&<form onSubmit={create} className="ins-finder-manual-form">
   <div className="ins-finder-manual-grid">
    {field("Nome completo","name")}
    {field("E-mail","email","email")}
    {field("Telefone","phone","tel")}
    {field("CPF","documentNumber")}
    {field("RG","rg")}
    {field("Nascimento","birthDate","date")}
    {field("CEP","postalCode")}
    {field("Rua / logradouro","street")}
    {field("Número","streetNumber")}
    {field("Complemento","complement")}
    {field("Bairro","neighborhood")}
    {field("Cidade","city")}
    {field("Estado / UF","state")}
   </div>
   <label className="ins-finder-field">Observações administrativas<textarea maxLength={2000} rows={3} value={newClient.notes} onChange={e=>setNewClient(v=>({...v,notes:e.target.value}))}/></label>
   <button type="submit" className="ins-finder-primary" disabled={busy||newClient.name.trim().length<2}>{busy?"Salvando...":"Salvar ficha na unidade"}</button>
  </form>}
 </div>;
}
