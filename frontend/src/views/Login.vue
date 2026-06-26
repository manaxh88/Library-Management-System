<template>
  <div class="login-page">
    <div class="bg-orb orb-one"></div>
    <div class="bg-orb orb-two"></div>

    <div class="login-card">
      <div class="brand">
        <div>
          <p class="kicker">智慧图书馆</p>
          <h1>读者服务中心</h1>
          <p class="subtitle">安全、便捷地管理借阅与账户信息</p>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="login-form"
      >
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="学号或工号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
          />
        </el-form-item>
        <div class="actions">
          <el-button type="primary" size="large" :loading="loading" @click="onSubmit">
            登录
          </el-button>
          <el-button text size="large" @click="resetForm">重置</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';

interface LoginForm {
  username: string;
  password: string;
}

const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive<LoginForm>({
  username: '',
  password: ''
});

const rules: FormRules<LoginForm> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const resetForm = () => {
  formRef.value?.resetFields();
};

const onSubmit = async () => {
  const valid = await formRef.value?.validate();
  if (!valid) {
    return;
  }

  loading.value = true;
  try {
    const response = await axios.post('/login', form);
    if (response.data?.success) {
      const token = response.data.data?.token;
      const userId = response.data.data?.userId;
      const role = response.data.data?.role;
      const realName = response.data.data?.realName;
      if (token) {
        localStorage.setItem('token', token);
      }
      if (form.username) {
        localStorage.setItem('username', form.username);
      }
      if (userId !== undefined && userId !== null) {
        localStorage.setItem('userId', String(userId));
      }
      if (role !== undefined && role !== null) {
        localStorage.setItem('role', String(role));
      }
      if (realName) {
        localStorage.setItem('realName', realName);
      }
      window.dispatchEvent(new Event('auth-changed'));
      ElMessage.success('登录成功');
      const redirectMap: Record<number, string> = {
        0: '/admin',
        1: '/',
        2: '/'
      };
      const targetPath = role !== undefined && role !== null
        ? (redirectMap[Number(role)] || '/')
        : '/';
      await router.push(targetPath);
      return;
    }

    ElMessage.error(response.data?.message || '登录失败');
  } catch (error: any) {
    const message = error?.response?.data?.message || '登录失败';
    ElMessage.error(message);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  position: relative;
  overflow: hidden;
  background: var(--app-bg);
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(0px);
  opacity: 0.7;
  animation: float 10s ease-in-out infinite;
}

.orb-one {
  width: 320px;
  height: 320px;
  background: rgba(56, 189, 248, 0.35);
  top: -60px;
  right: -40px;
}

.orb-two {
  width: 260px;
  height: 260px;
  background: rgba(253, 186, 116, 0.4);
  bottom: -80px;
  left: -50px;
  animation-delay: 1.5s;
}

.login-card {
  width: min(460px, 94vw);
  padding: 32px 30px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(148, 163, 184, 0.22);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
  backdrop-filter: blur(12px);
  animation: rise 0.6s ease-out;
  font-family: "Noto Serif SC", "Microsoft YaHei", serif;
}

.brand {
  margin-bottom: 20px;
}

.kicker {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #64748b;
  margin: 0 0 6px;
}

.brand h1 {
  font-size: 26px;
  margin: 0;
  color: #0f172a;
}

.subtitle {
  margin: 6px 0 0;
  color: #475569;
  font-size: 14px;
}

.login-form :deep(.el-form-item__label) {
  color: #334155;
  font-weight: 600;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.actions .el-button:first-child {
  width: 180px;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(14px);
  }
}

@keyframes rise {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0px);
    opacity: 1;
  }
}
</style>
