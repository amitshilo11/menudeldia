'use strict';

const TOKEN_KEY = 'admin-token';
const DAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

const EDITABLE_FIELDS = [
  'name', 'cuisineType', 'cuisineEmoji', 'menuPrice', 'priceAlt',
  'menuDetailsRaw', 'includesDessert', 'includesDrink',
  'daysFrom', 'daysTo', 'excludedDay', 'openTime', 'closeTime',
  'phone', 'website', 'googleMapsUrl', 'googlePlaceId', 'hidden',
];
const BOOLEAN_FIELDS = new Set(['includesDessert', 'includesDrink', 'hidden']);
const NUMERIC_FIELDS = new Set(['menuPrice']);
const FORM_ID_BY_FIELD = {
  name: 'f-name', cuisineType: 'f-cuisine_type', cuisineEmoji: 'f-cuisine_emoji',
  menuPrice: 'f-menu_price', priceAlt: 'f-price_alt', menuDetailsRaw: 'f-menu_details',
  includesDessert: 'f-includes_dessert', includesDrink: 'f-includes_drink',
  daysFrom: 'f-days_from', daysTo: 'f-days_to', excludedDay: 'f-excluded_day',
  openTime: 'f-open_time', closeTime: 'f-close_time',
  phone: 'f-phone', website: 'f-website', googleMapsUrl: 'f-google_maps_url',
  googlePlaceId: 'f-google_place_id', hidden: 'f-hidden',
};

let token = localStorage.getItem(TOKEN_KEY);
let restaurants = [];
let current = null;          // full DTO for the currently open restaurant
let selectedPhotos = [];
let dragSrcIndex = -1;
const blobCache = new Map();

const $ = id => document.getElementById(id);

// ---------------------------------------------------------------- bootstrap

populateDaySelects();
bindEvents();
init();

function bindEvents() {
  $('login-form').addEventListener('submit', onLoginSubmit);
  $('logout-btn').addEventListener('click', onLogout);
  $('search-input').addEventListener('input', e => renderTable(e.target.value));
  $('back-btn').addEventListener('click', () => showSection('list-section'));
  $('add-btn').addEventListener('click', openAddModal);
  $('add-cancel').addEventListener('click', closeAddModal);
  $('add-form').addEventListener('submit', onCreateSubmit);
  $('save-detail-btn').addEventListener('click', onSaveDetail);
  $('delete-btn').addEventListener('click', onDelete);
  $('photos-save-btn').addEventListener('click', onSavePhotos);
}

async function init() {
  if (!token) { showSection('login-section'); return; }
  const resp = await apiFetch('/api/v1/admin/restaurants');
  if (!resp.ok) { showSection('login-section'); return; }
  restaurants = await resp.json();
  showList();
}

// ---------------------------------------------------------------- auth

async function onLoginSubmit(e) {
  e.preventDefault();
  const t = $('token-input').value.trim();
  token = t;
  const resp = await apiFetch('/api/v1/admin/restaurants');
  if (resp.ok) {
    localStorage.setItem(TOKEN_KEY, token);
    restaurants = await resp.json();
    $('login-error').hidden = true;
    showList();
  } else {
    token = null;
    $('login-error').textContent = 'Invalid token';
    $('login-error').hidden = false;
  }
}

function onLogout() {
  localStorage.removeItem(TOKEN_KEY);
  token = null;
  showSection('login-section');
  $('logout-btn').hidden = true;
}

// ---------------------------------------------------------------- list

function showList() {
  showSection('list-section');
  $('logout-btn').hidden = false;
  renderTable($('search-input').value || '');
}

function renderTable(filter) {
  const q = filter.toLowerCase();
  const rows = q ? restaurants.filter(r => r.name.toLowerCase().includes(q)) : restaurants;
  const tbody = $('restaurant-tbody');
  tbody.innerHTML = '';
  for (const r of rows) {
    const tr = document.createElement('tr');
    if (r.hidden) tr.classList.add('hidden-row');
    const thumb = (r.photoNames && r.photoNames.length > 0)
      ? `<img class="thumb" src="/api/v1/restaurants/${r.id}/photos/0" alt="">`
      : `<div class="thumb"></div>`;
    const statusPill = r.hidden
      ? `<span class="status-pill hidden">Hidden</span>`
      : `<span class="status-pill visible">Visible</span>`;
    tr.innerHTML = `
      <td>${thumb}</td>
      <td>${escHtml(r.name)}</td>
      <td>${escHtml(r.cuisineType || '')}</td>
      <td>${r.menuPrice != null ? r.menuPrice + ' ' + (r.currency || '€') : ''}</td>
      <td>${(r.photoNames || []).length} / ${(r.availablePhotoNames || []).length}</td>
      <td>${statusPill}</td>
    `;
    tr.addEventListener('click', () => openDetail(r.id));
    tbody.appendChild(tr);
  }
}

// ---------------------------------------------------------------- detail (read + edit)

