<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- Left: User Profile Card -->
      <el-col :xs="24" :sm="8" :md="6">
        <el-card class="user-card" shadow="hover">
          <div class="user-header">
            <el-avatar :size="100" class="user-avatar" :src="defaultAvatar">{{ user.nickname?.charAt(0) }}</el-avatar>
            <h2 class="user-nickname">{{ user.nickname }}</h2>
            <el-tag size="small" :type="user.role === 'ADMIN' ? 'danger' : 'info'" effect="dark" class="user-role">
              {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
          <el-divider />
          <div class="user-details">
            <div class="detail-item">
              <el-icon><User /></el-icon>
              <span>用户名: {{ user.username }}</span>
            </div>
            <div class="detail-item">
              <el-icon><Iphone /></el-icon>
              <span>手机号: {{ user.phone }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right: My Items -->
      <el-col :xs="24" :sm="16" :md="18">
        <el-card class="items-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>我的发布</h3>
              <el-button type="primary" link @click="fetchMyItems">刷新列表</el-button>
            </div>
          </template>
          
          <el-tabs v-model="activeTab" class="items-tabs">
            <el-tab-pane label="全部物品" name="all">
               <div v-if="items.length === 0" class="empty-state">
                  <el-empty description="暂无发布记录" />
               </div>
               <div v-else class="item-list">
                 <div v-for="item in items" :key="item.id" class="item-row">
                    <div class="item-content">
                       <div class="item-main">
                          <el-tag :type="item.type === 0 ? 'danger' : 'success'" size="small" effect="plain" class="item-tag">
                            {{ item.type === 0 ? '寻物' : '招领' }}
                          </el-tag>
                          <span class="item-title">{{ item.title }}</span>
                          <el-tag v-if="item.status === 1" type="success" size="small" class="status-tag">已解决</el-tag>
                       </div>
                       <div class="item-meta">
                          <span class="meta-item"><el-icon><Clock /></el-icon> {{ formatDate(item.date) }}</span>
                          <span class="meta-item"><el-icon><Location /></el-icon> {{ item.location }}</span>
                       </div>
                    </div>
                    <div class="item-actions">
                        <el-button 
                          v-if="item.status === 0"
                          type="success" 
                          link
                          @click="handleMarkSolved(item)">
                          标记解决
                        </el-button>
                        <el-button type="primary" link @click="handleEdit(item)">编辑</el-button>
                        <el-button type="danger" link @click="handleDelete(item)">删除</el-button>
                    </div>
                 </div>
               </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- Edit Dialog -->
    <el-dialog v-model="editDialogVisible" title="编辑物品信息" width="500px" center>
      <el-form :model="editForm" ref="editFormRef" :rules="rules" label-width="80px">
        <el-form-item label="物品名称" prop="title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="时间" prop="date">
           <el-date-picker
            v-model="editForm.date"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="editForm.location" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="editForm.contactInfo" placeholder="请输入您的联系方式（手机/微信/QQ），留空则默认显示注册手机号" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :with-credentials="true"
          >
            <img v-if="editForm.imageUrl" :src="editForm.imageUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpdate">保存修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyItems, markAsSolved, deleteMyItem, updateItem } from '../api/item'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, Iphone, Clock, Location } from '@element-plus/icons-vue'

const user = ref({})
const items = ref([])
const loading = ref(false)
const activeTab = ref('all')
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const editForm = reactive({
  id: null,
  title: '',
  date: '',
  location: '',
  description: '',
  imageUrl: '',
  contactInfo: ''
})

const rules = {
  title: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' }]
}

const handleAvatarSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    editForm.imageUrl = response.data
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('图片必须是 JPG 或 PNG 格式!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const fetchMyItems = async () => {
  loading.value = true
  try {
    items.value = await getMyItems()
  } catch (e) {
    //
  } finally {
    loading.value = false
  }
}

const handleMarkSolved = async (row) => {
  try {
    await markAsSolved(row.id)
    ElMessage.success('已标记为解决')
    fetchMyItems()
  } catch (e) {
    //
  }
}

const handleEdit = (row) => {
  editForm.id = row.id
  editForm.title = row.title
  editForm.date = row.date ? new Date(row.date) : ''
  editForm.location = row.location
  editForm.description = row.description
  editForm.imageUrl = row.imageUrl
  editForm.contactInfo = row.contactInfo
  editDialogVisible.value = true
}

const formatDateForSubmit = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = ('0' + (d.getMonth() + 1)).slice(-2)
  const day = ('0' + d.getDate()).slice(-2)
  const hour = ('0' + d.getHours()).slice(-2)
  const minute = ('0' + d.getMinutes()).slice(-2)
  const second = ('0' + d.getSeconds()).slice(-2)
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

const handleUpdate = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const updateData = { ...editForm }
        updateData.date = formatDateForSubmit(updateData.date)
        await updateItem(editForm.id, updateData)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        fetchMyItems()
      } catch (e) {
        //
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条信息吗？此操作无法恢复。', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMyItem(row.id)
      ElMessage.success('删除成功')
      fetchMyItems()
    } catch (e) {
      //
    }
  })
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString() + ' ' + new Date(dateStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
    fetchMyItems()
  }
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 20px;
}

.user-card {
  text-align: center;
  margin-bottom: 20px;
  border-radius: 12px;
}

.user-header {
  padding: 20px 0;
}

.user-avatar {
  border: 4px solid #f0f2f5;
  margin-bottom: 15px;
}

.user-nickname {
  margin: 10px 0;
  font-size: 24px;
  color: #303133;
}

.user-role {
  margin-top: 5px;
}

.user-details {
  text-align: left;
  padding: 10px 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  color: #606266;
  font-size: 15px;
}

.items-card {
  border-radius: 12px;
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f2f5;
  transition: background-color 0.2s;
}

.item-row:hover {
  background-color: #fafafa;
}

.item-row:last-child {
  border-bottom: none;
}

.item-content {
  flex: 1;
}

.item-main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.item-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.item-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-actions {
  display: flex;
  gap: 5px;
}

.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
  border-radius: 6px;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}
</style>
