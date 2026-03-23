<template>
  <div class="container-fluid py-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold mb-0">Admin — All Requests</h3>
        <p class="text-muted small mb-0">Review and process Data Subject Access Requests</p>
      </div>
      <div class="d-flex gap-2 align-items-center">
        <!-- Status filter -->
        <select v-model="filter" class="form-select form-select-sm" style="width:180px">
          <option value="">All statuses</option>
          <option value="PENDING">Pending</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="COMPLETED">Completed</option>
          <option value="REJECTED">Rejected</option>
        </select>
        <button class="btn btn-sm btn-outline-secondary" @click="fetchRequests">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary"></div>
      <p class="text-muted mt-2">Loading…</p>
    </div>

    <!-- Error -->
    <div v-else-if="fetchError" class="alert alert-danger">{{ fetchError }}</div>

    <!-- Table -->
    <div v-else>
      <div class="card shadow-sm">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span class="fw-semibold">
            {{ filtered.length }} request{{ filtered.length !== 1 ? 's' : '' }}
            <span v-if="filter"> · filtered by <strong>{{ filter }}</strong></span>
          </span>
        </div>
        <div class="table-responsive">
          <table class="table table-hover mb-0 align-middle">
            <thead class="table-light">
              <tr>
                <th>ID</th>
                <th>Customer</th>
                <th>Email</th>
                <th>Type</th>
                <th>Description</th>
                <th>Status</th>
                <th>Submitted</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="filtered.length === 0">
                <td colspan="8" class="text-center py-4 text-muted">
                  <i class="bi bi-inbox me-2"></i>No requests match the current filter.
                </td>
              </tr>
              <tr v-for="req in filtered" :key="req.id">
                <td><code class="small">{{ req.id.substring(0, 8) }}…</code></td>
                <td><i class="bi bi-person-circle me-1 text-muted"></i>{{ req.customerId }}</td>
                <td>{{ req.customerEmail }}</td>
                <td>
                  <span class="badge" :class="typeBadge(req.requestType)">
                    {{ req.requestType }}
                  </span>
                </td>
                <td class="small" style="max-width:200px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">
                  {{ req.description }}
                </td>
                <td>
                  <span class="badge" :class="statusBadge(req.status)">{{ req.status }}</span>
                </td>
                <td class="small text-muted">{{ formatDate(req.createdAt) }}</td>
                <td>
                  <button
                    v-if="req.status !== 'COMPLETED' && req.status !== 'REJECTED'"
                    class="btn btn-sm btn-outline-primary"
                    @click="openProcess(req)">
                    <i class="bi bi-gear me-1"></i>Process
                  </button>
                  <span v-else class="text-muted small">—</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- Process Request Modal -->
  <div v-if="selected" class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,.5)">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title fw-bold">
            <i class="bi bi-gear-fill me-2 text-primary"></i>Process Request
          </h5>
          <button type="button" class="btn-close" @click="closeProcess"></button>
        </div>
        <div class="modal-body">
          <div v-if="processError" class="alert alert-danger py-2 small">{{ processError }}</div>

          <!-- Request summary -->
          <div class="bg-light rounded p-3 mb-3 small">
            <div><strong>Customer:</strong> {{ selected.customerId }} ({{ selected.customerEmail }})</div>
            <div><strong>Type:</strong> {{ selected.requestType }}</div>
            <div><strong>Description:</strong> {{ selected.description }}</div>
            <div><strong>Current status:</strong>
              <span class="badge ms-1" :class="statusBadge(selected.status)">{{ selected.status }}</span>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">New Status <span class="text-danger">*</span></label>
            <select v-model="processForm.status" class="form-select" required>
              <option value="" disabled>Select new status…</option>
              <option v-if="selected.status === 'PENDING'" value="IN_PROGRESS">→ In Progress</option>
              <option value="COMPLETED">→ Completed</option>
              <option value="REJECTED">→ Rejected</option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Admin Notes</label>
            <textarea v-model="processForm.adminNotes" class="form-control" rows="3"
                      placeholder="Optional notes visible to the customer…"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeProcess">Cancel</button>
          <button type="button" class="btn btn-primary" @click="handleProcess"
                  :disabled="!processForm.status || processing">
            <span v-if="processing">
              <span class="spinner-border spinner-border-sm me-1"></span>Saving…
            </span>
            <span v-else><i class="bi bi-check2 me-1"></i>Save</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dsarApi } from '../api/index.js'

const requests   = ref([])
const loading    = ref(false)
const fetchError = ref(null)
const filter     = ref('')
const selected   = ref(null)
const processing = ref(false)
const processError = ref(null)
const processForm  = ref({ status: '', adminNotes: '' })

const filtered = computed(() =>
  filter.value
    ? requests.value.filter(r => r.status === filter.value)
    : requests.value
)

async function fetchRequests() {
  loading.value    = true
  fetchError.value = null
  try {
    const { data } = await dsarApi.listRequests()
    requests.value  = data
  } catch {
    fetchError.value = 'Failed to load requests.'
  } finally {
    loading.value = false
  }
}

function openProcess(req) {
  selected.value    = req
  processForm.value = { status: '', adminNotes: req.adminNotes || '' }
  processError.value = null
}

function closeProcess() {
  selected.value     = null
  processError.value = null
}

async function handleProcess() {
  if (!processForm.value.status) return
  processing.value   = true
  processError.value = null
  try {
    await dsarApi.updateStatus(selected.value.id, processForm.value)
    closeProcess()
    await fetchRequests()
  } catch (e) {
    processError.value = e.response?.data?.message || 'Failed to update status.'
  } finally {
    processing.value = false
  }
}

function statusBadge(s) {
  return { PENDING: 'bg-warning text-dark', IN_PROGRESS: 'bg-info text-dark',
           COMPLETED: 'bg-success', REJECTED: 'bg-danger' }[s] || 'bg-secondary'
}
function typeBadge(t) {
  return { ACCESS: 'bg-primary', DELETE: 'bg-danger', CORRECT: 'bg-info text-dark' }[t] || 'bg-secondary'
}
function formatDate(d) {
  return d ? new Date(d).toLocaleString() : '—'
}

onMounted(fetchRequests)
</script>
