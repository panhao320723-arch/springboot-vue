<template>
  <div>
    <h2>用户管理</h2>
    <el-table :data="users" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="role" label="角色">
        <template #default="scope">
           <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : ''">{{ scope.row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="scope">
          <el-button 
            size="small" 
            @click="handleEditRole(scope.row)"
            :disabled="isActionDisabled(scope.row)"
          >权限</el-button>
          <el-button 
            type="warning" 
            size="small" 
            @click="handleSilence(scope.row)"
            :disabled="isActionDisabled(scope.row)"
          >禁言</el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="handleDelete(scope.row)"
            :disabled="isDeleteDisabled(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="roleDialogVisible" title="修改权限" width="300px">
      <el-select v-model="currentRole" placeholder="请选择角色">
        <el-option label="普通用户" value="USER" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmRoleUpdate">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="silenceDialogVisible" title="用户禁言" width="300px">
      <el-form>
         <el-form-item label="禁言时长">
            <el-select v-model="silenceDays" placeholder="请选择时长">
              <el-option label="解除禁言" :value="0" />
              <el-option label="1天" :value="1" />
              <el-option label="3天" :value="3" />
              <el-option label="7天" :value="7" />
              <el-option label="30天" :value="30" />
            </el-select>
         </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="silenceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSilence">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { updateUserRole, silenceUser } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)
const roleDialogVisible = ref(false)
const silenceDialogVisible = ref(false)
const currentUser = ref(null)
const currentRole = ref('')
const currentAdminId = ref(null)
const currentAdminUsername = ref('')
const silenceDays = ref(1)

const fetchUsers = async () => {
  loading.value = true
  try {
    users.value = await request.get('/admin/users')
  } catch (e) {
    //
  } finally {
    loading.value = false
  }
}

const isActionDisabled = (row) => {
  // 1. 不能操作自己
  if (row.id === currentAdminId.value) return true
  
  // 2. 如果目标是超级管理员(admin)，任何人不能操作
  if (row.username === 'admin') return true
  
  // 3. 如果当前登录的不是超级管理员，则不能操作其他管理员
  if (currentAdminUsername.value !== 'admin' && row.role === 'ADMIN') return true
  
  return false
}

const isDeleteDisabled = (row) => {
  // 普通管理员完全禁止删除操作
  if (currentAdminUsername.value !== 'admin') return true;
  return isActionDisabled(row);
}

const handleEditRole = (row) => {
  currentUser.value = row
  currentRole.value = row.role
  roleDialogVisible.value = true
}

const handleSilence = (row) => {
  currentUser.value = row
  silenceDays.value = 1
  silenceDialogVisible.value = true
}

const confirmRoleUpdate = async () => {
  try {
    await updateUserRole(currentUser.value.id, currentRole.value)
    ElMessage.success('权限修改成功')
    roleDialogVisible.value = false
    fetchUsers()
  } catch (e) {
    //
  }
}

const confirmSilence = async () => {
  try {
    await silenceUser(currentUser.value.id, silenceDays.value)
    if (silenceDays.value === 0) {
       ElMessage.success('已解除禁言')
    } else {
       ElMessage.success(`已禁言 ${silenceDays.value} 天`)
    }
    silenceDialogVisible.value = false
    fetchUsers()
  } catch (e) {
    //
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/admin/users/${row.id}`)
    ElMessage.success('删除成功')
    fetchUsers()
  })
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    currentAdminId.value = user.id
    currentAdminUsername.value = user.username
  }
  fetchUsers()
})
</script>
