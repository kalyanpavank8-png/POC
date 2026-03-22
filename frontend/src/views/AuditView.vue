<template>
  <div class="container-fluid py-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold mb-0">
          <i class="bi bi-journal-text me-2 text-primary"></i>Compliance Audit Trail
        </h3>
        <p class="text-muted small mb-0">Complete immutable log of all DSAR actions</p>
      </div>
      <div class="d-flex gap-2">
        <input v-model="search" type="text" class="form-control form-control-sm"
               placeholder="Filter by user or action…" style="width:220px" />
        <button class="btn btn-sm btn-outline-secondary" @click="fetchLogs">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary"></div>
      <p class="text-muted mt-2">Loading audit log…</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

    <!-- Empty -->
    <div v-else-if="filtered.length === 0 && !loading" class="card text-center py-5">
      <div class="card-body">
        <div class="display-4 text-muted mb-2"><i class="bi bi-journal-x"></i></div>
        <h5 class="text-muted">No audit entries yet</h5>
        <p class="text-muted small">Audit entries appear here as requests are submitted and processed.</p>
      </div>
    </div>

    <!-- Audit log table -->
    <div v-else class="card shadow-sm">
      <div class="card-header">
        <span class="fw-semibold">{{ filtered.length }} entries</span>
        <span v-if="search" class="text-muted ms-2 small">· filtered</span>
      </div>
      <div class="table-responsive">
        <table class="table table-hover table-sm align-middle mb-0">
          <thead class="table-dark">
            <tr>
              <th>Timestamp</th>
              <th>Action</th>
              <th>Performed By</th>
              <th>Request ID</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in filtered" :key="log.id">
              <td class="text-muted small text-nowrap">{{ formatDate(log.timestamp) }}</td>
              <td>
                <span class="badge" :class="actionBadge(log.action)">
                  <i class="bi me-1" :class="actionIcon(log.action)"></i>
                  {{ log.action }}
                </span>
              </td>
              <td>
                <i class="bi bi-person-circle me-1 text-muted"></i>{{ log.performedBy }}
              </td>
              <td>
                <code class="small text-muted">{{ log.requestId.substring(0, 8) }}…</code>
              </td>
              <td class="small">{{ log.details }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card-footer text-muted small">
        <i class="bi bi-info-circle me-1"></i>
        Audit entries are append-only and cannot be modified or deleted.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { auditApi } from '../api/index.js'

const logs    = ref([])
const loading = ref(false)
const error   = ref(null)
const search  = ref('')

const filtered = computed(() => {
  if (!search.value.trim()) return logs.value
  const q = search.value.toLowerCase()
  return logs.value.filter(l =>
    l.performedBy.toLowerCase().includes(q) ||
    l.action.toLowerCase().includes(q) ||
    l.details.toLowerCase().includes(q)
  )
})

async function fetchLogs() {
  loading.value = true
  error.value   = null
  try {
    const { data } = await auditApi.getAllLogs()
    logs.value = data
  } catch {
    error.value = 'Failed to load audit log.'
  } finally {
    loading.value = false
  }
}

function actionBadge(action) {
  if (action === 'REQUEST_SUBMITTED') return 'bg-success'
  if (action === 'STATUS_UPDATED')    return 'bg-primary'
  return 'bg-secondary'
}
function actionIcon(action) {
  if (action === 'REQUEST_SUBMITTED') return 'bi-plus-circle'
  if (action === 'STATUS_UPDATED')    return 'bi-arrow-repeat'
  return 'bi-activity'
}
function formatDate(d) {
  return d ? new Date(d).toLocaleString() : '—'
}

onMounted(fetchLogs)
</script>
