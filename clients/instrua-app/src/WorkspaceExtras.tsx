import {useEffect,useState,type FormEvent} from "react";
import {api,type Session,type Company,type CatalogCompany} from "./production-api";
import type {Module} from "./ModulesHub";
import ProfileSettings, {type Appearance} from "./ProfileSettings";
import "./workspace-extras.css";

type SettingsPage="dados"|"saude"|"seguranca"|"privacidade"|"notificacoes"|"acessibilidade"|"organizacao";
const pages:{id:SettingsPage;name:string;icon:string;note:string}[]=[
  {id:"dados",name:"Dados pessoais",icon:"♙",note:"Sua identidade, seus contatos e seu endereço"},
  {id:"saude",name:"Saúde privada",icon:"✿",note:"Anotações opcionais e pessoais"},
  {id:"seguranca",name:"Segurança e acesso",icon:"◇",note:"Verificação e proteção da conta"},
  {id:"privacidade",name:"Privacidade e LGPD",icon:"☑",note:"Permissões, histórico e seus direitos"},
  {id:"notificacoes",name:"Comunicação e avisos",icon:"✉",note:"Lembretes, mensagens e preferências"},
  {id:"acessibilidade",name:"Aparência e acessibilidade",icon:"◉",note:"Conforto visual e facilidade de uso"},
  {id:"organizacao",name:"Organização e agenda",icon:"▦",note:"Unidades, horários e serviços"}
];
const roles:Record<string,string>={PATIENT:"Pessoa / paciente",COMPANY_OWNER:"Proprietário(a)",COMPANY_ADMIN:"Administração",RECEPTION:"Recepção",CLINICAL:"Profissional",BILLING:"Financeiro",PLATFORM_ADMIN:"Administração da plataforma"};

function Status({ready=false}:{ready?:boolean}){
 return <span className={"ins-extras-status "+(ready?"is-connected":"is-development")}><span aria-hidden="true" className="ins-extras-dot"/>{ready?"Dados da conta":"Em desenvolvimento"}</span>;
}
function FutureFeature({title,body}:{title:string;body:string}){
 return <article className="ins-extras-future"><span className="ins-extras-feature-dot" aria-hidden="true"/><div><strong>{title}</strong><p>{body}</p></div><Status/></article>;
}
export function PlannedModuleScreen({module,back}:{module:Module;back:()=>void}){
 return <section className="ins-extras">
   <button type="button" className="ins-extras-back" onClick={back}>‹ Voltar aos módulos</button>
   <header className="ins-extras-hero">
      <span className="ins-extras-hero-icon" aria-hidden="true">{module.icon}</span>
      <div><p className="ins-extras-eyebrow">{module.area} · INSTRUA</p><h2>{module.name}</h2><p>{module.description}</p><Status/></div>
   </header>
   <div className="ins-extras-cards">
      <section className="ins-extras-card"><span className="ins-extras-kicker">✿ SUA PRÓXIMA ÁREA</span><h3>Um espaço próprio para {module.name.toLocaleLowerCase("pt-BR")}</h3><p>O layout desta área já está preparado dentro do Instrua. A função será liberada quando os serviços, as permissões e os testes estiverem concluídos.</p></section>
      <section className="ins-extras-card ins-extras-soft"><span className="ins-extras-kicker">O QUE AINDA FALTA</span><h3>Integração com dados reais</h3><p>Esta área ainda não consulta, altera ou envia informações. Isso evita mostrar dados inventados ou permitir ações incompletas.</p></section>
   </div>
   <p className="ins-extras-bottom">O mesmo módulo aparecerá no site e no aplicativo com o visual do Instrua. A disponibilidade pode variar conforme o perfil e a autorização de acesso.</p>
 </section>;
}