async function openDetail(id) {
  const resp = await apiFetch(`/api/v1/admin/restaurants/${id}`);
  if (!resp.ok) { setDetailStatus('Failed to load restaurant', 'err'); return; }
  current = await resp.json();
  selectedPhotos = [...(current.photoNames || [])];
  hydrateDetailForm(current);
  renderPhotos();
  showSection('detail-section');
  setDetailStatus('', 'ok');
}

function hydrateDetailForm(r) {
  $('detail-title').textContent = r.name;
  $('hidden-badge').hidden = !r.hidden;

  for (const field of EDITABLE_FIELDS) {
    const el = $(FORM_ID_BY_FIELD[field]);
    if (!el) continue;
    const val = r[field];
    if (BOOLEAN_FIELDS.has(field)) el.checked = !!val;
    else el.value = val == null ? '' : String(val);
  }

  // Google-side read-only fields
  $('g-address').value = r.address || '';
  $('g-lat').value = r.lat ?? '';
  $('g-lng').value = r.lng ?? '';
  $('g-rating').value = r.rating != null ? r.rating : '';
  $('g-userRatingCount').value = r.userRatingCount != null ? r.userRatingCount : '';
  $('g-editorial').value = r.editorialSummary || '';
  $('g-aiSummary').value = r.aiSummary || '';
  $('g-openingHours').value = formatOpeningHours(r.openingHours);
  $('g-reviews').value = formatReviews(r.reviews);
  $('g-attributes').value = formatAttributes(r);
  $('g-placesFetchedAt').value = r.placesFetchedAt || '';
}

function readEditableForm() {
  const payload = {};
  for (const field of EDITABLE_FIELDS) {
    const el = $(FORM_ID_BY_FIELD[field]);
    if (!el) continue;
    if (BOOLEAN_FIELDS.has(field)) payload[field] = el.checked;
    else if (NUMERIC_FIELDS.has(field)) payload[field] = el.value === '' ? null : Number(el.value);
    else payload[field] = el.value;
  }
  return payload;
}

async function onSaveDetail() {
  if (!current) return;
  setDetailStatus('Saving…', 'ok');
  const payload = readEditableForm();
  const resp = await apiFetch(`/api/v1/admin/restaurants/${current.id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!resp.ok) {
    setDetailStatus('Save failed: ' + resp.status, 'err');
    return;
  }
  current = await resp.json();
  // Refresh the list cache so name/cuisine/price changes show on Back.
  const idx = restaurants.findIndex(r => r.id === current.id);
  if (idx >= 0) restaurants[idx] = current;
  hydrateDetailForm(current);
  setDetailStatus('Saved — enrichment refreshing in background', 'ok');
}

async function onDelete() {
  if (!current) return;
  await deleteById(current.id, current.name, true);
}

async function deleteById(id, name, fromDetail = false) {
  if (!confirm(`Delete "${name}"? This cannot be undone.`)) return;
  const resp = await apiFetch(`/api/v1/admin/restaurants/${id}`, { method: 'DELETE' });
  if (!resp.ok) {
    if (fromDetail) setDetailStatus('Delete failed: ' + resp.status, 'err');
    else alert('Delete failed: ' + resp.status);
    return;
  }
  restaurants = restaurants.filter(r => r.id !== id);
  if (fromDetail) { current = null; }
  showList();
}

// ---------------------------------------------------------------- add

function openAddModal() {
  $('add-name').value = '';
  $('add-place-id').value = '';
  $('add-error').hidden = true;
  $('add-modal').hidden = false;
}

function closeAddModal() { $('add-modal').hidden = true; }

async function onCreateSubmit(e) {
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
  restaurants.push(created);
  closeAddModal();
  openDetail(created.id);
}

// ---------------------------------------------------------------- photos

function renderPhotos() {
  const selectedList = $('selected-list');
  const availableGrid = $('available-grid');
  selectedList.innerHTML = '';
  availableGrid.innerHTML = '';
  const selectedSet = new Set(selectedPhotos);
  const available = current.availablePhotoNames || [];

  for (let i = 0; i < selectedPhotos.length; i++) {
    const name = selectedPhotos[i];
    const avIdx = available.indexOf(name);
    const card = makeCard(name, i === 0 ? 'Thumbnail' : null, true, i);
    selectedList.appendChild(card);
    if (avIdx >= 0) loadBlob(card.querySelector('img'), name, current.id, avIdx);
  }
  for (let i = 0; i < available.length; i++) {
    const name = available[i];
    if (selectedSet.has(name)) continue;
    const card = makeCard(name, null, false, i);
    availableGrid.appendChild(card);
    loadBlob(card.querySelector('img'), name, current.id, i);
  }
  setupDrag();
}

function makeCard(name, label, isSelected, index) {
  const card = document.createElement('div');
  card.className = 'photo-card' + (isSelected ? '' : ' available-card');
  card.dataset.name = name;
  if (isSelected) card.draggable = true;

  const img = document.createElement('img');
  img.alt = '';
  img.draggable = false;
  card.appendChild(img);

  if (label) {
    const lbl = document.createElement('span');
    lbl.className = 'photo-label';
    lbl.textContent = label;
    card.appendChild(lbl);
  }
  if (isSelected) {
    const btn = document.createElement('button');
    btn.className = 'remove-btn';
    btn.textContent = '✕';
    btn.addEventListener('click', e => {
      e.stopPropagation();
      selectedPhotos.splice(index, 1);
      renderPhotos();
    });
    card.appendChild(btn);
  } else {
    card.addEventListener('click', () => {
      selectedPhotos.push(name);
      renderPhotos();
    });
  }
  return card;
}

function setupDrag() {
  const cards = [...$('selected-list').querySelectorAll('.photo-card')];
  cards.forEach((card, i) => {
    card.addEventListener('dragstart', e => {
      dragSrcIndex = i;
      e.dataTransfer.setData('text/plain', String(i));
      e.dataTransfer.effectAllowed = 'move';
      card.classList.add('dragging');
    });
    card.addEventListener('dragend', () => card.classList.remove('dragging'));
    card.addEventListener('dragover', e => {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'move';
      card.classList.add('drag-over');
    });
    card.addEventListener('dragleave', () => card.classList.remove('drag-over'));
    card.addEventListener('drop', e => {
      e.preventDefault();
      card.classList.remove('drag-over');
      if (dragSrcIndex === i) return;
      const moved = selectedPhotos.splice(dragSrcIndex, 1)[0];
      const insertAt = dragSrcIndex < i ? i - 1 : i;
      selectedPhotos.splice(insertAt, 0, moved);
      renderPhotos();
    });
  });
}

async function onSavePhotos() {
  if (!current) return;
  $('photos-save-status').textContent = 'Saving…';
  const resp = await apiFetch(`/api/v1/admin/restaurants/${current.id}/photos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ photoNames: selectedPhotos }),
  });
  if (resp.ok) {
    current = await resp.json();
    const idx = restaurants.findIndex(r => r.id === current.id);
    if (idx >= 0) restaurants[idx] = current;
    $('photos-save-status').textContent = 'Saved!';
    setTimeout(() => { $('photos-save-status').textContent = ''; }, 2000);
  } else {
    $('photos-save-status').textContent = 'Error saving.';
  }
}

