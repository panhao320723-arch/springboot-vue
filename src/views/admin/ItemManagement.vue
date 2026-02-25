<template>
  <div>
    <h2>物品管理</h2>
    <el-table :data="items" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.type === 0 ? 'danger' : 'success'">
            {{ scope.row.type === 0 ? '寻物' : '招领' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '已解决' : '未解决' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button 
            v-if="scope.row.status === 0"
            type="success" 
            size="small" 
            @click="handleUpdateStatus(scope.row, 1)">
            标记解决
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const items = ref([])
const loading = ref(false)

const fetchItems = async () => {
  loading.value = true
  try {
    items.value = await request.get('/admin/items')
  } catch (e) {
    //
  } finally {
    loading.value = false
  }
}

const handleUpdateStatus = async (row, status) => {
  await request.put(`/admin/items/${row.id}/status?status=${status}`)
  ElMessage.success('更新成功')
  fetchItems()
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该物品信息吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/admin/items/${row.id}`)
    ElMessage.success('删除成功')
    fetchItems()
  })
}

onMounted(() => {
  fetchItems()
})
</script>
