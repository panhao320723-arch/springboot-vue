<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>{{ isRegister ? '注册账号' : '欢迎登录' }}</h2>
        </div>
      </template>
      
      <el-form :model="form" ref="formRef" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入学号/用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        
        <template v-if="isRegister">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="验证码" prop="captcha">
            <div class="captcha-container">
               <el-input v-model="form.captcha" placeholder="请输入4位验证码" style="width: 150px; margin-right: 10px;" />
               <img :src="captchaUrl" @click="refreshCaptcha" class="captcha-img" alt="验证码" title="点击刷新" />
            </div>
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" class="submit-btn" @click="handleSubmit" :loading="loading">
            {{ isRegister ? '立即注册' : '登录' }}
          </el-button>
        </el-form-item>
        
        <div class="form-footer">
          <el-link type="primary" @click="toggleMode">
            {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isRegister = ref(false)
const loading = ref(false)
const formRef = ref(null)
const captchaUrl = ref('/api/auth/captcha?t=' + new Date().getTime())

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  captcha: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCaptcha = () => {
  captchaUrl.value = '/api/auth/captcha?t=' + new Date().getTime()
}

const toggleMode = () => {
  isRegister.value = !isRegister.value
  formRef.value.resetFields()
  if (isRegister.value) {
    refreshCaptcha()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (isRegister.value) {
          await request.post('/auth/register', form)
          ElMessage.success('注册成功，请登录')
          isRegister.value = false
        } else {
          const user = await request.post('/auth/login', {
            username: form.username,
            password: form.password
          })
          ElMessage.success('登录成功')
          localStorage.setItem('user', JSON.stringify(user))
          if (user.role === 'ADMIN') {
            router.push('/admin')
          } else {
            router.push('/')
          }
        }
      } catch (e) {
        // Error handled in interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding-top: 50px;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
}

.submit-btn {
  width: 100%;
}

.form-footer {
  text-align: center;
  margin-top: 10px;
}

.captcha-container {
  display: flex;
  align-items: center;
}

.captcha-img {
  height: 32px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
</style>
