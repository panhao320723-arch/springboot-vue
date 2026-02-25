<template>
  <div>
    <div class="header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="showCreateDialog">发布公告</el-button>
    </div>

    <el-table :data="announcements" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="发布时间">
         <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="发布公告" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreate">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAnnouncements, createAnnouncement, deleteAnnouncement } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const announcements = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({
  title: '',
  content: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    announcements.value = await getAnnouncements()
  } catch (e) {
    //
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  form.title = ''
  form.content = ''
  dialogVisible.value = true
}

const handleCreate = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整')
    return
  }
  try {
    await createAnnouncement(form)
    ElMessage.success('发布成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    //
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该公告吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
