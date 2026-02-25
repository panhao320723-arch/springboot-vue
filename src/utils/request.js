import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';

const request = axios.create({
    baseURL: '/api',
    timeout: 5000,
    withCredentials: true
});

request.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.code === 200) {
            return res.data;
        } else {
            ElMessage.error(res.message || '系统错误');
            return Promise.reject(new Error(res.message || '系统错误'));
        }
    },
    error => {
        console.log(error);
        if (error.response && error.response.status === 401) {
            ElMessage.error('登录已过期，请重新登录');
            localStorage.removeItem('user');
            router.push('/login');
        } else {
            ElMessage.error(error.message || '网络错误');
        }
        return Promise.reject(error);
    }
);

export default request;
