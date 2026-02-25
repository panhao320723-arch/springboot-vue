<template>
  <el-container class="admin-container">
    <el-aside width="200px">
      <el-menu
        router
        :default-active="$route.path"
        class="admin-menu"
      >
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/items">
          <el-icon><Goods /></el-icon>
          <span>物品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/announcements">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        <el-menu-item @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { User, Goods, SwitchButton, Bell } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

const handleLogout = async () => {
  await request.post('/auth/logout')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.admin-container {
  height: calc(100vh - 60px);
}
.admin-menu {
  height: 100%;
}
</style>
