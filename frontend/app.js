const state={token:null,companies:[],companyId:null,patients:[],appointments:[],services:[],team:[],user:null,apiUrl:localStorage.getItem("instrua_api")||"http://127.0.0.1:8080"};

const $=id=>document.getElementById(id);

async function api(path,options={}){
  const headers={"Content-Type":"application/json",...(options.headers||{})};
  if(state.token) headers.Authorization="Bearer "+state.token;
  const r=await fetch(state.apiUrl+path,{...options,headers});
  let data=null; try{data=await r.json()}catch{}
  if(!r.ok) throw new Error(data?.message||data?.error||"Erro ao comunicar com a API.");
  return data;
}

async function route(name){
  document.querySelectorAll(".page").forEach(p=>p.classList.add("hidden"));
  const target=$(name+"Page"); if(!target)return;
  target.classList.remove("hidden");
  document.querySelectorAll("[data-route]").forEach(b=>b.classList.toggle("active",b.dataset.route===name));
  try{
    if(name==="dashboard") await loadDashboard();
    if(name==="patients"){await loadPatients();renderPatients();}
    if(name==="agenda"){await loadPatients();await loadServices();await loadAppointments();prepareAppointmentForm();}
    if(name==="services"){await loadServices();renderServices();}
    if(name==="team"){await loadTeam();renderTeam();}
    if(name==="instructions"){await loadInstructions();renderInstructions();}
    if(name==="reports"){await loadReport();}
  }catch(e){showPageError(target,e.message)}
}

async function login(e){
  e.preventDefault(); $("loginError").textContent="";
  state.apiUrl=$("apiUrl").value.replace(/\/$/,"");
  localStorage.setItem("instrua_api",state.apiUrl);
  try{
    const data=await api("/api/v1/auth/login",{method:"POST",body:JSON.stringify({email:$("email").value,password:$("password").value})});
    state.token=data.accessToken; state.user=data; await loadSession();
  }catch(err){$("loginError").textContent=err.message}
}

async function loadSession(){
  state.companies=await api("/api/v1/me/organizations");
  state.companyId=state.companies[0]?.id||null;
  const me=await api("/api/v1/me");
  state.user={...state.user,...me};
  $("loginView").classList.add("hidden"); $("contentView").classList.remove("hidden"); $("logoutBtn").classList.remove("hidden");
  $("welcomeName").textContent=state.user?.name||"usuário";
  $("companyInfo").textContent=state.companies[0]?.name||"Nenhuma organização disponível";
  if(state.companyId) await loadPatients();
  await route("dashboard");
}

async function loadDashboard(){
  if(!state.companyId)return;
  await Promise.all([loadPatients(),loadAppointments()]);
  $("companyInfo").textContent=state.companies[0]?.name||"Nenhuma organização disponível";
  const preview=$("agendaPreview");
  preview.innerHTML=state.appointments.length
    ?state.appointments.slice(0,5).map(a=>`<div class="item"><strong>${escapeHtml(a.clientName||"Cliente")}</strong><div class="muted">${formatDate(a.startsAt)} • ${escapeHtml(a.serviceName||"Serviço")}</div></div>`).join("")
    :'<div class="empty">Nenhum agendamento encontrado.</div>';
}

async function loadPatients(){
  if(!state.companyId)return;
  state.patients=await api("/api/v1/companies/"+state.companyId+"/patients");
  $("patientCount").textContent=state.patients.length;
}
function renderPatients(){
  const q=($("patientSearch")?.value||"").toLowerCase();
  const list=state.patients.filter(p=>(p.name||"").toLowerCase().includes(q));
  $("patientList").innerHTML=list.length
    ?list.map(p=>`<div class="item"><strong>${escapeHtml(p.name)}</strong><div class="muted">${escapeHtml(p.phone||p.email||"Sem contato")}</div></div>`).join("")
    :'<div class="card">Nenhum cliente encontrado.</div>';
}

async function loadAppointments(){
  if(!state.companyId)return;
  state.appointments=await api("/api/v1/companies/"+state.companyId+"/appointments");
  $("appointmentCount").textContent=state.appointments.length;
  const list=$("agendaList");
  if(!list)return;
  list.innerHTML=state.appointments.length
    ?state.appointments.map(a=>`<div class="item"><strong>${escapeHtml(a.clientName||"Cliente")}</strong><div class="muted">${formatDate(a.startsAt)} • ${escapeHtml(a.serviceName||"Serviço")} • ${escapeHtml(a.status||"")}</div></div>`).join("")
    :'<div class="empty">Nenhum agendamento.</div>';
}

