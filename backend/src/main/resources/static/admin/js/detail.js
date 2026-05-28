import { apiFetch } from './api.js';
import { toast, confirmDialog, showSection } from './ui.js';
import { selectedPhotos, setSelectedPhotos, renderPhotos } from './photos.js';

export let current = null;
export function setCurrent(r) { current = r; }

const EDITABLE_FIELDS = [
  'name', 'cuisineType', 'cuisineEmoji', 'menuPrice', 'priceAlt',
  'menuDetailsRaw', 'includesDessert', 'includesDrink',
  'daysFrom', 'daysTo', 'excludedDay', 'openTime', 'closeTime',
  'phone', 'website', 'googleMapsUrl', 'googlePlaceId', 'hidden',
];
const BOOLEAN_FIELDS = new Set(['includesDessert', 'includesDrink', 'hidden']);
const NUMERIC_FIELDS = new Set(['menuPrice']);
const FORM_ID = {
  name: 'f-name', cuisineType: 'f-cuisine_type', cuisineEmoji: 'f-cuisine_emoji',
  menuPrice: 'f-menu_price', priceAlt: 'f-price_alt', menuDetailsRaw: 'f-menu_details',
  includesDessert: 'f-includes_dessert', includesDrink: 'f-includes_drink',
  daysFrom: 'f-days_from', daysTo: 'f-days_to', excludedDay: 'f-excluded_day',
  openTime: 'f-open_time', closeTime: 'f-close_time',
  phone: 'f-phone', website: 'f-website', googleMapsUrl: 'f-google_maps_url',
  googlePlaceId: 'f-google_place_id', hidden: 'f-hidden',
};

const $ = id => document.getElementById(id);

export async function openDetail(id) {
  const resp = await apiFetch(`/api/v1/admin/restaurants/${id}`);
  if (!resp.ok) { toast('Failed to load restaurant', 'err'); return; }
  current = await resp.json();
  setSelectedPhotos([...(current.photoNames || [])]);
  hydrateDetailForm(current);
  renderPhotos(current);
  showSection('detail-section');
  _activateTab('tab-details');
}

export function hydrateDetailForm(r) {
  $('detail-title').textContent = r.name;
  $('hidden-badge').hidden = !r.hidden;
  for (const field of EDITABLE_FIELDS) {
    const el = $(FORM_ID[field]);
    if (!el) continue;
    if (BOOLEAN_FIELDS.has(field)) el.checked = !!r[field];
    else el.value = r[field] == null ? '' : String(r[field]);
  }
  $('g-address').value = r.address || '';
  $('g-lat').value = r.lat ?? '';
  $('g-lng').value = r.lng ?? '';
  $('g-rating').value = r.rating != null ? r.rating : '';
  $('g-userRatingCount').value = r.userRatingCount != null ? r.userRatingCount : '';
  $('g-editorial').value = r.editorialSummary || '';
  $('g-aiSummary').value = r.aiSummary || '';
  $('g-openingHours').value = _fmtHours(r.openingHours);
  $('g-reviews').value = _fmtReviews(r.reviews);
  $('g-attributes').value = _fmtAttrs(r);
  $('g-placesFetchedAt').value = r.placesFetchedAt || '';
}

function _readForm() {
  const payload = {};
  for (const field of EDITABLE_FIELDS) {
    const el = $(FORM_ID[field]);
    if (!el) continue;
    if (BOOLEAN_FIELDS.has(field)) payload[field] = el.checked;
    else if (NUMERIC_FIELDS.has(field)) payload[field] = el.value === '' ? null : Number(el.value);
    else payload[field] = el.value;
  }
  return payload;
}

export async function onSaveDetail() {
  if (!current) return;
  const btn = $('save-detail-btn');
  btn.disabled = true;
  btn.classList.add('loading');

  const resp = await apiFetch(`/api/v1/admin/restaurants/${current.id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(_readForm()),
  });
  if (!resp.ok) {
    btn.disabled = false; btn.classList.remove('loading');
    let msg = 'Save failed (' + resp.status + ')';
    try { const e = await resp.json(); if (e.message) msg = e.message; } catch (_) {}
    toast(msg, 'err');
    return;
  }
  current = await resp.json();
  window.dispatchEvent(new CustomEvent('restaurant-saved', { detail: current }));

  const pr = await apiFetch(`/api/v1/admin/restaurants/${current.id}/photos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ photoNames: selectedPhotos }),
  });
  if (pr.ok) {
    current = await pr.json();
    window.dispatchEvent(new CustomEvent('restaurant-saved', { detail: current }));
  }

  hydrateDetailForm(current);
  btn.disabled = false; btn.classList.remove('loading');
  toast('Saved!', 'ok');
}

