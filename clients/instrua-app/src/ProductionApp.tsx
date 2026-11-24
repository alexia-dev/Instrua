import { useEffect, useState, type FormEvent } from "react";
import SiteHome from "./SiteHome";
import AuthPage, {type AuthMode} from "./AuthPage";
import ModulesHub,{modules,moduleKey,areaOrder} from "./ModulesHub";
import {PlannedModuleScreen,SettingsWorkspace} from "./WorkspaceExtras";
import ScheduleBoard from "./ScheduleBoard";
import ClientWorkspace from "./ClientWorkspace";
import InstruaHome, {type HomeDestination} from "./InstruaHome";
import BusinessOverviewExtras from "./BusinessOverviewExtras";
import { api, API_URL, type Appointment, type AppointmentStatus, type CatalogCompany, type CancellationPolicy, type Client, type Company, type Professional, type Service, type Session } from "./production-api";
import "./production.css";

type Tab = string;
type WorkspaceRole={companyId:string;role:string;employeeId:string|null};
const labelStatus: Record<AppointmentStatus,string> = {SCHEDULED:"Agendado",CONFIRMED:"Confirmado",CANCELLED:"Cancelado",COMPLETED:"Concluído",NO_SHOW:"Falta"};
const money=(n:number|null)=> n==null?"Sob consulta":new Intl.NumberFormat("pt-BR",{style:"currency",currency:"BRL"}).format(Number(n));
const when=(d:string)=>new Intl.DateTimeFormat("pt-BR",{dateStyle:"short",timeStyle:"short"}).format(new Date(d));
const tomorrow=()=>{const d=new Date(Date.now()+86400000);return [d.getFullYear(),String(d.getMonth()+1).padStart(2,"0"),String(d.getDate()).padStart(2,"0")].join("-")+"T14:00";};
const message=(e:unknown)=>e instanceof Error?e.message:"Falha inesperada. Tente novamente.";
const prefix=(companyId:string)=>"/companies/"+companyId;