type PersonalProfile={
 name:string; email:string; phone:string; document_number:string;rg:string;birth_date:string;
 postal_code:string;street:string;street_number:string;complement:string;neighborhood:string;
 city:string;state:string;emergency_contact_name:string;emergency_contact_phone:string;
 preferred_contact_channel:string;theme:"LIGHT"|"DARK";font_scale:number;high_contrast:boolean;
};
type Health={allergies:string;height_cm:string|number};
type Share={company_id:string;company_name:string;permitted:boolean};
const emptyProfile=(session:Session):PersonalProfile=>({
 name:session.name,email:session.email,phone:"",document_number:"",rg:"",birth_date:"",
 postal_code:"",street:"",street_number:"",complement:"",neighborhood:"",city:"",
 state:"",emergency_contact_name:"",emergency_contact_phone:"",
 preferred_contact_channel:"NONE",theme:"LIGHT",font_scale:1,high_contrast:false
});
const showError=(e:unknown)=>e instanceof Error?e.message:"Não foi possível concluir. Tente novamente.";
function applyAppearance(profile:PersonalProfile){
 document.documentElement.dataset.instruaTheme=profile.theme.toLowerCase();
 document.documentElement.dataset.instruaContrast=profile.high_contrast?"high":"normal";
 document.documentElement.style.fontSize=String(Math.round(profile.font_scale*100))+"%";
}
export function SettingsWorkspace({session,company,context}:{session:Session;company?:Company;context:"personal"|"business"}){
 const [page,setPage]=useState<SettingsPage>("dados");
 const [profile,setProfile]=useState<PersonalProfile>(()=>emptyProfile(session));
 const [health,setHealth]=useState<Health>({allergies:"",height_cm:""});
 const [shares,setShares]=useState<Share[]>([]);
 const [catalog,setCatalog]=useState<CatalogCompany[]>([]);
 const [shareCompany,setShareCompany]=useState("");
 const [loading,setLoading]=useState(true);
 const [busy,setBusy]=useState(false);
 const [notice,setNotice]=useState("");
 const [error,setError]=useState("");
 const active=pages.find(p=>p.id===page)!;
 useEffect(()=>{
   let mounted=true;setLoading(true);setError("");
   Promise.allSettled([
     api<Record<string,unknown>>("/me/profile",session.accessToken),
     api<Health>("/me/profile/private-health",session.accessToken),
     api<Share[]>("/me/profile/shares",session.accessToken),
     api<CatalogCompany[]>("/catalog",session.accessToken)
   ]).then(results=>{
     if(!mounted)return;
     if(results[0].status==="fulfilled"){
       const raw=results[0].value;
       const next=emptyProfile(session);
       for(const key of Object.keys(next) as (keyof PersonalProfile)[]){
         const value=raw[key];
         if(value!==null&&value!==undefined)(next as unknown as Record<string,unknown>)[key]=value;
       }
       next.font_scale=Number(next.font_scale)||1;
       setProfile(next);applyAppearance(next);
     }else setError(showError(results[0].reason));
     if(results[1].status==="fulfilled")setHealth(results[1].value);
     if(results[2].status==="fulfilled")setShares(results[2].value);
     if(results[3].status==="fulfilled")setCatalog(results[3].value);
     setLoading(false);
   });
   return()=>{mounted=false;};
 },[session.accessToken]);
 const update=(key:keyof PersonalProfile,value:string|number|boolean)=>{
   setProfile(old=>({...old,[key]:value}));
 };
 async function saveProfile(e:FormEvent){
   e.preventDefault();if(busy)return;
   setBusy(true);setNotice("");setError("");
   try{
     const r=await api<Record<string,unknown>>("/me/profile",session.accessToken,"PUT",{
       name:profile.name,phone:profile.phone,documentNumber:profile.document_number,rg:profile.rg,
       birthDate:profile.birth_date||null,postalCode:profile.postal_code.replace(/[^0-9]/g,""),
       street:profile.street,streetNumber:profile.street_number,complement:profile.complement,
       neighborhood:profile.neighborhood,city:profile.city,state:profile.state.toUpperCase(),
       emergencyContactName:profile.emergency_contact_name,emergencyContactPhone:profile.emergency_contact_phone,
       preferredContactChannel:profile.preferred_contact_channel,theme:profile.theme,
       fontScale:Number(profile.font_scale),highContrast:profile.high_contrast
     });
     setProfile(prev=>({...prev,...r,font_scale:Number(r.font_scale)||prev.font_scale} as PersonalProfile));
     applyAppearance({...profile,font_scale:Number(profile.font_scale)});
     setNotice("Suas informações foram salvas na sua conta Instrua.");
   }catch(e){setError(showError(e));}finally{setBusy(false);}
 }
 async function saveHealth(e:FormEvent){
   e.preventDefault();if(busy)return;setBusy(true);setNotice("");setError("");
   try{
     await api("/me/profile/private-health",session.accessToken,"PUT",{
       allergies:health.allergies,heightCm:String(health.height_cm).trim()?Number(health.height_cm):null
     });
     setNotice("Anotações pessoais de saúde salvas de forma separada.");
   }catch(e){setError(showError(e));}finally{setBusy(false);}
 }
 async function changeShare(id:string,permitted:boolean){
   if(busy)return;setBusy(true);setNotice("");setError("");
   try{
     await api("/me/profile/shares/"+id,session.accessToken,"PUT",{permitted});
     const list=await api<Share[]>("/me/profile/shares",session.accessToken);
     setShares(list);setNotice(permitted?"Você autorizou o estabelecimento a consultar sua ficha administrativa.":"Autorização revogada para novas consultas.");
   }catch(e){setError(showError(e));}finally{setBusy(false);}
 }
 return <section className="ins-extras ins-settings">
    <header className="ins-extras-hero ins-settings-header">
      <span className="ins-extras-hero-icon" aria-hidden="true">⚙</span>
      <div><p className="ins-extras-eyebrow">MEU INSTRUA · CONTROLE E PREFERÊNCIAS</p><h2>Configurações</h2><p>Atualize seus dados, escolha como prefere usar o Instrua e decida quem pode receber sua ficha.</p></div>
    </header>
    <div className="ins-settings-grid">
      <nav aria-label="Categorias de configurações" className="ins-settings-nav">
        {pages.map(p=><button type="button" key={p.id} className={p.id===page?"selected":""} aria-current={p.id===page?"page":undefined} onClick={()=>{setPage(p.id);setError("");setNotice("");}}>
          <span aria-hidden="true">{p.icon}</span><span><strong>{p.name}</strong><small>{p.note}</small></span>
        </button>)}
      </nav>
      <section className="ins-settings-content" aria-label={active.name}>
        <div className="ins-extras-heading"><div><span className="ins-extras-kicker">MINHAS PREFERÊNCIAS</span><h3>{active.name}</h3><p>{active.note}</p></div><Status ready={["dados","privacidade","acessibilidade","organizacao"].includes(page)}/></div>
        {loading&&<p role="status">Carregando seus dados da conta...</p>}
        {error&&<p role="alert" className="ins-settings-error">{error}</p>}
        {notice&&<p role="status" className="ins-settings-success">{notice}</p>}
        {page==="dados"&&!loading&&<>
          <form className="ins-profile-form" onSubmit={saveProfile}>
            <h4>Identificação e contato</h4>
            <div className="ins-profile-grid">
              <label className="wide">Nome completo<input required maxLength={255} autoComplete="name" value={profile.name} onChange={e=>update("name",e.target.value)}/></label>
              <label>E-mail da conta<input disabled type="email" value={session.email}/></label>
              <label>Telefone com DDD<input type="tel" maxLength={60} autoComplete="tel" value={profile.phone||""} onChange={e=>update("phone",e.target.value)}/></label>
              <label>CPF<input inputMode="numeric" placeholder="Somente números" maxLength={14} value={profile.document_number||""} onChange={e=>update("document_number",e.target.value)}/></label>
              <label>RG<input maxLength={30} value={profile.rg||""} onChange={e=>update("rg",e.target.value)}/></label>
              <label>Data de nascimento<input type="date" max={new Date().toISOString().slice(0,10)} value={profile.birth_date||""} onChange={e=>update("birth_date",e.target.value)}/></label>
              <label>Contato preferido<select value={profile.preferred_contact_channel||"NONE"} onChange={e=>update("preferred_contact_channel",e.target.value)}><option value="NONE">Ainda não definido</option><option value="EMAIL">E-mail</option><option value="WHATSAPP">WhatsApp</option><option value="SMS">SMS</option></select></label>
            </div>
            <h4>Endereço</h4>
            <div className="ins-profile-grid">
              <label>CEP<input inputMode="numeric" autoComplete="postal-code" maxLength={12} value={profile.postal_code||""} onChange={e=>update("postal_code",e.target.value)}/></label>
              <label>Estado (UF)<input maxLength={2} autoComplete="address-level1" value={profile.state||""} onChange={e=>update("state",e.target.value.toUpperCase())}/></label>
              <label className="wide">Rua, avenida ou logradouro<input autoComplete="address-line1" value={profile.street||""} onChange={e=>update("street",e.target.value)}/></label>
              <label>Número<input maxLength={30} value={profile.street_number||""} onChange={e=>update("street_number",e.target.value)}/></label>
              <label>Complemento<input maxLength={150} value={profile.complement||""} onChange={e=>update("complement",e.target.value)}/></label>
              <label>Bairro<input maxLength={150} value={profile.neighborhood||""} onChange={e=>update("neighborhood",e.target.value)}/></label>
              <label>Cidade<input maxLength={150} autoComplete="address-level2" value={profile.city||""} onChange={e=>update("city",e.target.value)}/></label>
            </div>
            <h4>Contato em caso de emergência</h4>
            <div className="ins-profile-grid">
              <label>Nome<input maxLength={255} value={profile.emergency_contact_name||""} onChange={e=>update("emergency_contact_name",e.target.value)}/></label>
              <label>Telefone<input type="tel" maxLength={60} value={profile.emergency_contact_phone||""} onChange={e=>update("emergency_contact_phone",e.target.value)}/></label>
            </div>
            <p className="ins-profile-note">Os dados pertencem à sua conta. Uma clínica só poderá reutilizá-los se você autorizar aquele estabelecimento. Informações clínicas não são compartilhadas nesta ficha.</p>
            <button className="real-primary" disabled={busy} type="submit">{busy?"Salvando...":"Salvar minha ficha"}</button>
          </form>
          <form className="ins-profile-form ins-health-private" onSubmit={saveHealth}>
            <h4>Minhas anotações privadas de saúde</h4>
            <p className="ins-profile-note">Somente você acessa estas anotações por esta tela. Elas não são repassadas à recepção pela busca de clientes.</p>
            <label>Alergias conhecidas<textarea rows={3} maxLength={2000} value={health.allergies||""} onChange={e=>setHealth(h=>({...h,allergies:e.target.value}))}/></label>
            <label>Altura (centímetros)<input type="number" min={20} max={300} step=".1" value={health.height_cm??""} onChange={e=>setHealth(h=>({...h,height_cm:e.target.value}))}/></label>
            <button type="submit" className="real-secondary" disabled={busy}>{busy?"Salvando...":"Salvar anotações privadas"}</button>
          </form>
        </>}
        {page==="acessibilidade"&&!loading&&<form className="ins-profile-form" onSubmit={saveProfile}>
          <h4>Personalize sua experiência</h4>
          <label>Tema visual<select value={profile.theme} onChange={e=>{update("theme",e.target.value);applyAppearance({...profile,theme:e.target.value as "LIGHT"|"DARK"});}}><option value="LIGHT">Claro, creme e verde-musgo</option><option value="DARK">Escuro, verde-musgo profundo</option></select></label>
          <label>Tamanho da fonte: {Math.round(Number(profile.font_scale)*100)}%<input type="range" min={0.9} max={1.4} step={0.05} value={profile.font_scale} onChange={e=>{const n=Number(e.target.value);update("font_scale",n);applyAppearance({...profile,font_scale:n});}}/></label>
          <label className="ins-profile-check"><input type="checkbox" checked={!!profile.high_contrast} onChange={e=>{update("high_contrast",e.target.checked);applyAppearance({...profile,high_contrast:e.target.checked});}}/> Ativar alto contraste</label>
          <p className="ins-profile-note">Os ajustes ficam salvos na sua conta para o site e o aplicativo Android.</p>
          <button className="real-primary" type="submit" disabled={busy}>Salvar aparência</button>
        </form>}
        {page==="privacidade"&&!loading&&<div className="ins-profile-form">
          <h4>Quem pode reutilizar minha ficha?</h4>
          <p>O Instrua não libera seus dados globais apenas porque alguém digitou seu e-mail ou telefone. Você precisa autorizar cada estabelecimento.</p>
          <label>Selecionar estabelecimento<select value={shareCompany} onChange={e=>setShareCompany(e.target.value)}><option value="">Escolha um estabelecimento</option>{catalog.map(c=><option key={c.id} value={c.id}>{c.name}</option>)}</select></label>
          <button type="button" className="real-primary" disabled={!shareCompany||busy} onClick={()=>void changeShare(shareCompany,true)}>Autorizar acesso à minha ficha administrativa</button>
          {shares.length>0&&<div className="ins-profile-shares">{shares.map(s=><article key={s.company_id}><div><strong>{s.company_name}</strong><small>{s.permitted?"Autorizado":"Sem autorização"}</small></div><button disabled={busy} className="real-secondary" onClick={()=>void changeShare(s.company_id,!s.permitted)}>{s.permitted?"Revogar":"Autorizar"}</button></article>)}</div>}
          <p className="ins-profile-note">O compartilhamento inclui identificação, CPF, endereço e telefone. Não inclui alergias, anotações privadas ou prontuários. Revogar impede novas consultas globais; registros operacionais já utilizados pelo estabelecimento seguem sua política legal de retenção.</p>
        </div>}
        {page==="seguranca"&&<>
          <FutureFeature title="Recuperação e verificação de identidade" body="Recuperação de conta, prova de vida e proteção adicional de sessões serão liberadas conforme validação de segurança."/>
          <FutureFeature title="Gerenciamento de sessões e dispositivos" body="Encerrar acessos antigos e revisar dispositivos autorizados ainda precisa de integração."/>
        </>}
        {page==="notificacoes"&&<>
          <FutureFeature title="Notificações e lembretes" body="Mensagens automáticas, preferências e horários silenciosos dependem de integrações de envio reais."/>
          <p className="ins-profile-note">Seu canal preferido já pode ser atualizado em Dados pessoais.</p>
        </>}
        {page==="organizacao"&&<>
          {context==="business"&&company?<div className="ins-settings-fields"><div><span>Estabelecimento</span><strong>{company.name}</strong></div><div><span>Fuso horário</span><strong>{company.timezone}</strong></div></div>:<p className="ins-extras-bottom">Seus vínculos e estabelecimentos são selecionados pela opção Trocar perfil no cabeçalho.</p>}
          <FutureFeature title="Disponibilidade e capacidade" body="Horários, salas, bloqueios e limites operacionais por profissional ainda serão configuráveis por módulo."/>
        </>}
      </section>
    </div>
  </section>;
}
