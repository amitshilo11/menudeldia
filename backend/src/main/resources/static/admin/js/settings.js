import { apiFetch } from './api.js';
import { toast, showSection } from './ui.js';

const $ = id => document.getElementById(id);

export function openSettings() {
  showSection('settings-section');
  $('upload-csv-result').textContent = '';
  $('enrich-all-result').textContent = '';
  $('find-place-ids-result').textContent = '';
  loadCircuitBreakers();
}

export async function onUploadCsv() {
  const fileInput = $('csv-file-input');
  if (!fileInput.files || !fileInput.files[0]) {
    toast('Select a CSV file first', 'err');
    return;
  }
  const btn = $('upload-csv-btn');
  btn.disabled = true; btn.classList.add('loading');
  $('upload-csv-result').textContent = '';
  toast('Uploading…', 'info');

  const fd = new FormData();
  fd.append('file', fileInput.files[0]);
  const resp = await apiFetch('/api/v1/admin/restaurants/sync-csv', { method: 'POST', body: fd });
  btn.disabled = false; btn.classList.remove('loading');
  if (!resp.ok) { toast('Upload failed: ' + resp.status, 'err'); return; }

  const r = await resp.json();
  $('upload-csv-result').textContent =
    `created ${r.created} · updated ${r.updated} · skipped ${r.skipped}`;
  toast(`CSV imported: ${r.created} created, ${r.updated} updated`, r.skipped ? 'err' : 'ok');
  fileInput.value = '';
  window.dispatchEvent(new CustomEvent('restaurants-reloaded'));
}

export async function onEnrichAll() {
  const btn = $('enrich-all-btn');
  btn.disabled = true; btn.classList.add('loading');
  $('enrich-all-result').textContent = '';
toast('Enriching…', 'info');

  const resp = await apiFetch('/api/v1/admin/enrich', { method: 'POST' });
  btn.disabled = false; btn.classList.remove('loading');
  if (!resp.ok) { toast('Enrich failed: ' + resp.status, 'err'); return; }

  const r = await resp.json();
  $('enrich-all-result').textContent =
    `attempted ${r.attempted} · succeeded ${r.succeeded} · failed ${r.failed}` +
    (r.failureReason ? ` (${r.failureReason})` : '');
  toast(`Enriched ${r.succeeded}/${r.attempted}`, r.failed ? 'err' : 'ok');
}

export async function onFindPlaceIds() {
  const btn = $('find-place-ids-btn');
  btn.disabled = true; btn.classList.add('loading');
  $('find-place-ids-result').textContent = '';
  toast('Searching…', 'info');

  const resp = await apiFetch('/api/v1/admin/find-place-ids', { method: 'POST' });
  btn.disabled = false; btn.classList.remove('loading');
  if (!resp.ok) { toast('Search failed: ' + resp.status, 'err'); return; }

  const r = await resp.json();
  $('find-place-ids-result').textContent = `found ${r.found} place ID(s)`;
  toast(`Found ${r.found} place ID(s)`, 'ok');
}

export async function loadCircuitBreakers() {
  const resp = await apiFetch('/api/v1/admin/circuit-breakers');
  if (!resp.ok) { $('cb-status').textContent = 'Failed to load: ' + resp.status; return; }
  $('cb-status').textContent = JSON.stringify(await resp.json(), null, 2);
}

export async function onResetCircuitBreakers() {
  const resp = await apiFetch('/api/v1/admin/circuit-breakers/reset', { method: 'POST' });
  if (!resp.ok) { toast('Reset failed: ' + resp.status, 'err'); return; }
  toast('Circuit breakers reset', 'ok');
  loadCircuitBreakers();
}
