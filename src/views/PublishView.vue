<template>
  <div class="publish-container">
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <h2>{{ form.type === 0 ? '发布寻物启事' : '发布失物招领' }}</h2>
        </div>
      </template>
      
      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px">
        <el-form-item label="信息类型">
          <el-radio-group v-model="form.type">
            <el-radio-button :label="0">我丢了东西</el-radio-button>
            <el-radio-button :label="1">我捡到东西</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="物品名称" prop="title">
          <el-input v-model="form.title" placeholder="例如：黑色联想笔记本" />
        </el-form-item>

        <el-form-item label="丢失/拾取时间" prop="date">
           <el-date-picker
            v-model="form.date"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入地点" />
        </el-form-item>
        
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="form.contactInfo" placeholder="请输入您的联系方式（手机/微信/QQ），留空则默认显示注册手机号" />
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
            <img v-if="form.imageUrl" :src="form.imageUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="el-upload__tip">只能上传 jpg/png 文件，且不超过 2MB</div>
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述物品特征、具体情况等..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">立即发布</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { publishItem } from '../api/item'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  type: 0,
  title: '',
  date: '',
  location: '',
  contactInfo: '',
  imageUrl: '',
  description: ''
})

const rules = {
  title: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' }]
}

// 检查禁言状态
const checkSilenceStatus = async () => {
  try {
    const user = await request.get('/auth/current')
    if (user && user.silenceUntil) {
      const silenceTime = new Date(user.silenceUntil).getTime()
      if (silenceTime > Date.now()) {
        ElMessageBox.alert(`您已被禁言，解除时间：${new Date(user.silenceUntil).toLocaleString()}`, '禁止发布', {
          confirmButtonText: '确定',
          callback: () => {
            router.push('/')
          }
        })
      }
    }
  } catch (e) {
    //
  }
}

const handleAvatarSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    form.imageUrl = response.data
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

const formatDate = (date) => {
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

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const formData = { ...form }
        formData.date = formatDate(formData.date)
        await publishItem(formData)
        ElMessage.success('发布成功')
        router.push('/')
      } catch (e) {
        // handled
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  checkSilenceStatus()
  if (route.query.type) {
    form.type = parseInt(route.query.type)
  }
})
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}

.card-header {
  text-align: center;
}

.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
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
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>
