/* Biznako domain model. Pure functions: no DOM, network or storage. Monetary units: whole MGA. */
(function(root){'use strict';
const tables=['activities','contacts','products','lots','sales','deals','payments','expenses'];
const uid=()=> 'b'+Date.now().toString(36)+Math.random().toString(36).slice(2,10);
const today=()=>{const d=new Date();return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;};
const clone=o=>JSON.parse(JSON.stringify(o));
function empty(){return {schema:1,revision:0,updatedAt:null,settings:{name:'Biznako',lang:'fr',theme:'light',assistant:true,onboard:false},statuses:[{id:'draft',label:'Brouillon',effect:'draft'},{id:'valid',label:'Validé',effect:'valid'},{id:'cancel',label:'Annulé',effect:'cancel'}],activities:[{id:'yogurt',name:'Yaourt artisanal',kind:'goods'},{id:'property',name:'Immobilier',kind:'service'}],contacts:[],products:[],lots:[],sales:[],deals:[],payments:[],expenses:[],audit:[],filters:[]};}
const get=(s,t,id)=>s[t].find(x=>x.id===id);
const effect=(s,r)=>s.statuses.find(x=>x.id===r.status)?.effect;
const total=r=>Math.round(r.lines.reduce((n,l)=>n+l.qty*l.price,0));
const commission=r=>Math.round(r.base*r.rate/100*r.share/100);
const paid=(s,type,id)=>s.payments.filter(p=>p.type===type&&p.ref===id).reduce((n,p)=>n+p.amount,0);
const stock=(s,id,except)=>(s.lots.find(l=>l.id===id).qty-(s.lots.find(l=>l.id===id).loss||0))-s.sales.filter(r=>r.id!==except&&effect(s,r)==='valid').reduce((n,r)=>n+r.lines.filter(l=>l.lot===id).reduce((m,l)=>m+l.qty,0),0);
function assert(ok,msg){if(!ok)throw Error(msg);}
function num(v,min=0,max=1e12){return typeof v==='number'&&Number.isFinite(v)&&v>=min&&v<=max;}
function date(v){return typeof v==='string'&&/^\d{4}-\d{2}-\d{2}$/.test(v)&&!isNaN(Date.parse(v))&&new Date(v).toISOString().slice(0,10)===v;}
function text(v,max=500){return typeof v==='string'&&v.trim().length>0&&v.length<=max;}
function validate(s){
assert(s&&s.schema===1,'Version de sauvegarde non prise en charge.');
assert(s.settings&&['fr','mg'].includes(s.settings.lang)&&['light','dark'].includes(s.settings.theme)&&text(s.settings.name,100),'Préférences invalides.');
assert(Array.isArray(s.statuses)&&s.statuses.length>=3&&s.statuses.length<=50,'Statuts invalides.');
const statusIds=new Set();s.statuses.forEach(x=>{assert(text(x.id,100)&&!statusIds.has(x.id)&&text(x.label,60)&&['draft','valid','cancel'].includes(x.effect),'Statut invalide.');statusIds.add(x.id);});
['draft','valid','cancel'].forEach(x=>assert(s.statuses.some(t=>t.effect===x),'Une catégorie de statut est absente.'));
assert(Array.isArray(s.audit)&&s.audit.length<=5000&&Array.isArray(s.filters)&&s.filters.length<=100,'Journal ou filtres invalides.');
s.audit.forEach(a=>assert(text(a.id,100)&&text(a.action,200)&&text(a.at,60),'Journal invalide.'));
s.filters.forEach(f=>{assert(text(f.id,100)&&text(f.name,60)&&text(f.view,30)&&f.query&&typeof f.query==='object','Filtre invalide.');for(const k of ['q','activity','status','from','to','sort'])assert(typeof f.query[k]==='string'&&f.query[k].length<500,'Filtre invalide.');assert(!f.query.from||date(f.query.from),'Date de filtre invalide.');assert(!f.query.to||date(f.query.to),'Date de filtre invalide.');});
for(const t of tables){assert(Array.isArray(s[t])&&s[t].length<=20000,'Table absente ou trop volumineuse : '+t);const ids=new Set();for(const r of s[t]){assert(r&&text(r.id,100)&&!ids.has(r.id),'Identifiant absent ou dupliqué : '+t);ids.add(r.id);if(r.note!==undefined)assert(typeof r.note==='string'&&r.note.length<=3000,'Note trop longue.');}}
s.activities.forEach(r=>assert(text(r.name,100)&&['goods','service'].includes(r.kind),'Activité invalide.'));
s.contacts.forEach(r=>assert(text(r.name,100)&&typeof r.phone==='string'&&r.phone.length<=60,'Contact invalide.'));
s.products.forEach(r=>assert(text(r.name,100)&&get(s,'activities',r.activity)&&num(r.price)&&Number.isInteger(r.price)&&num(r.threshold)&&typeof r.unit==='string','Produit invalide.'));
s.lots.forEach(r=>assert(get(s,'products',r.product)&&text(r.name,100)&&num(r.qty,1,1000000)&&Number.isInteger(r.qty)&&num(r.loss??0,0,r.qty)&&Number.isInteger(r.loss??0)&&num(r.cost)&&Number.isInteger(r.cost)&&date(r.date)&&(!r.expiry||date(r.expiry))&&(!r.expiry||r.expiry>=r.date),'Lot invalide : quantité, dates ou produit.'));
for(const t of ['sales','deals','expenses'])for(const r of s[t]){assert(date(r.date)&&statusIds.has(r.status)&&get(s,'activities',r.activity),'Date, activité ou statut invalide.');assert(!r.contact||get(s,'contacts',r.contact),'Contact absent.');assert(!r.due||date(r.due),'Échéance invalide.');}
s.sales.forEach(r=>{assert(Array.isArray(r.lines)&&r.lines.length>0&&r.lines.length<=100,'Ajoutez au moins une ligne.');r.lines.forEach(l=>{const lot=get(s,'lots',l.lot),p=lot&&get(s,'products',lot.product);assert(lot&&p&&p.activity===r.activity&&num(l.qty,1,1000000)&&Number.isInteger(l.qty)&&num(l.price)&&Number.isInteger(l.price),'Ligne de vente invalide.');if(effect(s,r)==='valid')assert(lot.date<=r.date&&(!lot.expiry||lot.expiry>=r.date),'Lot indisponible ou périmé à la date de vente.');});assert(total(r)<=1e12,'Total trop élevé.');});
s.deals.forEach(r=>assert(text(r.name,150)&&num(r.base)&&Number.isInteger(r.base)&&num(r.rate,0,100)&&num(r.share,0,100),'Dossier ou commission invalide.'));
s.expenses.forEach(r=>assert(text(r.name,150)&&num(r.amount,1)&&Number.isInteger(r.amount),'Dépense invalide.'));
s.payments.forEach(r=>{const t=r.type==='sale'?'sales':r.type==='deal'?'deals':null;const ref=t&&get(s,t,r.ref);assert(ref&&effect(s,ref)==='valid'&&num(r.amount,1)&&Number.isInteger(r.amount)&&date(r.date)&&r.date>=ref.date&&['cash','mobile','bank'].includes(r.method),'Encaissement invalide : document validé et date postérieure requis.');});
for(const t of ['sales','deals'])for(const r of s[t])assert(paid(s,t==='sales'?'sale':'deal',r.id)<=(t==='sales'?total(r):commission(r)),'Les encaissements dépassent le montant du document.');
s.lots.forEach(l=>assert(stock(s,l.id)>=0,'Stock insuffisant pour le lot '+l.name));
assert(num(s.revision)&&Number.isInteger(s.revision),'Révision invalide.');return s;
}
function metrics(s,query={}){
const scope=r=>(!query.activity||r.activity===query.activity)&&(!query.from||r.date>=query.from)&&(!query.to||r.date<=query.to);
const sales=s.sales.filter(r=>effect(s,r)==='valid'&&scope(r));const deals=s.deals.filter(r=>effect(s,r)==='valid'&&scope(r));
const payments=s.payments.filter(p=>{const r=get(s,p.type==='sale'?'sales':'deals',p.ref);return (!query.activity||r.activity===query.activity)&&(!query.from||p.date>=query.from)&&(!query.to||p.date<=query.to);});
const expenses=s.expenses.filter(r=>effect(s,r)==='valid'&&scope(r));
const revenue=sales.reduce((n,r)=>n+total(r),0),fees=deals.reduce((n,r)=>n+commission(r),0),received=payments.reduce((n,p)=>n+p.amount,0),out=expenses.reduce((n,r)=>n+r.amount,0);
const cost=sales.reduce((n,r)=>n+r.lines.reduce((m,l)=>m+l.qty*get(s,'lots',l.lot).cost,0),0);
const pending=sales.reduce((n,r)=>n+total(r)-paid(s,'sale',r.id),0)+deals.reduce((n,r)=>n+commission(r)-paid(s,'deal',r.id),0);
return {revenue,fees,received,out,cash:received-out,pending,margin:revenue-cost,cost,sales,deals,payments,expenses};}
function commit(s,table,record,action='save'){
assert(tables.includes(table),'Table inconnue.');const next=clone(s);const i=next[table].findIndex(r=>r.id===record.id);
if(action==='delete'){assert(i>=0,'Élément absent.');next[table].splice(i,1);}else if(i<0)next[table].push(record);else next[table][i]=record;
next.revision++;next.updatedAt=new Date().toISOString();next.audit.unshift({id:uid(),at:next.updatedAt,action:`${action==='delete'?'Suppression':i<0?'Création':'Modification'} · ${table}`,record:record.id});next.audit=next.audit.slice(0,5000);return validate(next);
}
function restore(raw){assert(typeof raw==='string'&&raw.length<10*1024*1024,'Sauvegarde trop volumineuse (10 Mo maximum).');let s;try{s=JSON.parse(raw);}catch{throw Error('Le fichier JSON est illisible.');}return validate(s);}
function csv(rows){const cell=v=>{let x=String(v??'');if(/^[=+@\-\t\r]/.test(x))x="'"+x;return '"'+x.replace(/"/g,'""')+'"';};return '\ufeff'+rows.map(r=>r.map(cell).join(';')).join('\r\n');}
function demo(){const s=empty(),d=today();s.settings.onboard=true;s.settings.demo=true;s.contacts=[{id:'c1',name:'Rasoa (démo)',phone:'034 00 000 00'},{id:'c2',name:'Client immobilier (démo)',phone:''}];s.products=[{id:'p1',name:'Yaourt nature 125 ml',activity:'yogurt',price:1500,unit:'pot',threshold:20},{id:'p2',name:'Yaourt vanille 250 ml',activity:'yogurt',price:2500,unit:'pot',threshold:15}];const future=new Date();future.setDate(future.getDate()+7);const exp=future.toISOString().slice(0,10);s.lots=[{id:'l1',name:'Production A',product:'p1',qty:100,cost:800,date:d,expiry:exp},{id:'l2',name:'Production B',product:'p2',qty:60,cost:1300,date:d,expiry:exp}];s.sales=[{id:'s1',activity:'yogurt',date:d,due:d,contact:'c1',status:'valid',note:'Commande de démonstration',lines:[{lot:'l1',qty:30,price:1500},{lot:'l2',qty:10,price:2500}]}];s.deals=[{id:'d1',name:'Location — appartement Ankorondrano',activity:'property',date:d,due:d,contact:'c2',status:'valid',base:1200000,rate:50,share:50,note:'Exemple : commission partagée à 50 %.'}];s.payments=[{id:'r1',type:'sale',ref:'s1',date:d,amount:50000,method:'mobile'},{id:'r2',type:'deal',ref:'d1',date:d,amount:150000,method:'bank'}];s.expenses=[{id:'e1',name:'Production des lots A et B',activity:'yogurt',date:d,status:'valid',amount:158000,note:'Coût des 160 pots produits, payé.'},{id:'e2',name:'Déplacement visite',activity:'property',date:d,status:'valid',amount:12000}];return validate(s);}
const api={tables,uid,today,clone,empty,get,effect,total,commission,paid,stock,validate,metrics,commit,restore,csv,demo,date};if(typeof module!=='undefined')module.exports=api;root.Biz=api;
})(typeof window!=='undefined'?window:globalThis);
