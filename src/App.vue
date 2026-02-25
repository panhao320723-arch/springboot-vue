<template>
  <div class="common-layout">
    <el-container>
      <el-header class="app-header">
        <div class="header-inner">
          <div class="logo">
            <el-icon :size="24" class="logo-icon"><School /></el-icon>
            <span>校园失物招领</span>
          </div>
          <div class="header-right">
            <el-menu mode="horizontal" :ellipsis="false" router :default-active="$route.path" class="nav-menu">
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/publish">发布信息</el-menu-item>
            </el-menu>
            
            <div class="user-actions">
              <template v-if="user">
                <el-dropdown @command="handleCommand">
                  <span class="el-dropdown-link">
                    {{ user.nickname || user.username }}
                    <el-icon class="el-icon--right"><arrow-down /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                      <el-dropdown-item v-if="user.role === 'ADMIN'" command="admin">后台管理</el-dropdown-item>
                      <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
              <template v-else>
                <el-button type="primary" link @click="$router.push('/login')">登录 / 注册</el-button>
              </template>
            </div>
          </div>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
      <el-footer class="app-footer">
        <p>&copy; 2026 校园失物招领互助平台 | 毕业设计</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { School, ArrowDown } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { logout } from './api/auth'

const router = useRouter()
const route = useRoute()
const user = ref(null)

const checkUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
  } else {
    user.value = null
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    await logout()
    localStorage.removeItem('user')
    user.value = null
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  }
}

watch(route, () => {
  checkUser()
})

onMounted(() => {
  checkUser()
})
</script>

<style>
body {
  margin: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: #f0f2f5;
}

.app-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  gap: 10px;
  cursor: pointer;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-menu {
  border-bottom: none !important;
  background: transparent !important;
  flex-grow: 0;
}

.user-actions {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  display: flex;
  align-items: center;
}

.app-main {
  padding: 0 !important;
  min-height: calc(100vh - 120px);
}

.app-footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
  padding: 20px 0;
  background-color: #fff;
  margin-top: 40px;
}
</style>
