const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..');
const file = name => path.join(root, 'src', name);
const read = name => fs.readFileSync(file(name), 'utf8');
let html = read('index.html');
const css = read('style.css');
const core = read('core.js');
const app = read('app.js');

if (!/<style\b[^>]*>[\s\S]*?<\/style>/i.test(html)) throw new Error('Balise style introuvable dans src/index.html.');
html = html.replace(/<style\b[^>]*>[\s\S]*?<\/style>/i, `<style>${css}</style>`);
const scripts = [core, app];
let inline = 0;
html = html.replace(/(<script\b(?![^>]*\bsrc\s*=)[^>]*>)[\s\S]*?<\/script>/gi, (block, open) => {
  if (inline >= scripts.length) return block;
  return `${open}${scripts[inline++]}</script>`;
});
if (inline !== scripts.length) throw new Error(`Deux blocs JavaScript intégrés étaient attendus, ${inline} trouvé(s).`);

fs.writeFileSync(file('index.html'), html);
fs.writeFileSync(path.join(root, 'Biznako.html'), html);
const dist = path.join(root, 'dist');
fs.mkdirSync(dist, { recursive: true });
fs.writeFileSync(path.join(dist, 'index.html'), html);
fs.writeFileSync(path.join(dist, 'Biznako.html'), html);
console.log('HTML autonome synchronisé : src/index.html et dist/Biznako.html');
