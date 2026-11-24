import type { MouseEvent } from "react";
import "./site-home.css";
import SiteHomeDetails from "./SiteHomeDetails";

type Navigate = (path: string) => void;
const features = [
  { icon:"◷", title:"Agenda que acompanha você", detail:"Organize profissionais, serviços e atendimentos em um espaço único.", tone:"sage" },
  { icon:"✓", title:"Confirmações sem confusão", detail:"Acompanhe o status real de cada agendamento no painel.", tone:"sand" },
  { icon:"♧", title:"Cada pessoa no seu lugar", detail:"Cliente, equipe e estabelecimento com acessos próprios.", tone:"clay" },
  { icon:"↗", title:"Do celular ao computador", detail:"A mesma conta, a mesma API e as informações de sempre.", tone:"green" }
];
const goals = [
  {name:"Recepção e clínicas", description:"Encontre o atendimento certo, acompanhe a agenda e reduza retrabalho."},
  {name:"Prestadores de serviço", description:"Profissionais, clientes e horários com uma organização mais leve."},
  {name:"Pessoas e famílias", description:"Um lugar para consultar e solicitar os próprios agendamentos."}
];
export default function SiteHome({onNavigate}:{onNavigate:Navigate}){
  function route(e:MouseEvent<HTMLAnchorElement>,path:string){e.preventDefault();onNavigate(path);}
  return <div className="ins-home">
    <header className="ins-landing-header">
      <a className="ins-wordmark" href="#/" onClick={e=>route(e,"/")} aria-label="Instrua, página inicial"><img src="/brand-icon.svg" alt=""/><span><b>instrua</b><small>por Kiyubi Technologies</small></span></a>
      <nav className="ins-landing-links" aria-label="Navegação pública">
        <button type="button" onClick={()=>document.getElementById("recursos")?.scrollIntoView({behavior:"smooth"})}>Recursos</button><button type="button" onClick={()=>document.getElementById("jornadas")?.scrollIntoView({behavior:"smooth"})}>Para quem</button><button type="button" onClick={()=>document.getElementById("ecossistema")?.scrollIntoView({behavior:"smooth"})}>Módulos</button><button type="button" onClick={()=>document.getElementById("perguntas")?.scrollIntoView({behavior:"smooth"})}>Perguntas</button>
      </nav>
      <a className="ins-login-link" href="#/entrar" onClick={e=>route(e,"/entrar")}>Entrar <span aria-hidden="true">↗</span></a>
    </header>
    <main>
      <section className="ins-hero" aria-labelledby="ins-hero-title">
        <div className="ins-hero-copy">
          <div className="ins-kicker"><span className="ins-kicker-dot"/> BEM-VINDO AO SEU PRÓXIMO CAPÍTULO</div>
          <h1 id="ins-hero-title">Confirma, orienta e <em>organiza seus atendimentos.</em></h1>
          <p>Agenda, confirmações, orientações e gestão em um só ecossistema. Um espaço acolhedor para clientes e ferramentas completas para profissionais, recepção e estabelecimentos.</p>
          <div className="ins-hero-actions">
            <a className="ins-button ins-button-primary" href="#/cadastro/empresa" onClick={e=>route(e,"/cadastro/empresa")}>Começar com meu negócio <span aria-hidden="true">↗</span></a>
            <a className="ins-button ins-button-light" href="#/cadastro/pessoa" onClick={e=>route(e,"/cadastro/pessoa")}>Sou cliente <span aria-hidden="true">→</span></a>
          </div>
          <div className="ins-hero-foot"><span><strong>Uma só plataforma.</strong> Várias possibilidades.</span><span className="ins-hero-divider"/><span>Feita para caber na sua rotina.</span></div>
        </div>
        <div className="ins-hero-art" aria-hidden="true">
          <div className="ins-orbit ins-orbit-one"/>
          <div className="ins-orbit ins-orbit-two"/>
          <div className="ins-blob-main">
            <span className="ins-blob-flower">✳</span>
            <div className="ins-hero-inside"><span className="ins-small-top">SEU DIA, EM HARMONIA</span><div className="ins-illustration-card"><span className="ins-card-day">SEG · TER · QUA · QUI · SEX</span><span className="ins-illustration-line"><i/>Atendimentos organizados</span><span className="ins-illustration-line"><i/>Pessoas conectadas</span><span className="ins-illustration-line"><i/>Seu tempo valorizado</span></div><img src="/brand-icon.svg" alt=""/></div>
          </div>
          <span className="ins-sparkle ins-sparkle-one">✦</span><span className="ins-sparkle ins-sparkle-two">✻</span>
          <div className="ins-leaf-one"/>
        </div>
      </section>
      <section id="recursos" className="ins-features-wrap" aria-labelledby="ins-features-title">
        <div className="ins-section-heading"><div><span className="ins-section-eyebrow">O QUE JÁ ESTAMOS CONSTRUINDO</span><h2 id="ins-features-title">Mais fluidez. <em>Menos esforço.</em></h2></div><p>Organize a operação com um sistema pensado tanto para a recepção quanto para quem chega do outro lado.</p></div>
        <div className="ins-feature-grid">{features.map(item=><article key={item.title} className={"ins-feature-card ins-tone-"+item.tone}><span className="ins-feature-icon" aria-hidden="true">{item.icon}</span><h3>{item.title}</h3><p>{item.detail}</p></article>)}</div>
        <p className="ins-disclosure">A plataforma está em evolução. As funções disponíveis dependem do backend configurado; automações externas e módulos adicionais só serão anunciados como ativos após validação.</p>
      </section>
      <section className="ins-manifesto">
        <div className="ins-manifesto-art" aria-hidden="true"><span>in</span><span className="ins-manifesto-curve">✳</span><div className="ins-manifesto-circle"/></div>
        <div className="ins-manifesto-copy"><span className="ins-section-eyebrow">A NOSSA IDEIA É SIMPLES</span><h2>Tecnologia que <em>acolhe.</em> Organização que <em>liberta.</em></h2><p>Chega de repetir informações, procurar horários perdidos e alternar entre sistemas desconectados. O Instrua nasceu para colocar as pessoas no centro da experiência.</p><a href="#/cadastro/empresa" onClick={e=>route(e,"/cadastro/empresa")} className="ins-text-link">Conhecer o Instrua para empresas <span aria-hidden="true">↗</span></a></div>
      </section>
      <section id="publicos" className="ins-audience">
        <span className="ins-section-eyebrow">DO SEU JEITO</span><h2>Um lugar para <em>cada jornada.</em></h2>
        <div className="ins-audience-grid">{goals.map((item,i)=><article key={item.name} className="ins-audience-card"><span className="ins-audience-count">0{i+1}</span><h3>{item.name}</h3><p>{item.description}</p></article>)}</div>
      </section>
      <SiteHomeDetails onNavigate={onNavigate}/>
      <section id="caminho" className="ins-cta"><div className="ins-cta-decoration" aria-hidden="true">✳</div><span className="ins-section-eyebrow">VAMOS JUNTOS?</span><h2>Uma rotina mais leve <em>começa aqui.</em></h2><p>Escolha como quer começar. O Instrua cresce junto com seu trabalho, no navegador e no celular.</p><div className="ins-hero-actions"><a className="ins-button ins-button-cream" href="#/cadastro/empresa" onClick={e=>route(e,"/cadastro/empresa")}>Cadastrar minha empresa <span aria-hidden="true">↗</span></a><a className="ins-button ins-button-outline" href="#/entrar" onClick={e=>route(e,"/entrar")}>Já tenho uma conta <span aria-hidden="true">→</span></a></div></section>
    </main>
    <footer className="ins-landing-footer"><a className="ins-wordmark" href="#/" onClick={e=>route(e,"/")}><img src="/brand-icon.svg" alt=""/><span><b>instrua</b><small>Orientação, conexão e confiança.</small></span></a><span>© {new Date().getFullYear()} Kiyubi Technologies · Criado para conectar.</span></footer>
  </div>
}
