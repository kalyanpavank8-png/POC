import axios from 'axios'

/**
 * Axios instance pointing at /api (proxied to http://localhost:8080/api by Vite).
 * The Authorization header is injected dynamically from the Pinia auth store
 * via an interceptor, so every request automatically carries the stored token.
 */
const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

// Attach Basic Auth token on every request
api.interceptors.request.use(config => {
  // Lazy-import to avoid circular dep with the store
  const token = sessionStorage.getItem('dsar_token')
  if (token) {
    config.headers.Authorization = `Basic ${token}`
  }
  return config
})

// ── DSAR Request helpers ──────────────────────────────────────────────────────

export const dsarApi = {
  submitRequest: (payload) => api.post('/requests', payload),
  listRequests:  ()        => api.get('/requests'),
  getRequest:    (id)      => api.get(`/requests/${id}`),
  updateStatus:      (id, payload)     => api.put(`/requests/${id}/status`, payload),
  updateDescription: (id, description) => api.patch(`/requests/${id}/description`, { description }),
}

// ── Audit helpers ─────────────────────────────────────────────────────────────

export const auditApi = {
  getAllLogs:        ()          => api.get('/audit'),
  getLogsForRequest: (requestId) => api.get(`/audit/${requestId}`),
}

export default api
