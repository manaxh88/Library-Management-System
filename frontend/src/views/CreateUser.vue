<template>
  <div class="create-user-page">
    <div class="card">
      <div class="header">
        <h2>创建用户</h2>
        <p>新增一个图书馆账号</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="form"
      >
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="学号或工号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="管理员" :value="0" />
            <el-option label="教师" :value="1" />
            <el-option label="学生" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="院系" prop="deptName">
          <el-select v-model="form.deptName" placeholder="请选择院系">
            <el-option label="计算机学院" value="计算机学院" />
            <el-option label="外国语学院" value="外国语学院" />
            <el-option label="经济学院" value="经济学院" />
            <el-option label="管理学院" value="管理学院" />
            <el-option label="理学院" value="理学院" />
          </el-select>
        </el-form-item>

        <div class="actions">
          <el-button type="primary" :loading="loading" @click="onSubmit">创建</el-button>
          <el-button @click="resetForm">重置</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import axios from 'axios';

interface CreateUserForm {
  username: string;
  password: string;
  realName: string;
  role: number | null;
  deptName: string;
}

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive<CreateUserForm>({
  username: '',
  password: '',
  realName: '',
  role: null,
  deptName: ''
});

const rules: FormRules<CreateUserForm> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  deptName: [{ required: true, message: '请输入院系', trigger: 'blur' }]
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
    const response = await axios.post('/users', form);
    if (response.data?.success) {
      ElMessage.success('用户创建成功');
      resetForm();
      return;
    }
    ElMessage.error(response.data?.message || '创建失败');
  } catch (error: any) {
    const message = error?.response?.data?.message || '创建失败';
    ElMessage.error(message);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.create-user-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: var(--app-bg);
  padding: 24px;
  font-family: "Noto Serif SC", "Microsoft YaHei", serif;
}

.card {
  width: min(520px, 92vw);
  background: rgba(255, 255, 255, 0.94);
  border-radius: 18px;
  padding: 28px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
}

.header h2 {
  margin: 0;
  font-size: 22px;
  color: #0f172a;
}

.header p {
  margin: 6px 0 18px;
  color: #475569;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: flex-start;
}
</style>
