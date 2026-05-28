import { apiFetch } from './api.js';

export let selectedPhotos = [];
export function setSelectedPhotos(arr) { selectedPhotos = arr; }

let _current = null;
let _dragSrcIndex = -1;
const blobCache = new Map();

const $ = id => document.getElementById(id);

export function renderPhotos(current) {
  _current = current;
  const selectedList = $('selected-list');
  const availableGrid = $('available-grid');
  selectedList.innerHTML = '';
  availableGrid.innerHTML = '';

  const selectedSet = new Set(selectedPhotos);
  const available = current.availablePhotoNames || [];

  for (let i = 0; i < selectedPhotos.length; i++) {
    const name = selectedPhotos[i];
    const avIdx = available.indexOf(name);
    const card = _makeCard(name, i === 0 ? 'Thumbnail' : null, true, i);
    selectedList.appendChild(card);
    if (avIdx >= 0) _loadBlob(card.querySelector('img'), name, current.id, avIdx);
  }
  for (let i = 0; i < available.length; i++) {
    const name = available[i];
    if (selectedSet.has(name)) continue;
    const card = _makeCard(name, null, false, i);
    availableGrid.appendChild(card);
    _loadBlob(card.querySelector('img'), name, current.id, i);
  }
  _setupDrag();
}

function _makeCard(name, label, isSelected, index) {
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
      renderPhotos(_current);
    });
    card.appendChild(btn);
  } else {
    card.addEventListener('click', () => {
      selectedPhotos.push(name);
      renderPhotos(_current);
    });
  }
  return card;
}

function _setupDrag() {
  const cards = [...$('selected-list').querySelectorAll('.photo-card')];
  cards.forEach((card, i) => {
    card.addEventListener('dragstart', e => {
      _dragSrcIndex = i;
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
      if (_dragSrcIndex === i) return;
      const moved = selectedPhotos.splice(_dragSrcIndex, 1)[0];
      const insertAt = _dragSrcIndex < i ? i - 1 : i;
      selectedPhotos.splice(insertAt, 0, moved);
      renderPhotos(_current);
    });
  });
}

async function _loadBlob(img, name, restaurantId, availIdx) {
  const url = await _fetchBlob(name, restaurantId, availIdx);
  if (url) img.src = url;
}

async function _fetchBlob(name, restaurantId, availIdx) {
  if (blobCache.has(name)) return blobCache.get(name);
  try {
    const resp = await apiFetch(`/api/v1/admin/restaurants/${restaurantId}/available-photos/${availIdx}`);
    if (!resp.ok) return null;
    const url = URL.createObjectURL(await resp.blob());
    blobCache.set(name, url);
    return url;
  } catch { return null; }
}
