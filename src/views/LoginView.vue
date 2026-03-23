<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center bg-light">
    <div class="card shadow-sm" style="width:100%;max-width:420px">
      <div class="card-body p-5">
        <!-- Header -->
        <div class="text-center mb-4">
          <div class="display-4 text-primary mb-2">
            <i class="bi bi-shield-lock-fill"></i>
          </div>
          <h2 class="fw-bold mb-0">DSAR Portal</h2>
          <p class="text-muted small mt-1">Data Subject Access Request System</p>
        </div>

        <!-- Error alert -->
        <div v-if="auth.error" class="alert alert-danger py-2" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ auth.error }}
        </div>

        <!-- Login form -->
        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <label class="form-label fw-semibold">Username</label>
            <div class="input-group">
              <span class="input-group-text"><i class="bi bi-person"></i></span>
              <input v-model="username" type="text" class="form-control"
                     placeholder="e.g. alice" required autocomplete="username" />
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label fw-semibold">Password</label>
            <div class="input-group">
              <span class="input-group-text"><i class="bi bi-lock"></i></span>
              <input v-model="password" :type="showPwd ? 'text' : 'password'"
                     class="form-control" placeholder="••••••••"
                     required autocomplete="current-password" />
              <button type="button" class="btn btn-outline-secondary"
                      @click="showPwd = !showPwd" tabindex="-1">
                <i :class="showPwd ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
              </button>
            </div>
          </div>

          <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold"
                  :disabled="auth.loading">
            <span v-if="auth.loading">
              <span class="spinner-border spinner-border-sm me-2" role="status"></span>
              Signing in…
            </span>
            <span v-else>
              <i class="bi bi-box-arrow-in-right me-2"></i>Sign In
            </span>
          </button>
        </form>

        <!-- Demo credentials hint -->
        <hr class="my-4" />
        <div class="bg-light rounded p-3 small">
          <p class="fw-semibold mb-1 text-muted">Demo credentials</p>
          <table class="table table-sm table-borderless mb-0">
            <thead><tr><th>Username</th><th>Password</th><th>Role</th></tr></thead>
            <tbody>
              <tr><td><code>Bhargava</code></td><td><code>myinfy124</code></td><td><span class="badge bg-danger">Admin</span></td></tr>
              <tr><td><code>Pavan</code></td><td><code>pavan@123</code></td><td><span class="badge bg-success">Customer</span></td></tr>
              <tr><td><code>Nagaraju</code></td><td><code>nag@123</code></td><td><span class="badge bg-success">Customer</span></td></tr>
              <tr><td><code>Krishna</code></td><td><code>krishna@123</code></td><td><span class="badge bg-success">Customer</span></td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const auth     = useAuthStore()
const router   = useRouter()
const username = ref('')
const password = ref('')
const showPwd  = ref(false)

async function handleLogin() {
  const ok = await auth.login(username.value, password.value)
  if (ok) {
    router.push(auth.isAdmin ? '/admin' : '/customer')
  }
}
</script>