async function loadServices(){
  if(!state.companyId)return;
  state.services=await api("/api/v1/companies/"+state.companyId+"/services");
}
function renderServices(){
  $("serviceList").innerHTML=state.services.length
    ?state.services.map(s=>`<div class="item"><strong>${escapeHtml(s.name)}</strong><div class="muted">${s.durationMinutes||0} min • ${formatMoney(s.price)}</div></div>`).join("")
    :'<div class="empty">Nenhum serviço cadastrado.</div>';
}
function fillServiceSelect(){
  const select=$("appointmentService"); if(!select)return;
  select.innerHTML='<option value="">Selecione um serviço</option>'+state.services.map(s=>`<option value="${s.id}">${escapeHtml(s.name)} — ${formatMoney(s.price)}</option>`).join("");
}

async function loadTeam(){
  if(!state.companyId)return;
  state.team=await api("/api/v1/companies/"+state.companyId+"/employees");
}
function renderTeam(){
  $("teamList").innerHTML=state.team.length
    ?state.team.map(e=>`<div class="item"><strong>${escapeHtml(e.name)}</strong><div class="muted">${escapeHtml(e.title||e.accessRole||"Equipe")} • ${escapeHtml(e.phone||e.email||"Sem contato")}</div></div>`).join("")
    :'<div class="empty">Nenhum profissional cadastrado.</div>';
}

async function loadInstructions(){
  if(!state.companyId)return;
  state.instructions=await api("/api/v1/companies/"+state.companyId+"/instructions");
}
function renderInstructions(){
  const list=state.instructions||[];
  $("instructionsPage").querySelector(".instruction-grid").innerHTML=list.length
    ?list.map(i=>`<div class="card instruction-card"><span>✦</span><h2>${escapeHtml(i.title)}</h2><p>${escapeHtml(i.content)}</p></div>`).join("")
    :'<div class="empty">Nenhuma instrução cadastrada.</div>';
}

async function loadReport(){
  if(!state.companyId)return;
  const r=await api("/api/v1/companies/"+state.companyId+"/reports/summary");
  $("reportSummary").innerHTML=[
    ["Clientes",r.totalClients],["Serviços ativos",r.activeServices],["Agendamentos",r.appointments],["Confirmados",r.confirmed],["Cancelados",r.cancelled],["No-show",r.noShows]
  ].map(([label,value])=>`<div><b>${value}</b><small>${label}</small></div>`).join("");
}

function prepareAppointmentForm(){
  const patient=$("appointmentPatient");
  patient.innerHTML='<option value="">Selecione um cliente</option>'+state.patients.map(p=>`<option value="${p.id}">${escapeHtml(p.name)}</option>`).join("");
  fillServiceSelect();
}
async function createAppointment(e){
  e.preventDefault(); $("appointmentError").textContent="";
  try{
    const local=$("appointmentDate").value;
    if(!local)throw new Error("Informe a data e o horário.");
    await api("/api/v1/companies/"+state.companyId+"/appointments",{method:"POST",body:JSON.stringify({
      clientId:$("appointmentPatient").value,serviceId:$("appointmentService").value,startsAt:new Date(local).toISOString(),notes:$("appointmentNotes").value||null
    })});
    e.target.reset(); await loadAppointments(); alert("Agendamento criado com sucesso.");
  }catch(err){$("appointmentError").textContent=err.message}
}

function escapeHtml(v){return String(v??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c]))}
function formatDate(v){return v?new Date(v).toLocaleString("pt-BR",{dateStyle:"short",timeStyle:"short"}):"Data não informada"}
function formatMoney(v){if(v==null||v==="")return"Preço não informado";return Number(v).toLocaleString("pt-BR",{style:"currency",currency:"BRL"})}
function showPageError(target,message){target.innerHTML=`<div class="card error">Não foi possível carregar esta área: ${escapeHtml(message)}</div>`}

function logout(){
  state.token=null;state.user=null;state.companies=[];state.companyId=null;
  $("contentView").classList.add("hidden");$("loginView").classList.remove("hidden");$("logoutBtn").classList.add("hidden");
}

document.querySelectorAll("[data-route]").forEach(b=>b.addEventListener("click",()=>route(b.dataset.route)));
$("loginForm").addEventListener("submit",login);
$("themeBtn").addEventListener("click",toggleTheme);
$("patientSearch").addEventListener("input",renderPatients);
$("logoutBtn").addEventListener("click",logout);
$("appointmentForm").addEventListener("submit",createAppointment);
applyTheme(localStorage.getItem("instrua_theme")||"dark");

function applyTheme(theme){
  document.body.classList.toggle("light",theme==="light");localStorage.setItem("instrua_theme",theme);
  const b=$("themeBtn");if(b){b.textContent=theme==="light"?"☾":"☀";b.setAttribute("aria-label",theme==="light"?"Usar modo escuro":"Usar modo claro")}
}
function toggleTheme(){applyTheme(document.body.classList.contains("light")?"dark":"light")}
