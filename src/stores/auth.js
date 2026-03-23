import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/index.js'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)       // { username, roles: [] }
  const token = ref(null)      // base64(username:password)
  const loading = ref(false)
  const error = ref(null)

  const isAuthenticated = computed(() => !!user.value)
  const isAdmin = computed(() =>
    user.value?.roles?.includes('ROLE_ADMIN') ?? false
  )
  const isCustomer = computed(() =>
    user.value?.roles?.includes('ROLE_CUSTOMER') ?? false
  )

  async function login(username, password) {
    loading.value = true
    error.value = null
    try {
      const encoded = btoa(`${username}:${password}`)
      // Test credentials against /api/auth/me
      const { data } = await api.get('/auth/me', {
        headers: { Authorization: `Basic ${encoded}` }
      })
      token.value = encoded
      user.value = data
      // Persist for page refresh
      sessionStorage.setItem('dsar_token', encoded)
      sessionStorage.setItem('dsar_user', JSON.stringify(data))
      return true
    } catch (err) {
      error.value = err.response?.status === 401
        ? 'Invalid username or password.'
        : 'Login failed. Please try again.'
      return false
    } finally {
      loading.value = false
    }
  }

  function logout() {
    user.value = null
    token.value = null
    sessionStorage.removeItem('dsar_token')
    sessionStorage.removeItem('dsar_user')
  }

  function restoreSession() {
    const savedToken = sessionStorage.getItem('dsar_token')
    const savedUser  = sessionStorage.getItem('dsar_user')
    if (savedToken && savedUser) {
      token.value = savedToken
      user.value  = JSON.parse(savedUser)
    }
  }

  return { user, token, loading, error, isAuthenticated, isAdmin, isCustomer,
           login, logout, restoreSession }
})
