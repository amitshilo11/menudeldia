const $ = id => document.getElementById(id);

let _confirmResolve = null;

export function initUi() {
  $('confirm-cancel').addEventListener('click', () => _settle(false));
  $('confirm-ok').addEventListener('click', () => _settle(true));
}

function _settle(value) {
  $('confirm-modal').hidden = true;
  if (_confirmResolve) { _confirmResolve(value); _confirmResolve = null; }
}

export function confirmDialog(msg, okLabel = 'Confirm') {
  $('confirm-msg').textContent = msg;
  $('confirm-ok').textContent = okLabel;
  $('confirm-ok').className = 'danger';
  $('confirm-cancel').hidden = false;
  $('confirm-modal').hidden = false;
  return new Promise(resolve => { _confirmResolve = resolve; });
}

export function alertDialog(msg) {
  const el = $('confirm-msg');
  el.innerHTML = '';
  msg.split('\n').forEach((line, i) => {
    if (i > 0) el.appendChild(document.createElement('br'));
    el.appendChild(document.createTextNode(line));
  });
  $('confirm-ok').textContent = 'Close';
  $('confirm-ok').className = '';
  $('confirm-cancel').hidden = true;
  $('confirm-modal').hidden = false;
  return new Promise(resolve => { _confirmResolve = resolve; });
}

export function toast(msg, type = 'info') {
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.textContent = msg;
  $('toast-container').appendChild(el);
  setTimeout(() => {
    el.classList.add('out');
    el.addEventListener('animationend', () => el.remove());
  }, type === 'err' ? 4500 : 2800);
}

export function showSection(id) {
  for (const s of ['login-section', 'list-section', 'detail-section', 'settings-section']) {
    const el = $(s);
    if (el) el.hidden = s !== id;
  }
}

export function escHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}
