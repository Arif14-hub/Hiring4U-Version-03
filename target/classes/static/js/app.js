(() => {
const list=document.getElementById("live-jobs-list"), feedback=document.getElementById("job-feedback");
const search=document.getElementById("job-search"), role=document.getElementById("job-role"), locationInput=document.getElementById("job-location");
const minCtc=document.getElementById("job-min-ctc"), maxCtc=document.getElementById("job-max-ctc"), posted=document.getElementById("job-posted"), sort=document.getElementById("job-sort");
const searchButton=document.getElementById("job-search-button"), clearButton=document.getElementById("job-clear"), summary=document.getElementById("job-filter-summary"), studio=document.querySelector(".job-search");
let timer, requestId=0;
const esc=s=>String(s??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c]));
const initials=s=>(s||"H").split(/\s+/).map(x=>x[0]).join("").slice(0,2).toUpperCase();
const money=v=>Number.isFinite(Number(v)) ? `₹ ${Number(v).toLocaleString("en-IN")}` : "Salary not listed";

function updateSummary(count){
  const active=[];
  if(search?.value.trim()) active.push(`“${search.value.trim()}”`);
  if(role?.value.trim()) active.push(role.value.trim());
  if(locationInput?.value.trim()) active.push(locationInput.value.trim());
  if(minCtc?.value) active.push(`from ${money(minCtc.value)}`);
  if(maxCtc?.value) active.push(`to ${money(maxCtc.value)}`);
  if(posted?.value) active.push(`last ${posted.value} days`);
  summary.textContent=`${count} ${count===1?"opportunity":"opportunities"}${active.length?" · "+active.join(" · "):" · All opportunities"}`;
}

function render(jobs){
 if(!Array.isArray(jobs)||!jobs.length){
   list.innerHTML='<div class="paper-card" style="padding:30px"><h3 class="serif">The canvas is quiet.</h3><p>No opportunities match your current search. Try widening a filter or clearing the canvas.</p></div>';
   updateSummary(0); return;
 }
 updateSummary(jobs.length);
 list.innerHTML=jobs.map(j=>`<article class="job paper-card"><div class="mark">${esc(initials(j.company))}</div><div><h3>${esc(j.title)}</h3><p>${esc(j.company||"Hiring4U partner")} · ${esc(j.location||"India")} · ${esc(money(j.salary))}</p><div class="skills">${esc(j.requireskills||"Software engineering")}</div></div><button class="btn alt" data-apply="${esc(j.id)}">Apply →</button></article>`).join("");
}

function params(){
 const p=new URLSearchParams();
 if(search.value.trim())p.set("search",search.value.trim());
 if(role.value.trim())p.set("role",role.value.trim());
 if(locationInput.value.trim())p.set("location",locationInput.value.trim());
 if(minCtc.value)p.set("minCtc",minCtc.value);
 if(maxCtc.value)p.set("maxCtc",maxCtc.value);
 if(posted.value)p.set("postedWithinDays",posted.value);
 if(sort.value)p.set("sort",sort.value);
 return p;
}

async function load(){
 const id=++requestId;
 studio?.classList.add("busy");
 feedback.textContent="";
 try{
   const q=params().toString();
   const r=await fetch(`/candidate/jobs${q?`?${q}`:""}`,{headers:{Accept:"application/json"},cache:"no-store"});
   if(!r.ok)throw new Error();
   const data=await r.json();
   if(id!==requestId)return;
   render(data);
 }catch(e){
   if(id!==requestId)return;
   list.innerHTML='<div class="paper-card" style="padding:30px"><h3 class="serif">The live canvas is unavailable.</h3><p>We could not reach the Hiring4U jobs service. Please try again.</p></div>';
   updateSummary(0);
   feedback.textContent="Search could not be completed right now.";
 }finally{if(id===requestId)studio?.classList.remove("busy");}
}

function schedule(){clearTimeout(timer);timer=setTimeout(load,350)}
searchButton?.addEventListener("click",e=>{e.preventDefault();clearTimeout(timer);load();document.getElementById("jobs")?.scrollIntoView({behavior:"smooth",block:"start"});});
[search,role,locationInput,minCtc,maxCtc].forEach(x=>x?.addEventListener("input",schedule));
[posted,sort].forEach(x=>x?.addEventListener("change",load));
search?.addEventListener("keydown",e=>{if(e.key==="Enter"){e.preventDefault();clearTimeout(timer);load();}});
clearButton?.addEventListener("click",()=>{[search,role,locationInput,minCtc,maxCtc,posted].forEach(x=>{if(x)x.value=""});sort.value="recent";load();});

list?.addEventListener("click",async e=>{
 const b=e.target.closest("[data-apply]");if(!b)return;
 try{const me=await fetch("/api/auth/me",{headers:{Accept:"application/json"}});if(!me.ok){location.href="/login.html";return}const p=await me.json();if(p.role!=="CANDIDATE"){feedback.textContent="Only candidate accounts can apply.";return}b.disabled=true;b.textContent="Applying…";const r=await fetch("/candidate/apply",{method:"POST",headers:{"Content-Type":"application/json",Accept:"text/plain"},body:JSON.stringify({jobId:b.dataset.apply})});feedback.textContent=await r.text();b.textContent=r.ok?"Applied":"Apply →";b.disabled=!r.ok}catch(_){feedback.textContent="Application could not be submitted."}
});
load();
})();
