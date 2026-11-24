import {useEffect,useState,type FormEvent} from "react";
import {api,type CatalogCompany,type Session} from "./production-api";
import "./profile-settings.css";

export type Appearance={theme:"LIGHT"|"DARK";fontScale:number;highContrast:boolean};
export type Profile={
 user_id:string;name:string;email:string;phone:string|null;document_number:string|null;
 birth_date:string|null;preferred_contact_channel:string|null;rg:string|null;
 postal_code:string|null;street:string|null;street_number:string|null;complement:string|null;
 neighborhood:string|null;city:string|null;state:string|null;
 emergency_contact_name:string|null;emergency_contact_phone:string|null;
 theme:"LIGHT"|"DARK";font_scale:number;high_contrast:boolean;
};
type FormData={
 fullName:string;phone:string;documentNumber:string;birthDate:string;preferredContactChannel:string;
 rg:string;postalCode:string;street:string;streetNumber:string;complement:string;neighborhood:string;
 city:string;state:string;emergencyContactName:string;emergencyContactPhone:string;
 theme:"LIGHT"|"DARK";fontScale:number;highContrast:boolean;
};
const empty:FormData={fullName:"",phone:"",documentNumber:"",birthDate:"",preferredContactChannel:"EMAIL",
 rg:"",postalCode:"",street:"",streetNumber:"",complement:"",neighborhood:"",city:"",state:"",
 emergencyContactName:"",emergencyContactPhone:"",theme:"LIGHT",fontScale:1,highContrast:false};
const normalize=(p:Profile):FormData=>({
 fullName:p.name||"",phone:p.phone||"",documentNumber:p.document_number||"",
 birthDate:p.birth_date||"",preferredContactChannel:p.preferred_contact_channel||"EMAIL",rg:p.rg||"",
 postalCode:p.postal_code||"",street:p.street||"",streetNumber:p.street_number||"",
 complement:p.complement||"",neighborhood:p.neighborhood||"",city:p.city||"",
 state:p.state||"",emergencyContactName:p.emergency_contact_name||"",
 emergencyContactPhone:p.emergency_contact_phone||"",theme:p.theme||"LIGHT",
 fontScale:Number(p.font_scale)||1,highContrast:!!p.high_contrast
});
type Props={session:Session;view:"dados"|"acessibilidade"|"privacidade"|"saude";
 onAppearance?:(value:Appearance)=>void;onNameChange?:(name:string)=>void};