async function loadBlob(img, name, restaurantId, availIdx) {
  const url = await fetchBlob(name, restaurantId, availIdx);
  if (url) img.src = url;
}

async function fetchBlob(name, restaurantId, availIdx) {
  if (blobCache.has(name)) return blobCache.get(name);
  try {
    const resp = await apiFetch(`/api/v1/admin/restaurants/${restaurantId}/available-photos/${availIdx}`);
    if (!resp.ok) return null;
    const url = URL.createObjectURL(await resp.blob());
    blobCache.set(name, url);
    return url;
  } catch { return null; }
}

// ---------------------------------------------------------------- helpers

function populateDaySelects() {
  for (const id of ['f-days_from', 'f-days_to', 'f-excluded_day']) {
    const sel = $(id);
    for (const d of DAYS) {
      const opt = document.createElement('option');
      opt.value = d; opt.textContent = d;
      sel.appendChild(opt);
    }
  }
}

function formatOpeningHours(oh) {
  if (!oh || typeof oh !== 'object') return '';
  const w = oh.weekdayDescriptions;
  if (Array.isArray(w)) return w.join('\n');
  return JSON.stringify(oh, null, 2);
}

function formatReviews(reviews) {
  if (!Array.isArray(reviews) || reviews.length === 0) return '';
  return reviews.slice(0, 5).map(r => {
    const author = r.authorName || 'anonymous';
    const rating = r.rating != null ? `[${r.rating}★]` : '';
    const text = (r.text || r.originalText || '').slice(0, 200);
    return `${author} ${rating} — ${text}`;
  }).join('\n\n');
}

function formatAttributes(r) {
  const flags = [];
  if (r.servesLunch) flags.push('lunch');
  if (r.servesVegetarian) flags.push('vegetarian');
  if (r.outdoorSeating) flags.push('outdoor');
  if (r.reservable) flags.push('reservable');
  if (r.takeout) flags.push('takeout');
  return flags.join(', ');
}

function setDetailStatus(text, cls) {
  const el = $('detail-status');
  el.textContent = text;
  el.className = cls || '';
}

function showSection(id) {
  for (const s of ['login-section', 'list-section', 'detail-section']) {
    const el = $(s);
    if (el) el.hidden = s !== id;
  }
}

function apiFetch(path, opts = {}) {
  return fetch(path, {
    ...opts,
    headers: { 'X-Admin-Token': token || '', ...(opts.headers || {}) },
  });
}

function escHtml(str) {
  return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
