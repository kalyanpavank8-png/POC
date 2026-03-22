<template>
  <div class="container py-4">
    <!-- Page header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold mb-0">My Privacy Requests</h3>
        <p class="text-muted small mb-0">Submit and track your Data Subject Access Requests</p>
      </div>
      <button class="btn btn-primary" @click="openNew">
        <i class="bi bi-plus-circle me-2"></i>New Request
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="text-muted mt-2">Loading your requests…</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
    </div>

    <!-- Empty state -->
    <div v-else-if="requests.length === 0" class="card text-center py-5">
      <div class="card-body">
        <div class="display-4 text-muted mb-3"><i class="bi bi-inbox"></i></div>
        <h5 class="text-muted">No requests yet</h5>
        <p class="text-muted small">Click "New Request" to submit your first privacy request.</p>
      </div>
    </div>

    <!-- Request cards -->
    <div v-else class="row g-3">
      <div v-for="req in requests" :key="req.id" class="col-12 col-md-6 col-lg-4">
        <div class="card h-100 shadow-sm">
          <div class="card-header d-flex justify-content-between align-items-center">
            <span class="badge" :class="typeBadge(req.requestType)">
              <i class="bi me-1" :class="typeIcon(req.requestType)"></i>
              {{ req.requestType }}
            </span>
            <span class="badge" :class="statusBadge(req.status)">
              {{ statusLabel(req.status) }}
            </span>
          </div>

          <div class="card-body">
            <p class="card-text">{{ req.description }}</p>
            <p class="text-muted small mb-1">
              <i class="bi bi-envelope me-1"></i>{{ req.customerEmail }}
            </p>
            <p class="text-muted small mb-0">
              <i class="bi bi-calendar me-1"></i>Submitted {{ formatDate(req.createdAt) }}
            </p>
            <p v-if="req.updatedAt !== req.createdAt" class="text-muted small mb-0">
              <i class="bi bi-arrow-repeat me-1"></i>Updated {{ formatDate(req.updatedAt) }}
            </p>
          </div>

          <!-- Admin note -->
          <div v-if="req.adminNotes" class="card-footer bg-warning-subtle small">
            <i class="bi bi-chat-left-text me-1 text-warning"></i>
            <strong>Admin note:</strong> {{ req.adminNotes }}
          </div>

          <!-- Edit button — only for PENDING requests -->
          <div v-if="req.status === 'PENDING'" class="card-footer bg-light d-flex justify-content-end">
            <button class="btn btn-sm btn-outline-secondary" @click="openEdit(req)">
              <i class="bi bi-pencil me-1"></i>Edit Request
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Submit / Edit Modal -->
  <div v-if="showModal" class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,.5)">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title fw-bold">
            <i class="bi me-2 text-primary" :class="editingRequest ? 'bi-pencil-square' : 'bi-plus-circle'"></i>
            {{ editingRequest ? 'Edit Pending Request' : 'New Privacy Request' }}
          </h5>
          <button type="button" class="btn-close" @click="closeModal"></button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="modal-body">
            <div v-if="submitError" class="alert alert-danger py-2 small">{{ submitError }}</div>

            <!-- Info banner when editing -->
            <div v-if="editingRequest" class="alert alert-info py-2 small mb-3">
              <i class="bi bi-info-circle me-1"></i>
              You can update the description of a <strong>Pending</strong> request before it is processed.
            </div>

            <div class="mb-3">
              <label class="form-label fw-semibold">Your Email Address <span class="text-danger">*</span></label>
              <input v-model="form.customerEmail" type="email" class="form-control"
                     placeholder="you@example.com" required
                     :disabled="!!editingRequest" />
            </div>

            <div class="mb-3">
              <label class="form-label fw-semibold">Request Type <span class="text-danger">*</span></label>
              <select v-model="form.requestType" class="form-select" required
                      :disabled="!!editingRequest">
                <option value="" disabled>Select a type…</option>
                <option value="ACCESS">Access my data</option>
                <option value="DELETE">Delete my data</option>
                <option value="CORRECT">Correct my data</option>
              </select>
              <div v-if="editingRequest" class="form-text">Type cannot be changed after submission.</div>
            </div>

            <div class="mb-3">
              <label class="form-label fw-semibold">Description <span class="text-danger">*</span></label>
              <textarea v-model="form.description" class="form-control" rows="4"
                        placeholder="Describe your request in detail…" required></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              <span v-if="submitting">
                <span class="spinner-border spinner-border-sm me-1"></span>Saving…
              </span>
              <span v-else>
                <i class="bi me-1" :class="editingRequest ? 'bi-check2' : 'bi-send'"></i>
                {{ editingRequest ? 'Save Changes' : 'Submit Request' }}
              </span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dsarApi } from '../api/index.js'

const requests      = ref([])
const loading       = ref(false)
const error         = ref(null)
const showModal     = ref(false)
const submitting    = ref(false)
const submitError   = ref(null)
const editingRequest = ref(null)   // holds the request being edited, or null for new

const form = ref({ customerEmail: '', requestType: '', description: '' })

// ── Data fetching ────────────────────────────────────────────────────────────

async function fetchRequests() {
  loading.value = true
  error.value   = null
  try {
    const { data } = await dsarApi.listRequests()
    requests.value = data
  } catch {
    error.value = 'Failed to load requests.'
  } finally {
    loading.value = false
  }
}

// ── Modal open/close ─────────────────────────────────────────────────────────

function openNew() {
  editingRequest.value = null
  form.value = { customerEmail: '', requestType: '', description: '' }
  submitError.value = null
  showModal.value = true
}

function openEdit(req) {
  editingRequest.value = req
  form.value = {
    customerEmail: req.customerEmail,
    requestType:   req.requestType,
    description:   req.description
  }
  submitError.value = null
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingRequest.value = null
  form.value = { customerEmail: '', requestType: '', description: '' }
  submitError.value = null
}

// ── Submit / update ──────────────────────────────────────────────────────────

async function handleSubmit() {
  submitting.value  = true
  submitError.value = null
  try {
    if (editingRequest.value) {
      // Update existing PENDING request via admin status endpoint
      // We send only the updated description as adminNotes workaround,
      // but the proper approach is a dedicated PATCH endpoint on the backend.
      await dsarApi.updateDescription(editingRequest.value.id, form.value.description)
    } else {
      await dsarApi.submitRequest(form.value)
    }
    closeModal()
    await fetchRequests()
  } catch (e) {
    submitError.value = e.response?.data?.message || 'Failed to save request.'
  } finally {
    submitting.value = false
  }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

function statusLabel(s) {
  return { PENDING: 'Pending', IN_PROGRESS: 'In Progress',
           COMPLETED: 'Completed', REJECTED: 'Rejected' }[s] || s
}
function statusBadge(s) {
  return { PENDING: 'bg-warning text-dark', IN_PROGRESS: 'bg-info text-dark',
           COMPLETED: 'bg-success', REJECTED: 'bg-danger' }[s] || 'bg-secondary'
}
function typeBadge(t) {
  return { ACCESS: 'bg-primary', DELETE: 'bg-danger', CORRECT: 'bg-info text-dark' }[t] || 'bg-secondary'
}
function typeIcon(t) {
  return { ACCESS: 'bi-eye', DELETE: 'bi-trash', CORRECT: 'bi-pencil-square' }[t] || 'bi-question'
}
function formatDate(d) {
  return d ? new Date(d).toLocaleString() : '—'
}

onMounted(fetchRequests)
</script>