const say=(e:unknown)=>e instanceof Error?e.message:"Não foi possível concluir a operação.";
export default function ProfileSettings({session,view,onAppearance,onNameChange}:Props){
 const [form,setForm]=useState<FormData>(empty);
 const [loading,setLoading]=useState(true);
 const [saving,setSaving]=useState(false);
 const [error,setError]=useState("");
 const [success,setSuccess]=useState("");
 const [health,setHealth]=useState({allergies:"",heightCm:""});
 const [catalog,setCatalog]=useState<CatalogCompany[]>([]);
 const [shares,setShares]=useState<{company_id:string;company_name:string;permitted:boolean}[]>([]);
 const [shareCompany,setShareCompany]=useState("");
 useEffect(()=>{let mounted=true;setLoading(true);
  void api<Profile>("/me/profile",session.accessToken).then(profile=>{
   if(mounted){setForm(normalize(profile));setLoading(false);onAppearance?.({theme:profile.theme||"LIGHT",fontScale:Number(profile.font_scale)||1,highContrast:!!profile.high_contrast});}
  }).catch(e=>{if(mounted){setError(say(e));setLoading(false);}});
  return()=>{mounted=false;};
 },[session.accessToken]);
 useEffect(()=>{if(view!=="saude")return;let mounted=true;
  void api<{allergies:string;heightCm:string|number}>("/me/profile/health",session.accessToken).then(v=>{
    if(mounted)setHealth({allergies:String(v.allergies||""),heightCm:v.heightCm?String(v.heightCm):""});
  }).catch(e=>{if(mounted)setError(say(e));});return()=>{mounted=false;};
 },[view,session.accessToken]);
 useEffect(()=>{if(view!=="privacidade")return;let mounted=true;
  void Promise.all([api<CatalogCompany[]>("/catalog",session.accessToken),
                    api<{company_id:string;company_name:string;permitted:boolean}[]>("/me/profile/shares",session.accessToken)])
   .then(([c,s])=>{if(mounted){setCatalog(c);setShares(s);setShareCompany(c[0]?.id||"");}})
   .catch(e=>{if(mounted)setError(say(e));});
  return()=>{mounted=false;};
 },[view,session.accessToken]);
 const field=(key:keyof FormData)=>(value:string)=>setForm(f=>({...f,[key]:value}));
 const fieldRow=(label:string,key:keyof FormData,opts:{type?:string;maxLength?:number;placeholder?:string;autoComplete?:string}={})=>
  <label className="ins-profile-field"><span>{label}</span><input type={opts.type||"text"} maxLength={opts.maxLength} placeholder={opts.placeholder}
    autoComplete={opts.autoComplete} value={String(form[key]??"")} onChange={e=>field(key)(e.target.value)}/></label>;
 const save=async(e:FormEvent)=>{e.preventDefault();if(saving)return;setSaving(true);setError("");setSuccess("");
  try{
   const payload={...form,fullName:form.fullName.trim(),documentNumber:form.documentNumber.replace(/\D/g,""),birthDate:form.birthDate||null,
    preferredContactChannel:form.preferredContactChannel||"NONE",fontScale:Number(form.fontScale),highContrast:!!form.highContrast,
    state:form.state.trim().toUpperCase()};
   const profile=await api<Profile>("/me/profile",session.accessToken,"PUT",payload);
   setForm(normalize(profile));onNameChange?.(profile.name);
   onAppearance?.({theme:profile.theme,fontScale:Number(profile.font_scale),highContrast:!!profile.high_contrast});
   setSuccess("Seus dados foram salvos no Instrua.");
  }catch(e){setError(say(e));}finally{setSaving(false);}
 };
 const saveHealth=async(e:FormEvent)=>{e.preventDefault();setSaving(true);setError("");setSuccess("");
  try{await api("/me/profile/health",session.accessToken,"PUT",{allergies:health.allergies,heightCm:health.heightCm===""?null:Number(health.heightCm)});
   setSuccess("Suas anotações privadas foram salvas.");}catch(e){setError(say(e));}finally{setSaving(false);}
 };
 const toggleShare=async(companyId:string,permitted:boolean)=>{
  setSaving(true);setError("");setSuccess("");
  try{await api("/me/profile/shares/"+companyId,session.accessToken,"PUT",{permitted});
   const updated=await api<typeof shares>("/me/profile/shares",session.accessToken);
   setShares(updated);setSuccess(permitted?"Você autorizou o compartilhamento dos dados administrativos com esse estabelecimento.":"Autorização revogada.");
  }catch(e){setError(say(e));}finally{setSaving(false);}
 };
 return <div className="ins-profile-settings">
  {loading&&<p role="status">Carregando sua ficha segura...</p>}
  {error&&<p className="ins-profile-alert error" role="alert">{error}</p>}
  {success&&<p className="ins-profile-alert success" role="status">{success}</p>}
  {!loading&&view==="dados"&&<form onSubmit={save} className="ins-profile-form">
    <div className="ins-profile-section"><h4>Identificação e contato</h4><p>Preencha uma vez e mantenha seus dados atualizados.</p>
      <div className="ins-profile-grid">
       {fieldRow("Nome completo","fullName",{maxLength:255,autoComplete:"name"})}
       <label className="ins-profile-field"><span>E-mail da conta</span><input value={session.email} disabled readOnly/><small>Para alterar o e-mail, será necessária uma verificação de segurança.</small></label>
       {fieldRow("Telefone / WhatsApp","phone",{maxLength:60,type:"tel",autoComplete:"tel"})}
       {fieldRow("CPF (11 dígitos)","documentNumber",{maxLength:14,placeholder:"Somente números"})}
       {fieldRow("RG","rg",{maxLength:30})}
       {fieldRow("Data de nascimento","birthDate",{type:"date"})}
       <label className="ins-profile-field"><span>Canal preferido</span><select value={form.preferredContactChannel} onChange={e=>field("preferredContactChannel")(e.target.value)}>
         <option value="EMAIL">E-mail</option><option value="WHATSAPP">WhatsApp</option><option value="SMS">SMS</option><option value="NONE">Não quero contato promocional</option>
       </select></label>
      </div>
    </div>
    <div className="ins-profile-section"><h4>Endereço e dados para cadastro</h4>
      <div className="ins-profile-grid">
       {fieldRow("CEP","postalCode",{maxLength:12,autoComplete:"postal-code"})}
       {fieldRow("Rua / logradouro","street",{maxLength:255,autoComplete:"address-line1"})}
       {fieldRow("Número","streetNumber",{maxLength:30})}
       {fieldRow("Complemento","complement",{maxLength:150,autoComplete:"address-line2"})}
       {fieldRow("Bairro","neighborhood",{maxLength:150})}
       {fieldRow("Cidade","city",{maxLength:150,autoComplete:"address-level2"})}
       {fieldRow("Estado (UF)","state",{maxLength:2,autoComplete:"address-level1"})}
      </div>
    </div>
    <div className="ins-profile-section"><h4>Contato para emergências</h4>
      <div className="ins-profile-grid">
        {fieldRow("Nome do contato","emergencyContactName",{maxLength:255})}
        {fieldRow("Telefone de emergência","emergencyContactPhone",{maxLength:60,type:"tel"})}
      </div>
    </div>
    <p className="ins-profile-privacy">Esses dados ficam na sua conta. Estabelecimentos não podem consultar sua ficha global somente por conhecerem seu telefone ou e-mail. Você decide quando compartilhar os dados administrativos.</p>
    <button className="ins-profile-save" disabled={saving||form.fullName.trim().length<2} type="submit">{saving?"Salvando...":"Salvar minha ficha"}</button>
  </form>}
  {!loading&&view==="acessibilidade"&&<form onSubmit={save} className="ins-profile-form">
    <div className="ins-profile-section"><h4>Deixe o Instrua do seu jeito</h4><p>Suas preferências ficam vinculadas à conta e podem ser usadas no site e no aplicativo.</p>
      <div className="ins-profile-grid">
       <label className="ins-profile-field"><span>Tema do Instrua</span><select value={form.theme} onChange={e=>setForm(f=>({...f,theme:e.target.value as "LIGHT"|"DARK"}))}><option value="LIGHT">Claro · creme e sálvia</option><option value="DARK">Escuro · verde profundo</option></select></label>
       <label className="ins-profile-field"><span>Tamanho das letras: {Math.round(form.fontScale*100)}%</span><input type="range" min="0.9" max="1.4" step="0.1" value={form.fontScale} onChange={e=>setForm(f=>({...f,fontScale:Number(e.target.value)}))}/></label>
       <label className="ins-profile-check"><input type="checkbox" checked={form.highContrast} onChange={e=>setForm(f=>({...f,highContrast:e.target.checked}))}/><span>Ativar alto contraste</span></label>
      </div>
      <p className="ins-profile-privacy">Os controles aumentam a legibilidade e preservam a organização das páginas. Use também o zoom do navegador e os recursos nativos de acessibilidade do celular.</p>
    </div>
    <button className="ins-profile-save" disabled={saving} type="submit">{saving?"Salvando...":"Aplicar e salvar aparência"}</button>
  </form>}
  {view==="saude"&&<form onSubmit={saveHealth} className="ins-profile-form">
   <div className="ins-profile-section"><h4>Anotações privadas de saúde</h4><p>Informações opcionais, visíveis apenas na sua conta nesta versão. Não são prontuário médico nem são compartilhadas pela busca de clientes.</p>
     <label className="ins-profile-field"><span>Alergias que você deseja registrar</span><textarea rows={4} maxLength={2000} value={health.allergies} onChange={e=>setHealth(h=>({...h,allergies:e.target.value}))}/></label>
     <label className="ins-profile-field"><span>Altura (cm, opcional)</span><input type="number" min="20" max="300" step=".1" value={health.heightCm} onChange={e=>setHealth(h=>({...h,heightCm:e.target.value}))}/></label>
   </div>
   <button type="submit" className="ins-profile-save" disabled={saving}>{saving?"Salvando...":"Salvar minhas anotações"}</button>
  </form>}
  {view==="privacidade"&&<div className="ins-profile-form">
    <div className="ins-profile-section"><h4>Quem pode usar sua ficha?</h4><p>Permita que um estabelecimento específico consulte e importe somente nome, contato, endereço, CPF e dados administrativos. Suas alergias e anotações de saúde ficam fora do compartilhamento.</p>
      <label className="ins-profile-field"><span>Escolha o estabelecimento</span>
       <select value={shareCompany} onChange={e=>setShareCompany(e.target.value)}>
        {catalog.length===0&&<option value="">Nenhum estabelecimento disponível</option>}
        {catalog.map(c=><option key={c.id} value={c.id}>{c.name}</option>)}
       </select></label>
      <button type="button" className="ins-profile-save" disabled={!shareCompany||saving} onClick={()=>void toggleShare(shareCompany,true)}>Autorizar este estabelecimento</button>
      <h4>Minhas autorizações</h4>
      {shares.filter(s=>s.permitted).length===0?<p>Nenhuma empresa autorizada até agora.</p>:shares.filter(s=>s.permitted).map(s=><div className="ins-profile-grant" key={s.company_id}><strong>{s.company_name}</strong><button type="button" disabled={saving} onClick={()=>void toggleShare(s.company_id,false)}>Revogar acesso</button></div>)}
      <p className="ins-profile-privacy">Revogar bloqueia novos acessos à ficha global. Registros já salvos pelo estabelecimento podem exigir uma solicitação específica de correção ou exclusão, conforme a legislação.</p>
    </div>
  </div>}
 </div>;
}
