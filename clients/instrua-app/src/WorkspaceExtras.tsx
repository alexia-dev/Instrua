import {useState} from "react";
import type {Session,Company} from "./production-api";
import type {Module} from "./ModulesHub";
import "./workspace-extras.css";

type SettingsPage="dados"|"seguranca"|"privacidade"|"notificacoes"|"acessibilidade"|"organizacao";
const pages:{id:SettingsPage;name:string;icon:string;note:string}[]=[
  {id:"dados",name:"Dados pessoais",icon:"♙",note:"Sua identidade e seus contatos"},
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
export function SettingsWorkspace({session,company,context}:{session:Session;company?:Company;context:"personal"|"business"}){
 const [page,setPage]=useState<SettingsPage>("dados");
 const active=pages.find(p=>p.id===page)!;
 return <section className="ins-extras ins-settings">
    <header className="ins-extras-hero ins-settings-header">
      <span className="ins-extras-hero-icon" aria-hidden="true">⚙</span>
      <div><p className="ins-extras-eyebrow">MEU INSTRUA · CONTROLE E PREFERÊNCIAS</p><h2>Configurações</h2><p>Seus dados, suas preferências e a organização do seu espaço, com clareza e privacidade.</p></div>
    </header>
    <div className="ins-settings-grid">
      <nav aria-label="Categorias de configurações" className="ins-settings-nav">
        {pages.map(p=><button type="button" key={p.id} className={p.id===page?"selected":""} aria-current={p.id===page?"page":undefined} onClick={()=>setPage(p.id)}>
          <span aria-hidden="true">{p.icon}</span><span><strong>{p.name}</strong><small>{p.note}</small></span>
        </button>)}
      </nav>
      <section className="ins-settings-content" aria-label={active.name}>
        <div className="ins-extras-heading"><div><span className="ins-extras-kicker">MINHAS PREFERÊNCIAS</span><h3>{active.name}</h3><p>{active.note}</p></div>{page==="dados"||page==="organizacao"?<Status ready/>:<Status/>}</div>
        {page==="dados"&&<>
          <div className="ins-settings-fields">
            <div><span>Nome da conta</span><strong>{session.name}</strong></div>
            <div><span>E-mail vinculado</span><strong>{session.email}</strong></div>
            <div><span>Tipo(s) de acesso</span><strong>{session.roles.map(r=>roles[r]||r).join(" · ")}</strong></div>
          </div>
          <p className="ins-extras-bottom">Essas informações são exibidas a partir da conta autenticada. A edição do perfil ainda não está habilitada aqui.</p>
          <FutureFeature title="Ficha pessoal reutilizável" body="Endereço, telefone, responsáveis e informações necessárias aos agendamentos, preenchidos uma única vez e compartilhados apenas com autorização."/>
          <FutureFeature title="Documentos e verificação" body="Documentos e prova de vida apenas quando necessários, com armazenamento seguro e acesso restrito."/>
        </>}
        {page==="seguranca"&&<>
          <FutureFeature title="Senha, recuperação e autenticação em duas etapas" body="Alteração segura de credenciais, proteção de sessões e verificações adicionais."/>
          <FutureFeature title="Acessos autorizados" body="Gerenciar convites, unidades, vínculos profissionais e dispositivos."/>
        </>}
        {page==="privacidade"&&<>
          <FutureFeature title="Permissões para compartilhar dados" body="Ver quais estabelecimentos podem acessar quais informações, com finalidade e prazo definidos."/>
          <FutureFeature title="Histórico, exportação e solicitações" body="Solicitar correções, consultar acessos e exercer direitos previstos na LGPD."/>
        </>}
        {page==="notificacoes"&&<>
          <FutureFeature title="WhatsApp, e-mail e notificações" body="Escolher canais autorizados, lembretes, frequência e silêncio em determinados horários."/>
          <FutureFeature title="Lembretes de atendimentos e preparo" body="Confirmações e orientações no momento correto, com confirmação de recebimento quando aplicável."/>
        </>}
        {page==="acessibilidade"&&<>
          <FutureFeature title="Tema, tamanho da fonte e contraste" body="Preferências de leitura, cores e navegação adaptadas a diferentes necessidades."/>
          <FutureFeature title="Idiomas e assistência" body="Idioma, comunicação acessível e facilidades para pessoas com menor familiaridade digital."/>
        </>}
        {page==="organizacao"&&<>
          {context==="business"&&company?<div className="ins-settings-fields"><div><span>Estabelecimento selecionado</span><strong>{company.name}</strong></div><div><span>Fuso horário da unidade</span><strong>{company.timezone}</strong></div></div>:<p className="ins-extras-bottom">Suas organizações e vínculos aparecerão aqui conforme as permissões da conta.</p>}
          <FutureFeature title="Disponibilidade e capacidade" body="Horários de funcionamento, salas, bloqueios, encaixes e limites configuráveis por profissional."/>
          <FutureFeature title="Módulos e permissões" body="Ativar recursos por unidade e setor, com perfis de acesso separados e auditoria."/>
        </>}
      </section>
    </div>
  </section>;
}
