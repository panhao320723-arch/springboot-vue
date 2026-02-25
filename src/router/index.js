import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/publish',
    name: 'publish',
    component: () => import('../views/PublishView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/users'
      },
      {
        path: 'users',
        component: () => import('../views/admin/UserManagement.vue')
      },
      {
        path: 'items',
        component: () => import('../views/admin/ItemManagement.vue')
      },
      {
        path: 'announcements',
        component: () => import('../views/admin/AnnouncementManagement.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  
  if (to.meta.requiresAuth && !user) {
    next('/login')
  } else if (to.meta.requiresAdmin && user.role !== 'ADMIN') {
    next('/')
  } else {
    next()
  }
})

export default router
