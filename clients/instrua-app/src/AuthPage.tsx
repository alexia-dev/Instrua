import {useState,type Dispatch,type FormEvent,type SetStateAction} from "react";
import type {Company,Session} from "./production-api";
import "./auth-pages.css";
import "./auth-organic.css";

export type AuthMode="login"|"empresa"|"cliente";
export type AuthFormData={name:string;email:string;password:string;companyName:string;slug:string};
export type ContextRole={companyId:string;role:string;employeeId:string|null};
const personLabel=(role:string)=>({
 COMPANY_OWNER:"Proprietário(a)",COMPANY_ADMIN:"Administração",RECEPTION:"Recepção",CLINICAL:"Profissional",BILLING:"Financeiro",PLATFORM_ADMIN:"Administração da plataforma"
} as Record<string,string>)[role]||"Vínculo profissional";

function Icon({name}:{name:string}){return <span aria-hidden="true" className="ioa-icon material-symbols-outlined">{name}</span>}

export default function AuthPage({mode,form,setForm,onSubmit,onNavigate,busy,apiAvailable}:{
 mode:AuthMode;form:AuthFormData;setForm:Dispatch<SetStateAction<AuthFormData>>;
 onSubmit:(e:FormEvent<HTMLFormElement>)=>void;onNavigate:(route:string)=>void;busy:boolean;apiAvailable:boolean;
}){
 const [showPassword,setShowPassword]=useState(false);
 const isLogin=mode==="login",isCompany=mode==="empresa";
 const heading=isLogin?"Bem-vindo ao Instrua":isCompany?"Seu espaço, um novo começo.":"Sua jornada começa aqui.";
 return <div className="ioa-page">
  <header className="ioa-header"><button type="button" className="ioa-brand" onClick={()=>onNavigate("/")}><img src="/brand-icon.svg" alt=""/><span><strong>instrua</strong><small>por Kiyubi Technologies</small></span></button><button type="button" className="ioa-back" onClick={()=>onNavigate("/")}><Icon name="arrow_back"/> Voltar para o início</button></header>
  <main className="ioa-main">
   <div className="ioa-title-row"><div><span className="ioa-eyebrow"><span className="ioa-status-dot"/> SUA IDENTIDADE NO INSTRUA</span><h1>{heading}</h1></div><span className="ioa-security"><Icon name="shield_person"/> Acesso protegido · Cada contexto com suas permissões</span></div>
   <div className="ioa-grid">
    <section className="ioa-login-card" aria-labelledby="ioa-form-title">
     <div className="ioa-botanical" aria-hidden="true">✦</div>
     <div className="ioa-card-top"><span className="ioa-eyebrow">{isLogin?"BOM TE VER DE VOLTA":isCompany?"CADASTRO DE ESTABELECIMENTO":"CADASTRO PESSOAL"}</span><h2 id="ioa-form-title">{isLogin?"Acesse seu ecossistema":isCompany?"Crie seu espaço de trabalho":"Uma conta para sua jornada"}</h2><p>{isLogin?"Entre com sua conta. Depois, você poderá escolher sua jornada pessoal ou qualquer espaço profissional ao qual tenha acesso.":isCompany?"Prepare seu estabelecimento para organizar serviços, equipe e agendamentos.":"Comece com seus dados básicos. Você poderá completar sua ficha pessoal depois de entrar."}</p></div>
     <form className="ioa-form" onSubmit={onSubmit} autoComplete="on">
      {!isLogin&&<label>Nome completo<input type="text" autoComplete="name" required maxLength={255} placeholder="Seu nome completo" value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label>}
      {isCompany&&<><label>Nome do estabelecimento<input type="text" required maxLength={255} placeholder="Seu consultório, empresa ou espaço" value={form.companyName} onChange={e=>setForm({...form,companyName:e.target.value})}/></label><label>Identificador do estabelecimento<input type="text" required pattern="[a-z0-9]+(-[a-z0-9]+)*" title="Use letras minúsculas, números e hífens" placeholder="meu-espaco" value={form.slug} onChange={e=>setForm({...form,slug:e.target.value.toLowerCase()})}/><small>Este identificador faz parte da URL do seu estabelecimento.</small></label></>}
      <label>E-mail pessoal ou profissional<div className="ioa-input-icon"><Icon name="alternate_email"/><input type="email" autoComplete="email" required maxLength={255} placeholder="nome@exemplo.com" value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></div></label>
      <label>Senha de acesso<div className="ioa-input-icon"><Icon name="lock_open"/><input type={showPassword?"text":"password"} autoComplete={isLogin?"current-password":"new-password"} required minLength={isLogin?1:8} placeholder={isLogin?"Digite sua senha":"Crie uma senha de pelo menos 8 caracteres"} value={form.password} onChange={e=>setForm({...form,password:e.target.value})}/><button className="ioa-toggle-pass" type="button" onClick={()=>setShowPassword(v=>!v)} aria-label={showPassword?"Ocultar senha":"Mostrar senha"}><Icon name={showPassword?"visibility_off":"visibility"}/></button></div></label>
      <button className="ioa-submit" type="submit" disabled={busy||!apiAvailable}>{busy?"Conectando com segurança…":isLogin?"Entrar na minha conta":isCompany?"Criar estabelecimento":"Criar minha conta"} <Icon name="arrow_forward"/></button>
      {!apiAvailable&&<p className="ioa-alert" role="alert">A API de cadastro está indisponível no momento.</p>}
     </form>
     <div className="ioa-login-foot">{isLogin?<><span>É sua primeira vez?</span><button type="button" onClick={()=>onNavigate("/cadastro/pessoa")}>Criar conta pessoal</button><button type="button" onClick={()=>onNavigate("/cadastro/empresa")}>Cadastrar estabelecimento</button></>:<><span>Já tem uma conta?</span><button type="button" onClick={()=>onNavigate("/entrar")}>Entrar com meu e-mail <Icon name="arrow_forward"/></button></>}</div>
     <p className="ioa-fineprint">Use seu e-mail e senha reais. Login por CPF, gov.br e biometria serão disponibilizados somente após integração e validação de segurança.</p>
    </section>
    <aside className="ioa-personas" aria-label="Como funciona uma identidade com vários perfis">
     <div className="ioa-personas-head"><span className="ioa-eyebrow"><Icon name="hub"/> IDENTIDADE MULTICONTEXTO</span><h2>Uma pessoa. <em>Várias jornadas.</em></h2><p>Seu acesso é único. Os espaços que você poderá escolher depois de entrar dependem dos convites e permissões vinculados à sua conta.</p></div>
     <div className="ioa-personas-list">
      <article className="ioa-persona"><span className="ioa-persona-icon"><Icon name="favorite"/></span><div><h3>Minha jornada pessoal</h3><small>Agendamentos, calendário, oportunidades e comunidade</small><p>Seu espaço pessoal está sempre disponível na sua conta.</p></div><span className="ioa-example">Pessoal</span></article>
      <article className="ioa-persona"><span className="ioa-persona-icon"><Icon name="medical_services"/></span><div><h3>Meus vínculos profissionais</h3><small>Agenda e atendimentos autorizados</small><p>Quando um estabelecimento convida você, o perfil fica disponível para seleção.</p></div><span className="ioa-example">Com convite</span></article>
      <article className="ioa-persona"><span className="ioa-persona-icon"><Icon name="domain"/></span><div><h3>Gestão e recepção</h3><small>Operação, equipe e serviços</small><p>O Instrua mostra somente as ferramentas permitidas pelo seu cargo.</p></div><span className="ioa-example">Com acesso</span></article>
     </div>
     <div className="ioa-dante-note"><span><Icon name="psychology"/></span><div><strong>Assistente Dante</strong><p>Uma experiência planejada para tornar sua rotina mais simples. As integrações inteligentes ainda estão em desenvolvimento.</p></div></div>
    </aside>
   </div>
  </main>
  <footer className="ioa-footer"><span>© {new Date().getFullYear()} Kiyubi Technologies · Instrua</span><span>Orientação, conexão e confiança.</span></footer>
  {busy&&<div className="ins-auth-loading" role="status" aria-live="polite"><div className="ins-auth-loading-card"><img src="/brand-icon.svg" alt=""/><strong>Conectando ao Instrua</strong><p>Preparando seu espaço com cuidado…</p><div className="ins-auth-loading-dots"><i/><i/><i/></div></div></div>}
 </div>;
}

