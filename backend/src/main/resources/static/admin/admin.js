'use strict';

const TOKEN_KEY = 'admin-token';
let token = localStorage.getItem(TOKEN_KEY);
let restaurants = [];
let current = null;
let selectedPhotos = [];
let dragSrcIndex = -1;
const blobCache = new Map();

const $ = id => document.getElementById(id);
const loginSection  = $('login-section');
const listSection   = $('list-section');
const curationSection = $('curation-section');
const logoutBtn     = $('logout-btn');
const loginForm     = $('login-form');
const loginError    = $('login-error');
const searchInput   = $('search-input');
const tbody         = $('restaurant-tbody');
const curationTitle = $('curation-title');
const selectedList  = $('selected-list');
const availableGrid = $('available-grid');
const saveBtn       = $('save-btn');
const saveStatus    = $('save-status');

loginForm.addEventListener('submit', async e => {
  e.preventDefault();
  const t = $('token-input').value.trim();
  const resp = await apiFetch('/api/v1/admin/restaurants', t);
  if (resp.ok) {
    token = t;
    localStorage.setItem(TOKEN_KEY, token);
    restaurants = await resp.json();
    showList();
  } else {
    loginError.textContent = 'Invalid token';
    loginError.hidden = false;
  }
});

logoutBtn.addEventListener('click', () => {
  localStorage.removeItem(TOKEN_KEY);
  token = null;
  showLogin();
});

searchInput.addEventListener('input', () => renderTable(searchInput.value));

$('back-btn').addEventListener('click', () => {
  showSection(listSection);
});

saveBtn.addEventListener('click', async () => {
  saveStatus.textContent = 'Saving…';
  const resp = await apiFetch(`/api/v1/admin/restaurants/${current.id}/photos`, token, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ photoNames: selectedPhotos }),
  });
  if (resp.ok) {
    const updated = await resp.json();
    current.photoNames = updated.photoNames;
    const idx = restaurants.findIndex(r => r.id === current.id);
    if (idx >= 0) restaurants[idx] = { ...restaurants[idx], photoNames: updated.photoNames };
    saveStatus.textContent = 'Saved!';
    setTimeout(() => { saveStatus.textContent = ''; }, 2000);
  } else {
    saveStatus.textContent = 'Error saving.';
  }
});

async function init() {
  if (!token) { showLogin(); return; }
  const resp = await apiFetch('/api/v1/admin/restaurants', token);
  if (!resp.ok) { showLogin(); return; }
  restaurants = await resp.json();
  showList();
}

function showLogin() {
  showSection(loginSection);
  logoutBtn.hidden = true;
}

function showList() {
  showSection(listSection);
  logoutBtn.hidden = false;
  renderTable('');
}

function showSection(section) {
  for (const s of [loginSection, listSection, curationSection]) s.hidden = s !== section;
}

function renderTable(filter) {
  const q = filter.toLowerCase();
  const rows = q ? restaurants.filter(r => r.name.toLowerCase().includes(q)) : restaurants;
  tbody.innerHTML = '';
  for (const r of rows) {
    const tr = document.createElement('tr');
    const thumbSrc = r.photoNames.length > 0
      ? `/api/v1/restaurants/${r.id}/photos/0`
      : '';
    tr.innerHTML = `
      <td>${thumbSrc ? `<img class="thumb" src="${thumbSrc}" alt="">` : ''}</td>
      <td>${escHtml(r.name)}</td>
      <td>${r.photoNames.length} / ${r.availablePhotoNames.length}</td>
    `;
    tr.addEventListener('click', () => openCuration(r));
    tbody.appendChild(tr);
  }
}

function openCuration(r) {
  current = r;
  selectedPhotos = [...r.photoNames];
  curationTitle.textContent = r.name;
  showSection(curationSection);
  renderCuration();
}

function renderCuration() {
  selectedList.innerHTML = '';
  availableGrid.innerHTML = '';
  const selectedSet = new Set(selectedPhotos);

  for (let i = 0; i < selectedPhotos.length; i++) {
    const name = selectedPhotos[i];
    const avIdx = current.availablePhotoNames.indexOf(name);
    const card = makeCard(name, i === 0 ? 'Thumbnail' : null, true, i);
    selectedList.appendChild(card);
    if (avIdx >= 0) loadBlob(card.querySelector('img'), name, current.id, avIdx);
  }

  for (let i = 0; i < current.availablePhotoNames.length; i++) {
    const name = current.availablePhotoNames[i];
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
      renderCuration();
    });
    card.appendChild(btn);
  } else {
    card.addEventListener('click', () => {
      selectedPhotos.push(name);
      renderCuration();
    });
  }
  return card;
}

function setupDrag() {
  const cards = [...selectedList.querySelectorAll('.photo-card')];
  cards.forEach((card, i) => {
    card.addEventListener('dragstart', e => { dragSrcIndex = i; e.dataTransfer.setData('text/plain', String(i)); e.dataTransfer.effectAllowed = 'move'; card.classList.add('dragging'); });
    card.addEventListener('dragend',   () => card.classList.remove('dragging'));
    card.addEventListener('dragover',  e => { e.preventDefault(); e.dataTransfer.dropEffect = 'move'; card.classList.add('drag-over'); });
    card.addEventListener('dragleave', () => card.classList.remove('drag-over'));
    card.addEventListener('drop', e => {
      e.preventDefault();
      card.classList.remove('drag-over');
      if (dragSrcIndex === i) return;
      const moved = selectedPhotos.splice(dragSrcIndex, 1)[0];
      const insertAt = dragSrcIndex < i ? i - 1 : i;
      selectedPhotos.splice(insertAt, 0, moved);
      renderCuration();
    });
  });
}

async function loadBlob(img, name, restaurantId, availIdx) {
  const url = await fetchBlob(name, restaurantId, availIdx);
  if (url) img.src = url;
}

async function fetchBlob(name, restaurantId, availIdx) {
  if (blobCache.has(name)) return blobCache.get(name);
  try {
    const resp = await apiFetch(`/api/v1/admin/restaurants/${restaurantId}/available-photos/${availIdx}`, token);
    if (!resp.ok) return null;
    const url = URL.createObjectURL(await resp.blob());
    blobCache.set(name, url);
    return url;
  } catch { return null; }
}

function apiFetch(path, tok, opts = {}) {
  return fetch(path, {
    ...opts,
    headers: { 'X-Admin-Token': tok, ...(opts.headers || {}) },
  });
}

function escHtml(str) {
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

init();
