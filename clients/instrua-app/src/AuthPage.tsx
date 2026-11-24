import type { Dispatch, FormEvent, SetStateAction } from "react";
import "./auth-pages.css";

export type AuthMode = "login" | "empresa" | "cliente";
export type AuthFormData = {name:string;email:string;password:string;companyName:string;slug:string};
export default function AuthPage({mode,form,setForm,onSubmit,onNavigate,busy,apiAvailable}:{
  mode:AuthMode;form:AuthFormData;setForm:Dispatch<SetStateAction<AuthFormData>>;
  onSubmit:(e:FormEvent<HTMLFormElement>)=>void;
  onNavigate:(route:string)=>void;busy:boolean;apiAvailable:boolean;
}){
  const isLogin=mode==="login";
  const isCompany=mode==="empresa";
  const pageTitle=isLogin?"Que bom ter você de volta.":isCompany?"Seu negócio merece leveza.":"Sua jornada começa aqui.";
  const pageDescription=isLogin?"Tudo no seu lugar, do jeito que você precisa.":"Um novo espaço para cuidar do tempo, dos atendimentos e das conexões.";
  return <div className="ins-auth-page">
    <header className="ins-auth-header"><button type="button" className="ins-auth-logo" onClick={()=>onNavigate("/")} aria-label="Voltar para a página inicial"><img src="/brand-icon.svg" alt=""/><span><b>instrua</b><small>Orientação, conexão e confiança.</small></span></button><button type="button" className="ins-auth-back" onClick={()=>onNavigate("/")}>← Voltar ao início</button></header>
    <main className="ins-auth-layout">
      <aside className="ins-auth-art" aria-label="Bem-vindo ao Instrua">
        <div className="ins-auth-swoosh"/><span className="ins-auth-sparkle" aria-hidden="true">✳</span>
        <div className="ins-auth-art-copy"><span className="ins-auth-eyebrow">INSTRUA · KIYUBI TECHNOLOGIES</span><h2>Organizar também pode ser <em>uma forma de cuidar.</em></h2><p>Mais conexão. Mais confiança. Mais espaço para o que realmente importa.</p></div>
        <div className="ins-auth-floating" aria-hidden="true"><span>✓</span><div><strong>Tudo conectado</strong><small>Sua rotina, seu tempo, seu Instrua.</small></div></div>
      </aside>
      <section className="ins-auth-main" aria-labelledby="ins-auth-title">
        <div className="ins-auth-main-head"><span className="ins-auth-eyebrow">{isLogin?"ENTRAR NA PLATAFORMA":isCompany?"CONTA DE ESTABELECIMENTO":"CONTA PESSOAL"}</span><h1 id="ins-auth-title">{pageTitle}</h1><p>{pageDescription}</p></div>
        <form className="ins-auth-form" onSubmit={onSubmit} autoComplete="on">
          {!isLogin&&<label>Seu nome completo<input required autoComplete="name" placeholder="Como podemos chamar você?" value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label>}
          {isCompany&&<><label>Nome do estabelecimento<input required placeholder="Nome da sua empresa" value={form.companyName} onChange={e=>setForm({...form,companyName:e.target.value})}/></label><label>Identificador exclusivo<input required pattern="[a-z0-9]+(-[a-z0-9]+)*" title="Apenas letras minúsculas, números e hífens" placeholder="minha-clinica" value={form.slug} onChange={e=>setForm({...form,slug:e.target.value.toLowerCase()})}/><span className="ins-auth-tip">Este nome identifica seu estabelecimento no Instrua.</span></label></>}
          <label>Seu e-mail<input type="email" required autoComplete="email" placeholder="voce@exemplo.com" value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></label>
          <label>Sua senha<input type="password" required minLength={isLogin?1:8} autoComplete={isLogin?"current-password":"new-password"} placeholder={isLogin?"Digite sua senha":"Mínimo de 8 caracteres"} value={form.password} onChange={e=>setForm({...form,password:e.target.value})}/></label>
          <button className="ins-auth-submit" type="submit" disabled={busy||!apiAvailable}>{busy?"Conectando com segurança…":isLogin?"Entrar no Instrua":isCompany?"Criar conta do estabelecimento":"Criar minha conta"} <span aria-hidden="true">↗</span></button>
          {!apiAvailable&&<p role="alert" className="ins-auth-notice">Servidor temporariamente indisponível para cadastro. Tente novamente mais tarde.</p>}
        </form>
        {isLogin?<div className="ins-auth-footer"><p>Ainda não tem uma conta?</p><div><button onClick={()=>onNavigate("/cadastro/pessoa")}>Sou cliente</button><button onClick={()=>onNavigate("/cadastro/empresa")}>Sou profissional autônomo</button><button onClick={()=>onNavigate("/cadastro/empresa")}>Tenho um estabelecimento</button></div></div>:<div className="ins-auth-footer"><p>Já tem uma conta?</p><button onClick={()=>onNavigate("/entrar")}>Entrar com meu e-mail →</button></div>}
        {!isLogin&&isCompany&&<p className="ins-auth-privacy">Profissional autônomo: use o nome do seu consultório ou negócio para criar seu espaço de trabalho. Profissional de um estabelecimento existente: crie uma conta pessoal e aceite o convite da equipe.</p>}
        <p className="ins-auth-privacy">Seus dados de autenticação são enviados à API segura; nenhuma senha é armazenada nesta página.</p>
      </section>
    </main>
    {busy&&<div className="ins-auth-loading" role="status" aria-live="polite" aria-label="Autenticando na plataforma">
      <div className="ins-auth-loading-card"><img src="/brand-icon.svg" alt=""/><span className="ins-auth-loading-dots" aria-hidden="true"><i/><i/><i/></span><strong>Conectando ao Instrua</strong><p>Preparando seu espaço com cuidado…</p></div>
    </div>}
    <footer className="ins-auth-bottom">Instrua por Kiyubi Technologies · Orientação, conexão e confiança.</footer>
  </div>;
}
