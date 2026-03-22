<template>
  <div>
    <!-- Global navbar (hidden on login page) -->
    <nav v-if="auth.isAuthenticated" class="navbar navbar-dark bg-primary px-4 mb-0">
      <span class="navbar-brand fw-bold">
        <i class="bi bi-shield-lock-fill me-2"></i>DSAR Portal
      </span>
      <div class="d-flex align-items-center gap-3">
        <span class="text-white-50 small">
          <i class="bi bi-person-circle me-1"></i>{{ auth.user?.username }}
          <span class="badge bg-light text-primary ms-1">
            {{ auth.isAdmin ? 'Admin' : 'Customer' }}
          </span>
        </span>

        <template v-if="auth.isAdmin">
          <RouterLink to="/admin" class="btn btn-sm btn-outline-light">
            <i class="bi bi-list-check me-1"></i>Requests
          </RouterLink>
          <RouterLink to="/admin/audit" class="btn btn-sm btn-outline-light">
            <i class="bi bi-journal-text me-1"></i>Audit Log
          </RouterLink>
        </template>

        <button class="btn btn-sm btn-outline-light" @click="handleLogout">
          <i class="bi bi-box-arrow-right me-1"></i>Logout
        </button>
      </div>
    </nav>

    <RouterView />
  </div>
</template>

<script setup>
import { RouterView, RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth.js'

const auth   = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>
