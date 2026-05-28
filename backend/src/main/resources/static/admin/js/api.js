export const TOKEN_KEY = 'admin-token';

export let token = localStorage.getItem(TOKEN_KEY) || null;
export let restaurants = [];

export function setToken(t)         { token = t; }
export function clearToken()        { token = null; }
export function setRestaurants(arr) { restaurants = arr; }

export function apiFetch(path, opts = {}) {
  return fetch(path, {
    ...opts,
    headers: { 'X-Admin-Token': token || '', ...(opts.headers || {}) },
  });
}
