<template>
  <div class="home-container">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">校园失物招领互助平台</h1>
        <p class="hero-subtitle">互帮互助，让失物回归原主，让爱心传递校园</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="handlePublish(0)" class="action-btn">我丢了东西</el-button>
          <el-button type="success" size="large" @click="handlePublish(1)" class="action-btn">我捡到了东西</el-button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <!-- Search & Filter -->
      <el-card class="filter-card">
        <el-row justify="space-between" align="middle">
          <el-col :span="12">
            <el-radio-group v-model="activeTab" size="large" @change="fetchData">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="lost">寻物启事</el-radio-button>
              <el-radio-button label="found">失物招领</el-radio-button>
            </el-radio-group>
          </el-col>
          <el-col :span="8">
            <el-input
              v-model="searchQuery"
              placeholder="搜索物品名称或描述..."
              prefix-icon="Search"
              clearable
              @clear="fetchData"
              @keyup.enter="fetchData"
            >
              <template #append>
                <el-button @click="fetchData">搜索</el-button>
              </template>
            </el-input>
          </el-col>
        </el-row>
      </el-card>

      <!-- Items Grid -->
      <el-row :gutter="20" class="items-grid" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in items" :key="item.id">
          <el-card class="item-card" shadow="hover" :body-style="{ padding: '0px' }">
            <div class="item-image-placeholder">
               <!-- 如果有图片则显示图片，否则显示图标 -->
              <img v-if="item.imageUrl" :src="item.imageUrl" class="item-image" />
              <el-icon v-else :size="40" color="#909399"><Picture /></el-icon>
            </div>
            <div class="item-info">
              <div class="item-header">
                <el-tag :type="item.type === 0 ? 'danger' : 'success'" effect="dark" size="small">
                  {{ item.type === 0 ? '寻物' : '招领' }}
                </el-tag>
                <span class="item-date">{{ formatDate(item.date) }}</span>
              </div>
              <h3 class="item-title" :title="item.title">{{ item.title }}</h3>
              <p class="item-desc" :title="item.description">{{ item.description }}</p>
              <div class="item-footer">
                <span class="item-location">
                  <el-icon><Location /></el-icon> {{ item.location }}
                </span>
                <el-button type="primary" link @click="showContact(item)">联系发布者</el-button>
                <el-tag v-if="item.status === 1" type="success" size="small" effect="plain">已解决</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col v-if="items.length === 0" :span="24">
            <el-empty description="暂无相关信息" />
        </el-col>
      </el-row>
    </div>

    <!-- Contact Dialog -->
    <el-dialog
      v-model="contactDialogVisible"
      title="联系发布者"
      width="300px"
      center
      class="contact-dialog"
    >
      <div class="contact-info">
        <!-- 优先显示自定义联系方式 -->
        <div class="contact-item" v-if="currentContact.contactInfo">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ currentContact.contactInfo }}</span>
        </div>
        
        <!-- 如果没有自定义联系方式，显示默认注册信息 -->
        <template v-else>
          <div class="contact-item">
            <el-icon><User /></el-icon>
            <span>{{ currentContact.publisherNickname }}</span>
          </div>
          <div class="contact-item">
            <el-icon><Phone /></el-icon>
            <span>{{ currentContact.publisherPhone }}</span>
          </div>
        </template>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="contactDialogVisible = false" style="width: 100%">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Announcement Modal -->
    <el-dialog
      v-model="announcementDialogVisible"
      title="最新公告"
      width="500px"
      center
      align-center
      class="announcement-dialog"
    >
      <div class="announcement-content" v-if="announcements.length > 0">
        <div v-for="(item, index) in announcements" :key="item.id" class="modal-announcement-item">
          <div class="announcement-header">
            <el-tag size="small" type="warning" effect="dark">通知</el-tag>
            <span class="announcement-title">{{ item.title }}</span>
            <span class="announcement-date">{{ formatDate(item.createdAt) }}</span>
          </div>
          <p class="announcement-body">{{ item.content }}</p>
          <el-divider v-if="index < announcements.length - 1" />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" size="large" @click="closeAnnouncementDialog" style="width: 100%">我已知晓</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Picture, Location, User, Phone, ChatDotRound } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getPublicItems } from '../api/item'
import { getAnnouncements } from '../api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeTab = ref('all')
const searchQuery = ref('')
const items = ref([])
const announcements = ref([])
const loading = ref(false)
const contactDialogVisible = ref(false)
const currentContact = ref({})
const announcementDialogVisible = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      query: searchQuery.value
    }
    if (activeTab.value === 'lost') params.type = 0
    if (activeTab.value === 'found') params.type = 1
    
    items.value = await getPublicItems(params)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const fetchAnnouncements = async () => {
  try {
    const data = await getAnnouncements()
    announcements.value = data
    if (data.length > 0) {
      // 检查 sessionStorage，如果未关闭过，则显示
      const hasSeen = sessionStorage.getItem('hasSeenAnnouncements')
      if (!hasSeen) {
        announcementDialogVisible.value = true
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const closeAnnouncementDialog = () => {
  announcementDialogVisible.value = false
  sessionStorage.setItem('hasSeenAnnouncements', 'true')
}

const handlePublish = (type) => {
  router.push({ path: '/publish', query: { type } })
}

const showContact = (item) => {
  const user = localStorage.getItem('user')
  if (!user) {
    ElMessage.warning('请先登录后查看联系方式')
    router.push('/login')
    return
  }
  currentContact.value = item
  contactDialogVisible.value = true
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString()
}

onMounted(() => {
  fetchData()
  fetchAnnouncements()
})
</script>

<style scoped>
/* ... existing styles ... */
.announcement-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.announcement-title {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
  flex-grow: 1;
}
.announcement-date {
  font-size: 12px;
  color: #909399;
}
.announcement-body {
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
  margin: 0;
}
.modal-announcement-item {
  padding: 10px 0;
}
</style>

<style scoped>
.home-container {
  background-color: #f4f7f6;
  min-height: 100vh;
}

.hero-section {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 100px 20px;
  text-align: center;
  border-radius: 0 0 50% 50% / 20px;
  margin-bottom: 40px;
  overflow: hidden;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('https://www.transparenttextures.com/patterns/cubes.png');
  opacity: 0.1;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 3.5em;
  margin-bottom: 15px;
  font-weight: 800;
  text-shadow: 0 2px 10px rgba(0,0,0,0.2);
}

.hero-subtitle {
  font-size: 1.3em;
  opacity: 0.95;
  margin-bottom: 40px;
  font-weight: 300;
}

.action-btn {
  margin: 0 15px;
  padding: 25px 50px;
  font-size: 1.2em;
  border-radius: 50px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
  transition: transform 0.2s;
}

.action-btn:hover {
  transform: translateY(-3px);
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.announcements-bar {
  margin-bottom: 30px;
}

.announcement-item {
  margin-bottom: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.filter-card {
  margin-bottom: 30px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.items-grid {
  margin-top: 20px;
}

.item-card {
  margin-bottom: 25px;
  transition: all 0.3s ease;
  border: none;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.item-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}

.item-image-placeholder {
  height: 180px;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.item-card:hover .item-image {
  transform: scale(1.05);
}

.item-info {
  padding: 20px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.item-date {
  font-size: 12px;
  color: #909399;
}

.item-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-desc {
  font-size: 14px;
  color: #606266;
  margin: 0 0 20px;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}

.item-location {
  display: flex;
  align-items: center;
  gap: 5px;
}

.contact-info {
  padding: 10px 0;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
}

.contact-item .el-icon {
  font-size: 20px;
  color: #409EFF;
}
</style>