export default function ProductionApp() {
  const [route,setRoute]=useState(()=>window.location.hash.replace(/^#/,"")||"/");
  const mode:AuthMode=route==="/cadastro/empresa"?"empresa":route==="/cadastro/pessoa"?"cliente":"login";
  useEffect(()=>{const onHash=()=>setRoute(window.location.hash.replace(/^#/,"")||"/");window.addEventListener("hashchange",onHash);return()=>window.removeEventListener("hashchange",onHash);},[]);
  function navigate(next:string){window.location.hash="#"+next;setRoute(next);window.scrollTo({top:0,behavior:"auto"});}
  const [session,setSession]=useState<Session|null>(null);
  const [authForm,setAuthForm]=useState({name:"",email:"",password:"",companyName:"",slug:""});
  const [companies,setCompanies]=useState<Company[]>([]);
  const [workspaceRoles,setWorkspaceRoles]=useState<WorkspaceRole[]>([]);
  const [selected,setSelected]=useState("");
  const [clients,setClients]=useState<Client[]>([]);
  const [professionals,setProfessionals]=useState<Professional[]>([]);
  const [services,setServices]=useState<Service[]>([]);
  const [appointments,setAppointments]=useState<Appointment[]>([]);
  const [catalog,setCatalog]=useState<CatalogCompany[]>([]);
  const [ownAppointments,setOwnAppointments]=useState<Appointment[]>([]);
  const [tab,setTab]=useState<Tab>("painel");
  const [patientArea,setPatientArea]=useState<"inicio"|"agenda"|"buscar"|"oportunidades"|"eventos"|"comunidade"|"calendario"|"convites"|"modulos"|"configuracoes">("inicio");
  // Dedicated routes: browser back/forward also navigates the personal workspace.
  useEffect(()=>{
    const next=route.startsWith("/app/")?route.slice(5):"";
    const pages=["inicio","agenda","buscar","oportunidades","eventos","comunidade","calendario","convites","modulos","configuracoes"];
    if(pages.includes(next))setPatientArea(next as typeof patientArea);
  },[route]);
  const [error,setError]=useState("");
  const [success,setSuccess]=useState("");
  const [loading,setLoading]=useState(false);
  const [busy,setBusy]=useState(false);
  const [revision,setRevision]=useState(0);
  const [newProfessional,setNewProfessional]=useState({name:"",title:"",email:""});
  const [newService,setNewService]=useState({name:"",durationMinutes:"30",price:""});
  const [newBooking,setNewBooking]=useState({clientId:"",employeeId:"",serviceId:"",startsAt:tomorrow(),notes:""});
  const [cancelReason,setCancelReason]=useState("");
  const [selectedCatalog,setSelectedCatalog]=useState("");
  const [patientBooking,setPatientBooking]=useState({serviceId:"",employeeId:"",startsAt:tomorrow(),accepted:false});
  const [policy,setPolicy]=useState<CancellationPolicy|null>(null);
  const [patientCancelReason,setPatientCancelReason]=useState("");
  const [workspaceMode,setWorkspaceMode]=useState<"personal"|"work">("personal");
  const [inviteEmail,setInviteEmail]=useState("");
  const [inviteRole,setInviteRole]=useState("RECEPTION");
  const [inviteCode,setInviteCode]=useState("");
  const [acceptCode,setAcceptCode]=useState(()=>new URLSearchParams(window.location.search).get("invite")||"");
  const canWork=!!session&&(workspaceRoles.length>0||session.roles.some(r=>["COMPANY_OWNER","COMPANY_ADMIN","RECEPTION","CLINICAL","BILLING","PLATFORM_ADMIN"].includes(r)));
  const isPatient=!!session&&workspaceMode==="personal";
  const workspaceRole=workspaceRoles.find(w=>w.companyId===selected)?.role||
    (session?.roles.includes("COMPANY_OWNER")?"COMPANY_OWNER":session?.roles.includes("PLATFORM_ADMIN")?"PLATFORM_ADMIN":null);
  const canManage=workspaceRole==="COMPANY_OWNER"||workspaceRole==="COMPANY_ADMIN";
  const canSchedule=canManage||workspaceRole==="RECEPTION";
  const canClinical=workspaceRole==="CLINICAL";
  const canSeeOperations=canSchedule||canClinical;
  const workspaceName=canManage?"Administração":workspaceRole==="RECEPTION"?"Recepção":canClinical?"Área profissional":workspaceRole==="BILLING"?"Financeiro":workspaceRole==="PLATFORM_ADMIN"?"Administração da plataforma":"Meu espaço";
  const business=companies.find(c=>c.id===selected);
  const selectedModule=modules.find(module=>moduleKey(module)===tab);
  const catalogBusiness=catalog.find(c=>c.id===selectedCatalog);

  function clearMessages(){setError("");setSuccess("");}
  function logout(){navigate("/");setWorkspaceMode("personal");setInviteCode("");setSession(null);setPatientArea("inicio");setTab("painel");setAuthForm(f=>({...f,password:""}));setCompanies([]);setWorkspaceRoles([]);setSelected("");setAppointments([]);setClients([]);setProfessionals([]);setServices([]);setCatalog([]);setOwnAppointments([]);clearMessages();}
  async function authenticate(e:FormEvent){
    e.preventDefault();if(busy)return;setBusy(true);clearMessages();
    try{
      let account:Session;
      if(mode==="login"){account=await api<Session>("/auth/login",undefined,"POST",{email:authForm.email.trim(),password:authForm.password});}
      else if(mode==="cliente"){account=await api<Session>("/auth/register/patient",undefined,"POST",{name:authForm.name.trim(),email:authForm.email.trim(),password:authForm.password});}
      else{
        const data=await api<{authentication:Session;company:Company}>("/auth/register",undefined,"POST",{ownerName:authForm.name.trim(),email:authForm.email.trim(),password:authForm.password,companyName:authForm.companyName.trim(),companySlug:authForm.slug.trim().toLowerCase(),companyEmail:authForm.email.trim(),timezone:"America/Sao_Paulo"});
        account=data.authentication;setCompanies([data.company]);setSelected(data.company.id);
      }
      setAuthForm(f=>({...f,password:""}));setSession(account);setWorkspaceMode(account.roles.some(r=>["COMPANY_OWNER","COMPANY_ADMIN","RECEPTION","CLINICAL","BILLING","PLATFORM_ADMIN"].includes(r))?"work":"personal");navigate("/app");setTab("painel");setPatientArea("inicio");setSuccess("Conta conectada ao Instrua.");
    }catch(e){setError(message(e));}finally{setBusy(false);}
  }
  useEffect(()=>{
    if(!session)return;
    let active=true;
    api<{theme?:string;font_scale?:number;high_contrast?:boolean}>("/me/profile",session.accessToken)
      .then(p=>{if(active){document.documentElement.dataset.instruaTheme=(p.theme||"LIGHT").toLowerCase();
        document.documentElement.dataset.instruaContrast=p.high_contrast?"high":"normal";
        document.documentElement.style.fontSize=String(Math.round((Number(p.font_scale)||1)*100))+"%";}})
      .catch(()=>{});
    return()=>{active=false;};
  },[session]);
  useEffect(()=>{
    if(!session)return;let active=true;
    void api<WorkspaceRole[]>("/me/workspaces",session.accessToken)
      .then(roles=>{if(active)setWorkspaceRoles(roles);})
      .catch(err=>{if(active)setError("Não foi possível verificar suas permissões profissionais: "+message(err));});
    return()=>{active=false;};
  },[session,revision]);
  useEffect(()=>{
    if(!session)return;let active=true;
    async function load(){
      try{
        if(isPatient){
          const [a,c]=await Promise.all([api<Appointment[]>("/me/appointments",session!.accessToken),api<CatalogCompany[]>("/catalog",session!.accessToken)]);
          if(active){setOwnAppointments(a);setCatalog(c);}
        }else{
          const c=await api<Company[]>("/companies",session!.accessToken);
          if(active){setCompanies(c);setSelected(prev=>c.some(x=>x.id===prev)?prev:(c[0]?.id||""));}
        }
      }catch(e){if(active)setError(message(e));}
    }
    void load();return()=>{active=false;};
  },[session,isPatient,revision]);
  useEffect(()=>{
    if(!session||isPatient||!selected||!workspaceRole)return;
    let active=true;setLoading(true);
    // Only request domains the verified membership authorizes.
    // A clinical account receives only its own schedule from the server.
    // A billing account never fetches patients or appointments.
    async function load(){
      try{
        const p=prefix(selected);
        if(canSchedule){
          const [c,e,s,a]=await Promise.all([
            api<Client[]>(p+"/clients",session!.accessToken),
            api<Professional[]>(p+"/employees",session!.accessToken),
            api<Service[]>(p+"/services",session!.accessToken),
            api<Appointment[]>(p+"/appointments",session!.accessToken)
          ]);
          if(active){setClients(c);setProfessionals(e);setServices(s);setAppointments(a);}
        }else if(canClinical){
          const a=await api<Appointment[]>(p+"/appointments",session!.accessToken);
          if(active){setAppointments(a);setClients([]);setProfessionals([]);setServices([]);}
        }else if(active){setClients([]);setProfessionals([]);setServices([]);setAppointments([]);}
      }catch(err){if(active)setError(message(err));}
      finally{if(active)setLoading(false);}
    }
    void load();return()=>{active=false;};
  },[session,selected,isPatient,revision,workspaceRole,canSchedule,canClinical]);
  useEffect(()=>{
    if(!session||isPatient)return;
    const allowed=canManage?["painel","agenda","clientes","equipe","servicos","modulos","comunidade","configuracoes"]:
      workspaceRole==="RECEPTION"?["painel","agenda","clientes","equipe","comunidade","configuracoes"]:
      canClinical?["painel","agenda","comunidade","configuracoes"]:["painel","comunidade","configuracoes"];
    if(!allowed.includes(tab)&&!canManage)setTab("painel");
  },[session,isPatient,workspaceRole,selected,tab,canManage,canClinical]);
  useEffect(()=>{setSelectedCatalog(old=>catalog.some(c=>c.id===old)?old:(catalog[0]?.id||""));},[catalog]);
  useEffect(()=>{
    if(!catalogBusiness)return;
    setPatientBooking(old=>({...old,serviceId:catalogBusiness.services.some(s=>s.id===old.serviceId)?old.serviceId:(catalogBusiness.services[0]?.id||""),employeeId:catalogBusiness.professionals.some(p=>p.id===old.employeeId)?old.employeeId:(catalogBusiness.professionals[0]?.id||""),accepted:false}));
  },[catalogBusiness]);
  useEffect(()=>{
    if(!isPatient||!session||!selectedCatalog||!patientBooking.serviceId){setPolicy(null);return;}
    let active=true;setPolicy(null);
    void api<CancellationPolicy[]>(prefix(selectedCatalog)+"/cancellation-policies?serviceId="+encodeURIComponent(patientBooking.serviceId),session.accessToken)
    .then(list=>{if(active)setPolicy(list.find(p=>new Date(p.effective_from).getTime()<=Date.now())||null);})
    .catch(e=>{if(active)setError(message(e));});
    return()=>{active=false;};
  },[session,isPatient,selectedCatalog,patientBooking.serviceId]);

  async function commit<T>(fn:()=>Promise<T>,ok:string,after?:()=>void){
    if(busy)return;setBusy(true);clearMessages();
    try{await fn();after?.();setSuccess(ok);setRevision(r=>r+1);}
    catch(e){setError(message(e));}finally{setBusy(false);}
  }
  function createProfessional(e:FormEvent){e.preventDefault();if(!session||!selected)return;
    void commit(()=>api<Professional>(prefix(selected)+"/employees",session.accessToken,"POST",{...newProfessional,accessRole:"CLINICAL"}),"Profissional salvo no PostgreSQL.",()=>setNewProfessional({name:"",title:"",email:""}));}
  function createService(e:FormEvent){e.preventDefault();if(!session||!selected)return;
    void commit(()=>api<Service>(prefix(selected)+"/services",session.accessToken,"POST",{name:newService.name,durationMinutes:Number(newService.durationMinutes),price:newService.price===""?null:Number(newService.price)}),"Serviço salvo no PostgreSQL.",()=>setNewService({name:"",durationMinutes:"30",price:""}));}
  function createAppointment(e:FormEvent){e.preventDefault();if(!session||!selected)return;
    if(!newBooking.clientId||!newBooking.employeeId||!newBooking.serviceId){setError("Escolha cliente, profissional e serviço.");return;}
    void commit(()=>api<Appointment>(prefix(selected)+"/appointments",session.accessToken,"POST",{...newBooking,startsAt:new Date(newBooking.startsAt).toISOString()}),"Agendamento gravado no servidor.",()=>setNewBooking(x=>({...x,notes:""})));}
  function updateStatus(id:string,status:AppointmentStatus){
    if(!session||!selected)return;if(status==="CANCELLED"&&!cancelReason.trim()){setError("Informe o motivo antes de cancelar.");return;}
    void commit(()=>api<Appointment>(prefix(selected)+"/appointments/"+id+"/status",session.accessToken,"PATCH",{status,reason:status==="CANCELLED"?cancelReason.trim():null}),"Agenda atualizada no servidor.",()=>setCancelReason(""));
  }
  function bookSelf(e:FormEvent){e.preventDefault();if(!session||!selectedCatalog)return;
    if(!patientBooking.employeeId||!patientBooking.serviceId){setError("Escolha serviço e profissional.");return;}
    if(policy&&!patientBooking.accepted){setError("Leia e aceite a política de cancelamento.");return;}
    void commit(()=>api(prefix(selectedCatalog)+"/appointments/self",session.accessToken,"POST",{serviceId:patientBooking.serviceId,employeeId:patientBooking.employeeId,startsAt:new Date(patientBooking.startsAt).toISOString(),policyAccepted:!!policy?true:null,cancellationPolicyId:policy?.id||null}),"Agendamento registrado no servidor.",()=>setPatientBooking(x=>({...x,accepted:false})));}
  function cancelSelf(id:string){if(!session)return;if(!patientCancelReason.trim()){setError("Informe um motivo para cancelar.");return;}
    void commit(()=>api("/me/appointments/"+id+"/cancel",session.accessToken,"POST",{reason:patientCancelReason.trim(),confirmPotentialFee:false}),"Agendamento cancelado.",()=>setPatientCancelReason(""));}

  async function generateInvitation(e:FormEvent){
    e.preventDefault();if(!session||!selected||busy)return;setBusy(true);clearMessages();
    try{const response=await api<{token:string}>(prefix(selected)+"/invitations",session.accessToken,"POST",{email:inviteEmail.trim(),role:inviteRole});setInviteCode(response.token);setSuccess("Convite criado. Compartilhe o código somente com o destinatário.");setInviteEmail("");}
    catch(err){setError(message(err));}finally{setBusy(false);}
  }
  async function acceptInvitation(e:FormEvent){
    e.preventDefault();if(!session||!acceptCode.trim()||busy)return;setBusy(true);clearMessages();
    try{await api("/me/invitations/"+encodeURIComponent(acceptCode.trim())+"/accept",session.accessToken,"POST");setAcceptCode("");window.history.replaceState(null,"",window.location.pathname);setWorkspaceMode("work");setRevision(v=>v+1);setSuccess("Você agora faz parte da equipe. Acesso atualizado.");}
    catch(err){setError(message(err));}finally{setBusy(false);}
  }
  return <div className="real-app">
    {session&&<header className="real-header"><div className="real-brand"><img src="/brand-icon.svg" className="real-mark-image" alt="" /><div><strong>instrua</strong><small>Kiyubi Technologies</small></div></div>
      {session?<div className="real-header-actions"><span>{session.name}</span>{canWork&&<label className="ins-workspace-switch"><span>Usar Instrua como</span><select aria-label="Trocar perfil" value={workspaceMode} onChange={e=>{setWorkspaceMode(e.target.value as "personal"|"work");setPatientArea("inicio");setTab("painel");clearMessages();navigate("/app");}}><option value="personal">Minha jornada pessoal</option><option value="work">Área profissional / estabelecimento</option></select></label>}{isPatient&&<button type="button" className="real-secondary" onClick={()=>setPatientArea(v=>v==="configuracoes"?"inicio":"configuracoes")}>{patientArea==="configuracoes"?"Meu início":"⚙ Configurações"}</button>}<button className="real-secondary" onClick={logout}>Sair</button></div>:<small>Agenda e gestão conectadas</small>}</header>}
    <main className={session?"real-container":"real-public-main"}>
      {error&&<p role="alert" className="real-alert real-error">{error}<button type="button" aria-label="Fechar aviso" onClick={()=>setError("")}>×</button></p>}
      {success&&<p role="status" className="real-alert real-success">{success}<button type="button" aria-label="Fechar aviso" onClick={()=>setSuccess("")}>×</button></p>}
      {!API_URL&&<p role="alert" className="real-alert real-error">API não configurada. Defina VITE_INSTRUA_API_URL para habilitar o ambiente real. Nenhum dado fictício será usado.</p>}

      {!session && (
        route==="/entrar"||route==="/cadastro/empresa"||route==="/cadastro/pessoa"||route.startsWith("/app")?
        <AuthPage mode={mode} form={authForm} setForm={setAuthForm} onSubmit={authenticate} onNavigate={navigate} busy={busy} apiAvailable={!!API_URL}/>:
        <SiteHome onNavigate={navigate}/>
      )}

      {session&&!isPatient&&<><div className="real-topline"><div><p className="real-eyebrow">{workspaceName.toUpperCase()} · INSTRUA</p><h1>{business?.name||"Estabelecimentos"}</h1><p className="real-workspace-subtitle">Seu espaço de trabalho, com tudo no lugar certo.</p></div>
        {companies.length>0&&<label>Estabelecimento<select value={selected} onChange={e=>setSelected(e.target.value)}>{companies.map(c=><option key={c.id} value={c.id}>{c.name}</option>)}</select></label>}</div>
        {companies.length===0?<section className="real-panel"><h2>Sem estabelecimento vinculado</h2><p>Entre com a conta administradora cadastrada para sua empresa.</p></section>:<>
          <div className="real-workspace-layout">
            <nav className="real-tabs" aria-label="Navegação do espaço de trabalho">
              <div className="real-sidebar-intro"><span>✳ INSTRUA WORKSPACE</span><strong>{workspaceName}</strong><small>Uma coisa de cada vez, com carinho.</small></div>
              {([["painel","⌂","Visão geral"],["agenda","▦","Agenda"],["clientes","♧","Encontrar pessoas"],["comunidade","♡","Instrua Social"],["equipe","♙","Equipe"],["servicos","✳","Serviços"],["modulos","☷","Módulos"]] as [Tab,string,string][]).filter(([key])=>key==="painel"||key==="comunidade"||(key==="agenda"&&canSeeOperations)||(key==="clientes"&&canSchedule)||(key==="equipe"&&canSchedule)||(key==="servicos"&&canManage)||(key==="modulos"&&canManage)).map(([key,symbol,title])=><button type="button" key={key} className={tab===key?"selected":""} aria-current={tab===key?"page":undefined} onClick={()=>{setTab(key);clearMessages();}}><span aria-hidden="true" className="real-tab-symbol">{symbol}</span><span>{title}</span>{tab===key&&<span aria-hidden="true" className="real-tab-current">•</span>}</button>)}
              {canManage&&<div className="real-sidebar-module-group">
                <div className="real-sidebar-module-label">✦ TODOS OS MÓDULOS</div>
                {areaOrder.map(area=><div className="real-sidebar-cluster" key={area}>
                  <div className="real-sidebar-cluster-title">{area}</div>
                  {modules.filter(m=>m.area===area).map(m=>{
                    const target=m.tab||moduleKey(m);
                    return <button type="button" key={moduleKey(m)} className={"real-sidebar-module"+(tab===target?" selected":"")} onClick={()=>{setTab(target);clearMessages();}} aria-current={tab===target?"page":undefined}>
                      <span className="real-tab-symbol" aria-hidden="true">{m.icon}</span>
                      <span className="real-sidebar-module-title"><strong>{m.name}</strong>{!m.tab&&<small><span className="real-sidebar-inprogress" aria-hidden="true"/> Em desenvolvimento</small>}</span>
                    </button>;
                  })}
                </div>)}
              </div>}
              <button type="button" className={tab==="configuracoes"?"selected real-settings-trigger":"real-settings-trigger"} onClick={()=>{setTab("configuracoes");clearMessages();}} aria-current={tab==="configuracoes"?"page":undefined}><span className="real-tab-symbol" aria-hidden="true">⚙</span><span>Configurações</span></button>
              <p className="real-sidebar-footnote">🌿 Menos confusão, mais tempo pra você.</p>
            </nav>
            <div className="real-workspace-main">
          {loading&&<p role="status">Sincronizando com o servidor...</p>}
          {tab==="painel"&&<div className="real-business-home">
            <section className="real-dashboard-welcome"><div className="real-welcome-copy">
              <span className="real-welcome-kicker">✦ SEU DIA, MAIS LEVE</span>
              <h2>Que bom te ver, {session.name.split(" ")[0]}! <span aria-hidden="true">🌿</span></h2>
              <p>{canManage?"Sua gestão, equipe e atendimento organizados, com os números reais do dia.":canSchedule?"Sua recepção com agenda, pessoas e avisos no lugar certo.":canClinical?"Os seus atendimentos e a comunidade, com acesso apenas ao necessário.":"Seus indicadores e ferramentas autorizadas em um espaço só."}</p>
              {canSeeOperations&&<button type="button" className="real-welcome-action" onClick={()=>setTab("agenda")}>Abrir agenda <span aria-hidden="true">↗</span></button>}
            </div><div className="real-welcome-ornament" aria-hidden="true"><span>INSTRUA</span><strong>Seu espaço,<br/>seu ritmo.</strong><small>feito pra facilitar ✿</small></div></section>
            {canSeeOperations&&<><div className="real-stats">
                <div><span>Atendimentos</span><b>{appointments.length}</b></div>
                <div><span>Confirmados</span><b>{appointments.filter(a=>a.status==="CONFIRMED").length}</b></div>
                {canSchedule&&<><div><span>Profissionais</span><b>{professionals.length}</b></div><div><span>Pessoas cadastradas</span><b>{clients.length}</b></div></>}
              </div><section className="real-panel"><h2>{canClinical?"Meus próximos atendimentos":"Próximos atendimentos"}</h2>
              <Bookings list={appointments.filter(a=>new Date(a.startsAt).getTime()>=Date.now()&&a.status!=="CANCELLED").slice(0,8)}/>
              <button type="button" className="real-secondary" onClick={()=>setTab("agenda")}>Ver agenda completa</button></section></>}
            {business&&<BusinessOverviewExtras token={session.accessToken} companyId={business.id}
              canSeeSales={canManage||workspaceRole==="BILLING"} onSocial={()=>setTab("comunidade")}/>}
          </div>}
          {tab==="comunidade"&&<InstruaHome view="comunidade" session={session} appointments={appointments} isPatient={false} onOpen={()=>setTab("painel")}/>}
          {canManage&&tab==="modulos"&&<ModulesHub openTab={nextTab=>{setTab(nextTab);clearMessages();}}/>}
          {tab==="configuracoes"&&<SettingsWorkspace session={session} company={business} context="business"/>}
          {canManage&&selectedModule&&!selectedModule.tab&&<PlannedModuleScreen module={selectedModule} back={()=>setTab("modulos")}/>}
          
          {canSchedule&&tab==="clientes"&&session&&selected&&<ClientWorkspace key={selected} session={session} companyId={selected} clients={clients} onChanged={()=>setRevision(r=>r+1)}/>}
          {tab==="equipe"&&canManage&&<section className="real-panel"><h2>Convidar colaborador para acessar o sistema</h2><form className="real-form" onSubmit={generateInvitation}><label>E-mail da pessoa<input type="email" required value={inviteEmail} onChange={e=>setInviteEmail(e.target.value)}/></label><label>Função<select value={inviteRole} onChange={e=>setInviteRole(e.target.value)}><option value="RECEPTION">Recepção</option><option value="CLINICAL">Profissional</option><option value="COMPANY_ADMIN">Administração</option><option value="BILLING">Financeiro</option></select></label><button type="submit" className="real-primary" disabled={busy}>Gerar convite individual</button></form>{inviteCode&&<div className="real-policy"><h3>Código único do convite</h3><input aria-label="Código a compartilhar" readOnly value={inviteCode} onFocus={e=>e.currentTarget.select()}/><p className="real-small">A pessoa deve criar ou acessar a própria conta com o e-mail convidado, abrir Minha jornada e aceitar este código. O código expira em 7 dias.</p></div>}</section>}
          {canManage&&tab==="servicos"&&<div className="real-columns"><section className="real-panel"><h2>Adicionar serviço</h2><form className="real-form" onSubmit={createService}>
            <label>Nome<input required value={newService.name} onChange={e=>setNewService({...newService,name:e.target.value})}/></label><label>Duração em minutos<input type="number" required min="1" max="1440" value={newService.durationMinutes} onChange={e=>setNewService({...newService,durationMinutes:e.target.value})}/></label><label>Preço em reais (opcional)<input type="number" min="0" step=".01" value={newService.price} onChange={e=>setNewService({...newService,price:e.target.value})}/></label><button className="real-primary" type="submit" disabled={busy}>Salvar serviço</button></form></section>
            <section className="real-panel"><h2>Serviços ({services.length})</h2><Records list={services.map(s=>({id:s.id,title:s.name,subtitle:String(s.durationMinutes)+" min · "+money(s.price)}))}/></section></div>}
          {canSeeOperations&&tab==="agenda"&&<div className="real-columns"><section className="real-panel real-schedule-full"><div className="real-section-head"><h2>Agenda visual por profissional</h2><span className="real-workspace-caption">Dados sincronizados com o servidor</span></div><ScheduleBoard appointments={appointments} professionals={professionals} canManage={canSchedule} busy={busy} onStatus={updateStatus}/></section>{canSchedule&&<section className="real-panel"><h2>Novo agendamento</h2><form className="real-form" onSubmit={createAppointment}>
            <label>Cliente<select value={newBooking.clientId} required onChange={e=>setNewBooking({...newBooking,clientId:e.target.value})}><option value="">Selecionar</option>{clients.filter(c=>c.active).map(c=><option value={c.id} key={c.id}>{c.name}</option>)}</select></label>
            <label>Profissional<select value={newBooking.employeeId} required onChange={e=>setNewBooking({...newBooking,employeeId:e.target.value})}><option value="">Selecionar</option>{professionals.filter(p=>p.active).map(p=><option value={p.id} key={p.id}>{p.name}</option>)}</select></label>
            <label>Serviço<select value={newBooking.serviceId} required onChange={e=>setNewBooking({...newBooking,serviceId:e.target.value})}><option value="">Selecionar</option>{services.filter(s=>s.active).map(s=><option value={s.id} key={s.id}>{s.name} ({s.durationMinutes} min)</option>)}</select></label>
            <label>Data/hora do seu dispositivo<input type="datetime-local" required value={newBooking.startsAt} onChange={e=>setNewBooking({...newBooking,startsAt:e.target.value})}/></label>
            <label>Observações administrativas<textarea maxLength={2000} rows={3} value={newBooking.notes} onChange={e=>setNewBooking({...newBooking,notes:e.target.value})}/></label>
            <button className="real-primary" type="submit" disabled={busy||!clients.length||!professionals.length||!services.length}>Registrar no servidor</button></form><p className="real-small">Para reservar, é necessário ter cliente, profissional e serviço cadastrados.</p></section>}
            <section className="real-panel"><div className="real-section-head"><h2>Agenda ({appointments.length})</h2><button className="real-secondary" type="button" onClick={()=>setRevision(v=>v+1)}>Atualizar</button></div>
              <label>Motivo para cancelar<input placeholder="Obrigatório para cancelamentos" maxLength={1000} value={cancelReason} onChange={e=>setCancelReason(e.target.value)}/></label>
              <Bookings list={appointments} controls={canSchedule?a=>(a.status==="SCHEDULED"||a.status==="CONFIRMED")?<div className="real-row-actions">{a.status==="SCHEDULED"&&<button disabled={busy} onClick={()=>updateStatus(a.id,"CONFIRMED")}>Confirmar</button>}<button disabled={busy} onClick={()=>updateStatus(a.id,"COMPLETED")}>Concluir</button><button disabled={busy} onClick={()=>updateStatus(a.id,"CANCELLED")}>Cancelar</button></div>:null:undefined}/></section></div>}
            </div>
          </div>
        </>}</>}


      {session&&isPatient&&<section className="real-patient">
        <nav className="ins-personal-nav" aria-label="Navegação da pessoa">
          {([
            ["inicio","⌂","Início"],["agenda","▦","Agendamentos"],["buscar","⌕","Encontrar"],
            ["oportunidades","✧","Oportunidades"],["eventos","▤","Eventos"],["comunidade","♡","Social"],
            ["calendario","◷","Calendário"],["convites","♧","Convites"],["modulos","✳","Módulos"],
            ["configuracoes","⚙","Ajustes"]
          ] as [typeof patientArea,string,string][]).map(([key,icon,label])=>
            <button key={key} type="button" className={patientArea===key?"active":""} aria-current={patientArea===key?"page":undefined}
              onClick={()=>{setPatientArea(key);clearMessages();window.location.hash="#/app/"+key;}}>
              <span aria-hidden="true">{icon}</span>{label}
            </button>
          )}
        </nav>
        {(["inicio","oportunidades","eventos","comunidade"] as string[]).includes(patientArea)&&
          <InstruaHome
            view={patientArea as "inicio"|"oportunidades"|"eventos"|"comunidade"}
            session={session} appointments={ownAppointments} isPatient
            onOpen={(destination:HomeDestination)=>{const next=destination==="agenda"?"agenda":destination==="equipe"?"convites":destination;setPatientArea(next as typeof patientArea);window.location.hash="#/app/"+next;clearMessages();}}
          />
        }
        {patientArea==="agenda"&&<>
          <section className="real-personal-hero"><p className="real-eyebrow">MINHA JORNADA</p><h1>Meus agendamentos</h1><p>Veja seus próximos compromissos e acompanhe cada confirmação.</p>
            <div className="real-personal-summary"><div><small>Próximos</small><strong>{ownAppointments.filter(a=>a.status!=="CANCELLED"&&new Date(a.startsAt).getTime()>=Date.now()).length}</strong></div><div><small>Confirmados</small><strong>{ownAppointments.filter(a=>a.status==="CONFIRMED").length}</strong></div></div></section>
          <section className="real-panel"><div className="real-section-head"><h2>Meus atendimentos</h2><button type="button" className="real-secondary" onClick={()=>setRevision(r=>r+1)}>Atualizar</button></div><Bookings list={ownAppointments}/>
            <label>Motivo para cancelar<input maxLength={1000} value={patientCancelReason} onChange={e=>setPatientCancelReason(e.target.value)}/></label>
            {ownAppointments.filter(a=>a.status==="SCHEDULED"||a.status==="CONFIRMED").map(a=><button className="real-secondary real-spaced" type="button" key={a.id} disabled={busy} onClick={()=>cancelSelf(a.id)}>Cancelar {a.serviceName} — {when(a.startsAt)}</button>)}
          </section>
          <button type="button" className="real-primary" onClick={()=>setPatientArea("buscar")}>Encontrar novo atendimento</button>
        </>}
        {patientArea==="buscar"&&<section className="real-panel ins-personal-search">
          <span className="real-eyebrow">DESCUBRA E AGENDE</span><h1>Encontrar atendimento</h1><p>Escolha um estabelecimento, um serviço e o melhor horário para você.</p>
          {catalog.length===0?<p className="real-empty">Nenhum estabelecimento ativo disponível no momento.</p>:<form className="real-form" onSubmit={bookSelf}>
            <label>Estabelecimento<select value={selectedCatalog} onChange={e=>setSelectedCatalog(e.target.value)}>{catalog.map(c=><option value={c.id} key={c.id}>{c.name}</option>)}</select></label>
            <label>Serviço<select required value={patientBooking.serviceId} onChange={e=>setPatientBooking({...patientBooking,serviceId:e.target.value,accepted:false})}><option value="">Selecionar</option>{catalogBusiness?.services.map(s=><option value={s.id} key={s.id}>{s.name} · {money(s.price)}</option>)}</select></label>
            <label>Profissional<select required value={patientBooking.employeeId} onChange={e=>setPatientBooking({...patientBooking,employeeId:e.target.value})}><option value="">Selecionar</option>{catalogBusiness?.professionals.map(p=><option value={p.id} key={p.id}>{p.name}</option>)}</select></label>
            <label>Data/hora do seu dispositivo<input type="datetime-local" required value={patientBooking.startsAt} onChange={e=>setPatientBooking({...patientBooking,startsAt:e.target.value})}/></label>
            {policy&&<div className="real-policy"><h3>{policy.title}</h3><p>{policy.policy_text}</p><label className="real-checkbox"><input type="checkbox" checked={patientBooking.accepted} onChange={e=>setPatientBooking({...patientBooking,accepted:e.target.checked})}/> Li e aceito a política de cancelamento</label></div>}
            <button type="submit" className="real-primary" disabled={busy||!patientBooking.employeeId||!patientBooking.serviceId}>Solicitar agendamento</button>
          </form>}
          <p className="real-small">A disponibilidade é confirmada pelo servidor no momento da reserva. Sem cobrança online nesta versão.</p>
        </section>}
        {patientArea==="calendario"&&<section className="real-panel">
          <span className="real-eyebrow">ORGANIZE SUA VIDA</span><h1>Meu calendário</h1><p>Seus compromissos reais, organizados por data.</p>
          <Bookings list={[...ownAppointments].sort((a,b)=>a.startsAt.localeCompare(b.startsAt))}/>
          <p className="real-small">Lembretes de medicamentos, exames e eventos ainda serão integrados a este calendário.</p>
        </section>}
        {patientArea==="convites"&&<section className="real-panel">
          <span className="real-eyebrow">ÁREA PROFISSIONAL</span><h1>Meus convites</h1><p>Foi convidada para trabalhar em um estabelecimento? Use o código recebido para vincular sua conta.</p>
          <form className="real-form" onSubmit={acceptInvitation}><label>Código do convite<input value={acceptCode} onChange={e=>setAcceptCode(e.target.value)} placeholder="Cole o código recebido" required/></label><button type="submit" className="real-primary" disabled={busy||!acceptCode.trim()}>Aceitar convite</button></form>
        </section>}
        {patientArea==="modulos"&&<section className="real-panel">
          <span className="real-eyebrow">ECOSSISTEMA INSTRUA</span><h1>Descubra os módulos</h1><p>Cada área é desenvolvida separadamente. Recursos administrativos só aparecem para pessoas autorizadas.</p>
          <div className="ins-personal-modules">{modules.filter(m=>["Pessoas e cuidados","Comunidade","Automações"].includes(m.area)).map(m=><article key={moduleKey(m)}><strong>{m.icon} {m.name}</strong><p>{m.description}</p><small>{m.tab?"Disponível na área de gestão":"Em desenvolvimento"}</small></article>)}</div>
        </section>}
        {patientArea==="configuracoes"&&<SettingsWorkspace session={session} context="personal"/>}
      </section>}
    </main>
    {session&&<footer className="real-footer">Instrua · Kiyubi Technologies · Orientação, conexão e confiança.</footer>}
  </div>;
}

function Records({list}:{list:{id:string;title:string;subtitle:string}[]}){
  if(!list.length)return <p className="real-empty">Nenhum registro. Cadastre o primeiro.</p>;
  return <ul className="real-records">{list.map(item=><li key={item.id}><strong>{item.title}</strong><small>{item.subtitle}</small></li>)}</ul>;
}
function Bookings({list,controls}:{list:Appointment[];controls?:(a:Appointment)=>import("react").ReactNode}){
  if(!list.length)return <p className="real-empty">Nenhum agendamento cadastrado.</p>;
  return <div className="real-bookings">{list.map(a=><article className="real-booking" key={a.id}><div className="real-booking-head"><strong>{when(a.startsAt)}</strong><span className="real-status">{labelStatus[a.status]}</span></div><h3>{a.clientName}</h3><p>{a.serviceName} · {a.employeeName||"Sem profissional"}</p>{a.cancellationReason&&<small>Motivo: {a.cancellationReason}</small>}{controls?.(a)}</article>)}</div>;
}
