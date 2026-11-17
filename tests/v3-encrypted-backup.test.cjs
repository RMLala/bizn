const test=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const {webcrypto}=require('node:crypto');
const {TextEncoder,TextDecoder}=require('node:util');
const html=fs.readFileSync('src/index.html','utf8');
const start=html.indexOf('// AppForge V3: optional encrypted export');
const end=html.indexOf('function strip(html)',start);
assert.ok(start>=0&&end>start,'AppForge V3 backup code missing');
const source=html.slice(start,end);
function api(password){
  const context=vm.createContext({
    crypto:webcrypto,TextEncoder,TextDecoder,Uint8Array,Array,JSON,
    btoa,atob,Error,confirm:()=>false,prompt:()=>password
  });
  vm.runInContext(source,context,{timeout:3000});
  return {
    encrypt:raw=>vm.runInContext('encryptedBackup('+JSON.stringify(raw)+','+JSON.stringify(password)+')',context),
    decrypt:raw=>vm.runInContext('decodedBackup('+JSON.stringify(raw)+')',context)
  };
}
test('encrypted backup roundtrip retains accented FR/MG data',async()=>{
  const raw=JSON.stringify({clients:[{name:'Épicerie Antananarivo',note:'Misaotra'}],amount:250000});
  const a=api('correct-horse-battery-passphrase');
  const encrypted=await a.encrypt(raw);
  const envelope=JSON.parse(encrypted);
  assert.equal(envelope.format,'appforge-v3-aesgcm');
  assert.equal(envelope.version,1);
  assert.equal(envelope.iterations,310000);
  assert.notEqual(encrypted.includes('Épicerie'),true);
  assert.equal(await a.decrypt(encrypted),raw);
});
test('legacy JSON remains importable without migration',async()=>{
  const old='{"clients":[{"name":"Ancien client"}]}';
  assert.equal(await api('unused').decrypt(old),old);
});
test('wrong password cannot decrypt',async()=>{
  const encrypted=await api('correct-horse-battery-passphrase').encrypt('{"amount":100}');
  await assert.rejects(api('incorrect-password').decrypt(encrypted),/Mot de passe incorrect/);
});
test('tampering with encrypted data is rejected',async()=>{
  const encrypted=JSON.parse(await api('correct-horse-battery-passphrase').encrypt('{"amount":100}'));
  const data=Buffer.from(encrypted.ciphertext,'base64');
  data[0]^=1;encrypted.ciphertext=data.toString('base64');
  await assert.rejects(api('correct-horse-battery-passphrase').decrypt(JSON.stringify(encrypted)),/Mot de passe incorrect/);
});