export function ContextChooser({session,roles,companies,loading,onChoose,onLogout}:{
 session:Session;roles:ContextRole[];companies:Company[];loading:boolean;
 onChoose:(mode:"personal"|"work",companyId?:string)=>void;onLogout:()=>void;
}){
 // Never display fabricated company affiliations or profession credentials.
 const workspaces=roles.filter(r=>r.companyId);
 if(session.roles.includes("COMPANY_OWNER")){
   for(const company of companies){
     if(!workspaces.some(w=>w.companyId===company.id))workspaces.push({companyId:company.id,role:"COMPANY_OWNER",employeeId:null});
   }
 }
 return <div className="ioa-page ioa-context-page">
  <header className="ioa-header"><div className="ioa-brand"><img src="/brand-icon.svg" alt=""/><span><strong>instrua</strong><small>por Kiyubi Technologies</small></span></div><button type="button" className="ioa-back" onClick={onLogout}><Icon name="logout"/> Sair da conta</button></header>
  <main className="ioa-main">
   <div className="ioa-title-row"><div><span className="ioa-eyebrow"><span className="ioa-status-dot"/> SUA CONTA ESTÁ CONECTADA</span><h1>Qual jornada vamos abrir?</h1><p className="ioa-context-intro">Olá, {session.name.split(" ")[0]}! Escolha como quer usar o Instrua agora. Você poderá trocar de perfil quando quiser.</p></div><span className="ioa-security"><Icon name="verified_user"/> Perfis reais vinculados à sua conta</span></div>
   <div className="ioa-context-grid">
    <button type="button" onClick={()=>onChoose("personal")} className="ioa-context-option"><span className="ioa-persona-icon"><Icon name="favorite"/></span><span className="ioa-context-body"><strong>Minha jornada pessoal</strong><small>Meus agendamentos, ficha, orientações, social e calendário</small><em>Sempre disponível</em></span><Icon name="arrow_forward"/></button>
    {workspaces.map(w=><button type="button" onClick={()=>onChoose("work",w.companyId)} key={w.companyId} className="ioa-context-option"><span className="ioa-persona-icon"><Icon name={w.role==="CLINICAL"?"medical_services":w.role==="RECEPTION"?"support_agent":w.role==="BILLING"?"payments":"domain"}/></span><span className="ioa-context-body"><strong>{companies.find(c=>c.id===w.companyId)?.name||"Estabelecimento vinculado"}</strong><small>{personLabel(w.role)} · Área de trabalho</small><em>Acesso sujeito às permissões do estabelecimento</em></span><Icon name="arrow_forward"/></button>)}
    {loading&&<p className="ioa-context-status" role="status">Verificando seus vínculos com os estabelecimentos…</p>}
    {!loading&&workspaces.length===0&&<p className="ioa-context-status">Você ainda não tem um estabelecimento vinculado. Pode continuar pela jornada pessoal e aceitar um convite profissional mais tarde.</p>}
   </div>
  </main><footer className="ioa-footer">Você decide qual contexto usar; o acesso aos dados é validado pelo servidor.</footer>
 </div>;
}