export async function onEnrich() {
  if (!current) return;
  const btn = $('enrich-btn');
  const savedAt = current.updatedAt;
  btn.disabled = true; btn.classList.add('loading');

  const resp = await apiFetch(`/api/v1/admin/restaurants/${current.id}/enrich`, { method: 'POST' });
  if (!resp.ok) {
    btn.disabled = false; btn.classList.remove('loading');
    toast('Enrich failed: ' + resp.status, 'err');
    return;
  }
  toast('Enriching…', 'info');
  _pollEnrichment(current.id, savedAt, btn);
}

function _pollEnrichment(id, savedAt, btn) {
  const MAX = 30_000, TICK = 2_000, start = Date.now();
  const tick = async () => {
    if (Date.now() - start > MAX) {
      btn.disabled = false; btn.classList.remove('loading');
      toast('Enrichment running in background', 'info');
      return;
    }
    const pr = await apiFetch(`/api/v1/admin/restaurants/${id}`, { cache: 'no-store' });
    if (pr.ok) {
      const fresh = await pr.json();
      if (fresh.updatedAt !== savedAt) {
        current = fresh;
        window.dispatchEvent(new CustomEvent('restaurant-saved', { detail: current }));
        setSelectedPhotos([...(current.photoNames || [])]);
        hydrateDetailForm(current);
        renderPhotos(current);
        btn.disabled = false; btn.classList.remove('loading');
        toast('Enriched!', 'ok');
        return;
      }
    }
    setTimeout(tick, TICK);
  };
  setTimeout(tick, TICK);
}

export async function onDelete() {
  if (!current) return;
  await deleteById(current.id, current.name, true);
}

export async function deleteById(id, name, fromDetail = false) {
  const ok = await confirmDialog(`Delete "${name}"? This cannot be undone.`, 'Delete');
  if (!ok) return;
  const resp = await apiFetch(`/api/v1/admin/restaurants/${id}`, { method: 'DELETE' });
  if (!resp.ok) { toast('Delete failed: ' + resp.status, 'err'); return; }
  if (fromDetail) current = null;
  window.dispatchEvent(new CustomEvent('restaurant-deleted', { detail: { id } }));
}

export function openAddModal() {
  $('add-name').value = '';
  $('add-place-id').value = '';
  $('add-error').hidden = true;
  $('add-modal').hidden = false;
  $('add-name').focus();
}

export function closeAddModal() { $('add-modal').hidden = true; }

export async function onCreateSubmit(e) {
  e.preventDefault();
  const name = $('add-name').value.trim();
  const placeId = $('add-place-id').value.trim();
  if (!name || !placeId) return;
  const resp = await apiFetch('/api/v1/admin/restaurants', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, googlePlaceId: placeId }),
  });
  if (!resp.ok) {
    $('add-error').textContent = 'Create failed: ' + resp.status;
    $('add-error').hidden = false;
    return;
  }
  const created = await resp.json();
  window.dispatchEvent(new CustomEvent('restaurant-created', { detail: created }));
  closeAddModal();
  openDetail(created.id);
}

function _activateTab(tabId) {
  document.querySelectorAll('.tab-pane').forEach(p => { p.hidden = true; });
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  const pane = document.getElementById(tabId);
  const btn = document.querySelector(`[data-tab="${tabId}"]`);
  if (pane) pane.hidden = false;
  if (btn) btn.classList.add('active');
}

function _fmtHours(oh) {
  if (!oh || typeof oh !== 'object') return '';
  const w = oh.weekdayDescriptions;
  return Array.isArray(w) ? w.join('\n') : JSON.stringify(oh, null, 2);
}

function _fmtReviews(reviews) {
  if (!Array.isArray(reviews) || !reviews.length) return '';
  return reviews.slice(0, 5).map(r => {
    const author = r.authorName || 'anonymous';
    const rating = r.rating != null ? ` [${r.rating}★]` : '';
    const text = (r.text || r.originalText || '').slice(0, 200);
    return `${author}${rating} — ${text}`;
  }).join('\n\n');
}

function _fmtAttrs(r) {
  const flags = [];
  if (r.servesLunch) flags.push('lunch');
  if (r.servesVegetarian) flags.push('vegetarian');
  if (r.outdoorSeating) flags.push('outdoor');
  if (r.reservable) flags.push('reservable');
  if (r.takeout) flags.push('takeout');
  return flags.join(', ');
}
