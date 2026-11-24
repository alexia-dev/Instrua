import {useEffect,useMemo,useState} from "react";
import {api} from "./production-api";
import "./business-overview.css";

type Sales={date:string;ordersToday:number;salesToday:number;currency:string;note:string};
type Post={id:string;body:string;created_at:string;audience:string};
type Props={token:string;companyId:string;canSeeSales:boolean;onSocial:()=>void};
const money=(n:number)=>new Intl.NumberFormat("pt-BR",{style:"currency",currency:"BRL"}).format(Number(n)||0);
const today=(value:string)=>{const d=new Date(value),now=new Date();return d.getDate()===now.getDate()&&d.getMonth()===now.getMonth()&&d.getFullYear()===now.getFullYear();};
export default function BusinessOverviewExtras({token,companyId,canSeeSales,onSocial}:Props){
 const [sales,setSales]=useState<Sales|null>(null);
 const [posts,setPosts]=useState<Post[]>([]);
 const [notice,setNotice]=useState("");
 useEffect(()=>{let active=true;setNotice("");setSales(null);setPosts([]);
  const tasks:Promise<unknown>[]=[
    api<Post[]>("/social/posts",token).then(v=>{if(active)setPosts(v);}).catch(()=>{if(active)setNotice("Feed público temporariamente indisponível.");})
  ];
  if(canSeeSales)tasks.push(api<Sales>("/companies/"+companyId+"/dashboard/sales",token)
    .then(v=>{if(active)setSales(v);}).catch(()=>{if(active)setNotice("Não foi possível carregar o resumo de vendas.");}));
  void Promise.all(tasks);
  return()=>{active=false;};
 },[token,companyId,canSeeSales]);
 const trends=useMemo(()=>{
   const tags=new Map<string,number>();
   for(const p of posts){if(!today(p.created_at))continue;
    for(const m of p.body.matchAll(/#([\p{L}\p{N}_]{2,40})/gu)){
     const tag=m[1].toLowerCase();tags.set(tag,(tags.get(tag)||0)+1);
    }
   }
   return [...tags].sort((a,b)=>b[1]-a[1]).slice(0,5);
 },[posts]);
 return <div className="ins-business-overview">
  <section className="ins-overview-social">
   <header><div><small>✿ ACONTECE NO INSTRUA</small><h2>Instrua Social</h2></div><button type="button" onClick={onSocial}>Abrir a comunidade ↗</button></header>
   {posts.length?<div className="ins-overview-feed">{posts.slice(0,3).map(p=><article key={p.id}><div><span aria-hidden="true">✳</span><strong>Comunidade Instrua</strong></div><p>{p.body}</p></article>)}</div>:
    <p className="ins-overview-empty">Ainda não há publicações públicas disponíveis. As novidades reais vão aparecer aqui.</p>}
   <h3>Em alta hoje</h3>
   {trends.length?<div className="ins-overview-tags">{trends.map(([t,n])=><span key={t}>#{t} <small>{n}</small></span>)}</div>:
     <p className="ins-overview-empty">Nenhuma tendência publicada hoje.</p>}
  </section>
  {canSeeSales&&<section className="ins-overview-sales">
   <header><div><small>✦ GESTÃO DO DIA</small><h2>Vendas do dia</h2></div></header>
   <div className="ins-overview-sales-amount"><span>Total registrado</span><strong>{sales?money(sales.salesToday):"Carregando..."}</strong></div>
   <div className="ins-overview-sales-quantity"><span>Pedidos pagos registrados</span><strong>{sales?.ordersToday??"—"}</strong></div>
   <p>Somente vendas realmente registradas no Instrua entram neste total. Integrações com caixa, convênios e recebimentos externos ainda não estão ativas.</p>
  </section>}
  {notice&&<p role="status" className="ins-overview-warning">{notice}</p>}
 </div>;
}
