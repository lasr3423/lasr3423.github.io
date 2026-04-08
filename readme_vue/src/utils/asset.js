const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8202').replace(/\/$/, '');

export function resolveAssetUrl(path) {
  if (!path) return '/img/no-image.svg';
  if (path.startsWith('http://') || path.startsWith('https://')) return path;
  if (path.startsWith('/uploads/')) return `${API_BASE_URL}${path}`;
  return path;
}
