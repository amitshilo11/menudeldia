import { restaurants } from './api.js';
import { escHtml, showSection } from './ui.js';
import { openDetail } from './detail.js';

let _sortField = '';
let _sortDir = 1;
let _searchQuery = '';
let _filterStatus = '';
let _filterCuisine = '';

const $ = id => document.getElementById(id);

export function initList() {
  $('search-input').addEventListener('input', e => { _searchQuery = e.target.value; renderTable(); });
  $('status-filter').addEventListener('change', e => { _filterStatus = e.target.value; renderTable(); });
  $('cuisine-filter').addEventListener('change', e => { _filterCuisine = e.target.value; renderTable(); });

  document.querySelectorAll('th.sortable').forEach(th => {
    th.addEventListener('click', () => {
      const field = th.dataset.sort;
      if (_sortField === field) _sortDir *= -1;
      else { _sortField = field; _sortDir = 1; }
      _updateSortIndicators();
      renderTable();
    });
  });
}

export function showList() {
  showSection('list-section');
  buildCuisineFilter();
  renderTable();
}

export function buildCuisineFilter() {
  const sel = $('cuisine-filter');
  const prev = sel.value;
  while (sel.options.length > 1) sel.remove(1);
  const cuisines = [...new Set(restaurants.map(r => r.cuisineType).filter(Boolean))].sort();
  for (const c of cuisines) {
    const opt = document.createElement('option');
    opt.value = c; opt.textContent = c;
    sel.appendChild(opt);
  }
  if ([...sel.options].some(o => o.value === prev)) sel.value = prev;
}

export function showListSkeleton() {
  $('restaurant-table').hidden = false;
  $('empty-state').hidden = true;
  $('restaurant-tbody').innerHTML = Array.from({ length: 6 }, () => `
    <tr class="skeleton-tr">
      <td><div class="skeleton thumb-empty"></div></td>
      <td><div class="skeleton w-200"></div></td>
      <td><div class="skeleton w-100"></div></td>
      <td><div class="skeleton w-60"></div></td>
      <td><div class="skeleton w-40"></div></td>
      <td><div class="skeleton w-80"></div></td>
    </tr>`).join('');
}

export function renderTable() {
  const q = _searchQuery.toLowerCase();
  let rows = restaurants.filter(r => {
    if (q && !r.name.toLowerCase().includes(q)) return false;
    if (_filterStatus === 'visible' && r.hidden) return false;
    if (_filterStatus === 'hidden' && !r.hidden) return false;
    if (_filterCuisine && r.cuisineType !== _filterCuisine) return false;
    return true;
  });

  if (_sortField) {
    rows = [...rows].sort((a, b) => {
      let va, vb;
      if (_sortField === 'name') {
        va = (a.name || '').toLowerCase();
        vb = (b.name || '').toLowerCase();
      } else if (_sortField === 'price') {
        va = a.menuPrice ?? Infinity;
        vb = b.menuPrice ?? Infinity;
      } else if (_sortField === 'status') {
        va = a.hidden ? 1 : 0;
        vb = b.hidden ? 1 : 0;
      }
      return va < vb ? -_sortDir : va > vb ? _sortDir : 0;
    });
  }

  const total = rows.length;
  $('result-count').textContent = total + ' restaurant' + (total !== 1 ? 's' : '');

  const tbody = $('restaurant-tbody');
  const table = $('restaurant-table');
  const empty = $('empty-state');

  if (total === 0) {
    tbody.innerHTML = '';
    table.hidden = true;
    empty.hidden = false;
    return;
  }
  table.hidden = false;
  empty.hidden = true;
  tbody.innerHTML = '';

  for (const r of rows) {
    const tr = document.createElement('tr');
    if (r.hidden) tr.classList.add('hidden-row');
    const v = encodeURIComponent(r.updatedAt || r.id);
    const thumb = (r.photoNames && r.photoNames.length > 0)
      ? `<img class="thumb" src="/api/v1/restaurants/${r.id}/photos/0?v=${v}" alt="" loading="lazy">`
      : `<span class="thumb-empty"></span>`;
    const pill = r.hidden
      ? `<span class="status-pill hidden">Hidden</span>`
      : `<span class="status-pill visible">Visible</span>`;
    tr.innerHTML = `
      <td data-label="Photo">${thumb}</td>
      <td data-label="Name">${escHtml(r.name)}</td>
      <td data-label="Cuisine">${escHtml(r.cuisineType || '')}</td>
      <td data-label="Price">${r.menuPrice != null ? r.menuPrice + ' ' + (r.currency || '€') : '—'}</td>
      <td data-label="Photos">${(r.photoNames || []).length} / ${(r.availablePhotoNames || []).length}</td>
      <td data-label="Status">${pill}</td>`;
    tr.addEventListener('click', () => openDetail(r.id));
    tbody.appendChild(tr);
  }
}

function _updateSortIndicators() {
  document.querySelectorAll('th.sortable').forEach(th => {
    const ind = th.querySelector('.sort-ind');
    if (!ind) return;
    ind.textContent = th.dataset.sort === _sortField ? (_sortDir === 1 ? ' ↑' : ' ↓') : '';
  });
}
