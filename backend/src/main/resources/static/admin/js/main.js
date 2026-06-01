import { TOKEN_KEY, token, setToken, clearToken, restaurants, setRestaurants, apiFetch } from './api.js';
import { initUi, showSection, toast } from './ui.js';
import { initList, showList, showListSkeleton, renderTable, buildCuisineFilter } from './list.js';
import {
  setCurrent, openDetail,
  onSaveDetail, onEnrich, onDelete,
  openAddModal, closeAddModal, onCreateSubmit,
} from './detail.js';
import {
  openSettings, onEnrichAll, onFindPlaceIds,
  loadCircuitBreakers, onResetCircuitBreakers,
} from './settings.js';

const DAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const $ = id => document.getElementById(id);

initUi();
initList();
_populateDaySelects();
_bindEvents();
_init();

function _bindEvents() {
  $('login-form').addEventListener('submit', _onLoginSubmit);
  $('logout-btn').addEventListener('click', _onLogout);
  $('back-btn').addEventListener('click', () => { showSection('list-section'); renderTable(); });
  $('settings-btn').addEventListener('click', openSettings);
  $('settings-back-btn').addEventListener('click', () => { showSection('list-section'); renderTable(); });
  $('enrich-all-btn').addEventListener('click', onEnrichAll);
  $('find-place-ids-btn').addEventListener('click', onFindPlaceIds);
  $('cb-refresh-btn').addEventListener('click', loadCircuitBreakers);
  $('cb-reset-btn').addEventListener('click', onResetCircuitBreakers);
  $('add-btn').addEventListener('click', openAddModal);
  $('add-cancel').addEventListener('click', closeAddModal);
  $('add-form').addEventListener('submit', onCreateSubmit);
  $('save-detail-btn').addEventListener('click', onSaveDetail);
  $('enrich-btn').addEventListener('click', onEnrich);
  $('delete-btn').addEventListener('click', onDelete);

  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-pane').forEach(p => { p.hidden = true; });
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      const pane = document.getElementById(btn.dataset.tab);
      if (pane) pane.hidden = false;
      btn.classList.add('active');
    });
  });

  window.addEventListener('admin-token-rejected', () => {
    localStorage.removeItem(TOKEN_KEY);
    clearToken();
    $('logout-btn').hidden = true;
    showSection('login-section');
    toast('Session expired — please log in again', 'err');
  });

  window.addEventListener('restaurant-saved', e => {
    const arr = [...restaurants];
    const idx = arr.findIndex(r => r.id === e.detail.id);
    if (idx >= 0) { arr[idx] = e.detail; setRestaurants(arr); }
  });
  window.addEventListener('restaurant-deleted', e => {
    setRestaurants(restaurants.filter(r => r.id !== e.detail.id));
    setCurrent(null);
    showList();
  });
  window.addEventListener('restaurant-created', e => {
    setRestaurants([...restaurants, e.detail]);
    buildCuisineFilter();
  });
}

async function _onLoginSubmit(e) {
  e.preventDefault();
  const t = $('token-input').value.trim();
  setToken(t);
  const resp = await apiFetch('/api/v1/admin/restaurants');
  if (resp.ok) {
    localStorage.setItem(TOKEN_KEY, t);
    setRestaurants(await resp.json());
    $('login-error').hidden = true;
    $('logout-btn').hidden = false;
    showList();
  } else {
    clearToken();
    $('login-error').textContent = 'Invalid token';
    $('login-error').hidden = false;
  }
}

function _onLogout() {
  localStorage.removeItem(TOKEN_KEY);
  clearToken();
  showSection('login-section');
  $('logout-btn').hidden = true;
}

async function _init() {
  if (!token) { showSection('login-section'); return; }
  showSection('list-section');
  showListSkeleton();
  const resp = await apiFetch('/api/v1/admin/restaurants');
  if (!resp.ok) { showSection('login-section'); return; }
  setRestaurants(await resp.json());
  $('logout-btn').hidden = false;
  showList();
}

function _populateDaySelects() {
  for (const id of ['f-days_from', 'f-days_to', 'f-excluded_day']) {
    const sel = $(id);
    for (const d of DAYS) {
      const opt = document.createElement('option');
      opt.value = d; opt.textContent = d;
      sel.appendChild(opt);
    }
  }
}
