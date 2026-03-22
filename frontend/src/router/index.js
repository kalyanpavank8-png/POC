import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

import LoginView    from '../views/LoginView.vue'
import CustomerView from '../views/CustomerView.vue'
import AdminView    from '../views/AdminView.vue'
import AuditView    from '../views/AuditView.vue'

const routes = [
  { path: '/',        redirect: '/login' },
  { path: '/login',   component: LoginView, meta: { public: true } },
  {
    path: '/customer',
    component: CustomerView,
    meta: { requiresAuth: true, role: 'ROLE_CUSTOMER' }
  },
  {
    path: '/admin',
    component: AdminView,
    meta: { requiresAuth: true, role: 'ROLE_ADMIN' }
  },
  {
    path: '/admin/audit',
    component: AuditView,
    meta: { requiresAuth: true, role: 'ROLE_ADMIN' }
  },
  // Catch-all
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  auth.restoreSession()

  if (to.meta.public) {
    // Already logged in → redirect to dashboard
    if (auth.isAuthenticated) {
      return next(auth.isAdmin ? '/admin' : '/customer')
    }
    return next()
  }

  if (!auth.isAuthenticated) {
    return next('/login')
  }

  // Role check
  if (to.meta.role) {
    const hasRole = auth.user?.roles?.includes(to.meta.role)
    if (!hasRole) {
      return next(auth.isAdmin ? '/admin' : '/customer')
    }
  }

  next()
})

export default router
