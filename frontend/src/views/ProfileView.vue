<template>
  <div class="profile-page">
    <section class="profile-card">
      <div class="header">
        <div>
          <p class="kicker">用户中心</p>
          <h2>个人信息</h2>
          <p class="subtitle">查看你的账号信息与借阅统计。</p>
        </div>
        <div class="header-actions">
          <div class="avatar">{{ initials }}</div>
          <button
            class="change-password-btn"
            type="button"
            @click="onShowChangePasswordDialog"
          >
            修改密码
          </button>
          <button class="logout" type="button" @click="onLogout">退出登录</button>
        </div>
      </div>

      <el-row :gutter="16" class="info-grid">
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <p>账号</p>
            <h4>{{ profile.username }}</h4>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <p>姓名</p>
            <h4>{{ profile.realName }}</h4>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <p>角色</p>
            <h4>{{ roleLabel }}</h4>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <p>院系</p>
            <h4>{{ profile.deptName }}</h4>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <p>借阅上限</p>
            <h4>{{ profile.maxLimit }}</h4>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <p>状态</p>
            <h4>{{ statusLabel }}</h4>
          </div>
        </el-col>
      </el-row>
    </section>

    <section class="stats">
      <div class="stat">
        <p>当前借阅</p>
        <h3>{{ profile.currentBorrowCount }}</h3>
      </div>
      <div class="stat">
        <p>历史借阅</p>
        <h3>{{ profile.totalBorrowCount }}</h3>
      </div>
      <div class="stat">
        <p>剩余可借</p>
        <h3>{{ remainingLimit }}</h3>
      </div>
    </section>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="changePasswordDialogVisible"
      title="修改密码"
      width="400px"
      @close="resetChangePasswordForm"
    >
      <el-form
        ref="changePasswordFormRef"
        :model="changePasswordForm"
        :rules="changePasswordRules"
        label-width="80px"
        @keyup.enter="onConfirmChangePassword"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="changePasswordForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="changePasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="changePasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="changePasswordDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="onConfirmChangePassword"
            :loading="changePasswordLoading"
          >
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import axios from 'axios';
import { ElMessage, FormInstance } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();

interface Profile {
  userId: number | null;
  username: string;
  realName: string;
  role: number | null;
  deptName: string;
  maxLimit: number;
  status: number | null;
  currentBorrowCount: number;
  totalBorrowCount: number;
}

interface ChangePasswordForm {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

const profile = reactive<Profile>({
  userId: null,
  username: '-',
  realName: '-',
  role: null,
  deptName: '-',
  maxLimit: 0,
  status: null,
  currentBorrowCount: 0,
  totalBorrowCount: 0
});

const changePasswordDialogVisible = ref(false);
const changePasswordLoading = ref(false);
const changePasswordFormRef = ref<FormInstance>();
const changePasswordForm = reactive<ChangePasswordForm>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 验证密码策略
const validateNewPassword = (_rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('新密码不能为空'));
  } else if (value.length < 6) {
    callback(new Error('新密码至少需要6个字符'));
  } else {
    callback();
  }
};

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('确认密码不能为空'));
  } else if (value !== changePasswordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const changePasswordRules = {
  oldPassword: [{ required: true, message: '旧密码不能为空', trigger: 'blur' }],
  newPassword: [{ validator: validateNewPassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
};

const roleLabel = computed(() => {
  if (profile.role === 0) return '管理员';
  if (profile.role === 1) return '教师';
  if (profile.role === 2) return '学生';
  return '未知';
});

const statusLabel = computed(() => (profile.status === 1 ? '正常' : '冻结'));

const remainingLimit = computed(() => {
  const remaining = profile.maxLimit - profile.currentBorrowCount;
  return remaining < 0 ? 0 : remaining;
});

const initials = computed(() => profile.realName?.slice(0, 1) || 'U');

const getUsernameFromToken = () => {
  const token = localStorage.getItem('token');
  if (!token) {
    return null;
  }

  try {
    const payload = token.split('.')[1];
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=');
    const json = atob(padded);
    const data = JSON.parse(json);
    return data?.sub ? String(data.sub) : null;
  } catch {
    return null;
  }
};

const loadProfile = async () => {
  const storedUsername = localStorage.getItem('username') || getUsernameFromToken();
  if (!storedUsername) {
    ElMessage.error('未找到用户信息，请重新登录');
    return;
  }

  localStorage.setItem('username', storedUsername);

  try {
    const response = await axios.get(`/users/username/${encodeURIComponent(storedUsername)}`);
    if (response.data?.success) {
      const data = response.data.data || {};
      profile.userId = Number(data.userId ?? profile.userId);
      profile.username = data.username ?? '-';
      profile.realName = data.realName ?? '-';
      profile.role = data.role ?? null;
      profile.deptName = data.deptName ?? '-';
      profile.maxLimit = Number(data.maxLimit ?? 0);
      profile.status = data.status ?? null;
      profile.currentBorrowCount = Number(data.currentBorrowCount ?? 0);
      profile.totalBorrowCount = Number(data.totalBorrowCount ?? 0);
      return;
    }
    ElMessage.error(response.data?.message || '加载用户信息失败');
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载用户信息失败');
  }
};

const onShowChangePasswordDialog = () => {
  changePasswordDialogVisible.value = true;
};

const resetChangePasswordForm = () => {
  changePasswordForm.oldPassword = '';
  changePasswordForm.newPassword = '';
  changePasswordForm.confirmPassword = '';
  changePasswordFormRef.value?.clearValidate();
};

const onConfirmChangePassword = async () => {
  if (!changePasswordFormRef.value) {
    return;
  }

  try {
    await changePasswordFormRef.value.validate();
  } catch {
    return;
  }

  changePasswordLoading.value = true;
  try {
    const response = await axios.put('/users/change-password', {
      oldPassword: changePasswordForm.oldPassword,
      newPassword: changePasswordForm.newPassword,
      confirmPassword: changePasswordForm.confirmPassword
    });

    if (response.data?.success) {
      ElMessage.success('密码修改成功，请重新登录');
      changePasswordDialogVisible.value = false;
      resetChangePasswordForm();
      // 强制重新登录
      setTimeout(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        localStorage.removeItem('role');
        localStorage.removeItem('realName');
        router.push('/login');
      }, 1500);
    } else {
      ElMessage.error(response.data?.message || '密码修改失败');
    }
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '密码修改失败');
  } finally {
    changePasswordLoading.value = false;
  }
};

const onLogout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
  localStorage.removeItem('realName');
  router.push('/login');
};

onMounted(loadProfile);
</script>

<style scoped>
.profile-page {
  min-height: auto;
  padding: 0;
  font-family: "Noto Serif SC", "Microsoft YaHei", serif;
  color: #0f172a;
}

.profile-card {
  width: 100%;
  margin: 0 0 20px;
  border-radius: 18px;
  padding: 28px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.kicker {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #64748b;
  margin: 0 0 6px;
}

.subtitle {
  margin: 6px 0 0;
  color: #64748b;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  background: #0f172a;
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 26px;
  font-weight: 700;
}

.logout {
  border: none;
  background: linear-gradient(135deg, #0f172a, #1e293b);
  color: #fff;
  font-weight: 700;
  width: 64px;
  height: 64px;
  border-radius: 16px;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.2);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.logout:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 24px rgba(15, 23, 42, 0.24);
  opacity: 0.95;
}

.change-password-btn {
  border: none;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  font-weight: 700;
  width: 100px;
  height: 64px;
  border-radius: 16px;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(59, 130, 246, 0.2);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
  font-size: 14px;
}

.change-password-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 24px rgba(59, 130, 246, 0.3);
  opacity: 0.95;
}

.info-grid {
  margin-top: 8px;
}

.info-grid :deep(.el-col) {
  margin-bottom: 16px;
}

.info-item {
  background: #f8fafc;
  border-radius: 16px;
  padding: 16px;
}

.info-item p {
  margin: 0 0 6px;
  color: #64748b;
  font-size: 13px;
}

.info-item h4 {
  margin: 0;
  font-size: 16px;
}

.stats {
  width: 100%;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.stat {
  background: #0f172a;
  color: #fff;
  padding: 18px;
  border-radius: 20px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.16);
  transition: all 0.3s ease;
  position: relative;
}

.stat p {
  margin: 0 0 8px;
  color: rgba(255, 255, 255, 0.7);
}

.stat h3 {
  margin: 0;
  font-size: 26px;
}

@media (max-width: 768px) {
  .profile-card {
    padding: 20px;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #e5e7eb;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-form-item__label) {
  color: #0f172a;
  font-weight: 500;
}

:deep(.el-input__inner) {
  border-radius: 8px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background: #f5f7fa;
}

:deep(.el-table__body) {
  background: #fff;
}

:deep(.el-table__row) {
  transition: background-color 0.3s ease;
}

:deep(.el-table__row:hover > td) {
  background-color: #f5f7fa;
}

:deep(.el-pagination) {
  margin-top: 16px;
  text-align: right;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #64748b;
  font-size: 14px;
}

:deep(.dialog-footer) {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
