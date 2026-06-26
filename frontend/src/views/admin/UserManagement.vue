<template>
  <div class="page-wrapper">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>👤 用户管理</span>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="按账号/姓名搜索"
          clearable
          class="search-input"
          @keyup.enter="fetchUsers"
        />
        <el-button type="primary" @click="fetchUsers">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="openCreateDialog">创建用户</el-button>
      </div>

      <el-table :data="users" v-loading="tableLoading" border stripe>
        <el-table-column prop="userId" label="ID" width="70" />
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="110" />
        <el-table-column label="角色" width="100">
          <template #default="scope">
            <el-tag>{{ getRoleLabel(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="院系/部门" min-width="140" />
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="320" fixed="right">
          <template #default="scope">
            <div class="row-actions">
              <el-button
                size="small"
                :type="scope.row.status === 1 ? 'warning' : 'success'"
                @click="toggleUserStatus(scope.row)"
              >
                {{ scope.row.status === 1 ? '冻结' : '解冻' }}
              </el-button>
              <el-button size="small" type="primary" @click="openResetPasswordDialog(scope.row)">
                重置密码
              </el-button>
              <el-button size="small" @click="openRoleDialog(scope.row)">
                修改角色
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="pagination.total"
          :page-size="pagination.size"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50]"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="createDialogVisible" title="创建用户" width="520px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="createForm.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="createForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="createForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" :value="0" />
            <el-option label="教师" :value="1" />
            <el-option label="学生" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="院系" prop="deptName">
          <el-input v-model="createForm.deptName" placeholder="请输入院系或部门" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="onCreateUser">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetDialogVisible" title="重置密码" width="420px">
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-position="top">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" show-password placeholder="至少6位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="onResetPassword">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="修改角色" width="420px">
      <el-form label-position="top">
        <el-form-item label="新角色">
          <el-select v-model="roleForm.newRole" style="width: 100%">
            <el-option label="管理员" :value="0" />
            <el-option label="教师" :value="1" />
            <el-option label="学生" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="onChangeRole">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import axios from 'axios';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getUserIdFromLocalStorage } from '../../util/authUtil';

type UserItem = {
  userId: number;
  username: string;
  realName: string;
  role: number;
  deptName: string;
  status: number;
};

type PagedResponse<T> = {
  content: T[];
  totalElements: number;
};

interface CreateUserForm {
  username: string;
  password: string;
  realName: string;
  role: number | null;
  deptName: string;
}

interface ResetPasswordForm {
  newPassword: string;
}

const createFormRef = ref<FormInstance>();
const resetFormRef = ref<FormInstance>();
const tableLoading = ref(false);
const submitLoading = ref(false);

const users = ref<UserItem[]>([]);
const keyword = ref('');
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

const createDialogVisible = ref(false);
const resetDialogVisible = ref(false);
const roleDialogVisible = ref(false);
const selectedUserId = ref<number | null>(null);

const createForm = reactive<CreateUserForm>({
  username: '',
  password: '',
  realName: '',
  role: null,
  deptName: ''
});

const resetForm = reactive<ResetPasswordForm>({
  newPassword: ''
});

const roleForm = reactive({
  newRole: 2
});

const createRules: FormRules<CreateUserForm> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  deptName: [{ required: true, message: '请输入院系', trigger: 'blur' }]
};

const resetRules: FormRules<ResetPasswordForm> = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
};

const getRoleLabel = (role: number) => {
  if (role === 0) return '管理员';
  if (role === 1) return '教师';
  if (role === 2) return '学生';
  return '未知';
};

const getAdminId = () => {
  const userId = getUserIdFromLocalStorage();
  return userId ? Number(userId) : null;
};

const fetchUsers = async () => {
  tableLoading.value = true;
  try {
    const response = await axios.get('/admin/users', {
      params: {
        page: pagination.page,
        size: pagination.size,
        keyword: keyword.value || undefined
      }
    });

    if (!response.data?.success) {
      throw new Error(response.data?.message || '加载用户列表失败');
    }

    const pageData = (response.data.data || {}) as PagedResponse<UserItem>;
    users.value = pageData.content || [];
    pagination.total = Number(pageData.totalElements || 0);
  } catch (error: any) {
    ElMessage.error(error?.message || '加载用户列表失败');
  } finally {
    tableLoading.value = false;
  }
};

const resetSearch = () => {
  keyword.value = '';
  pagination.page = 1;
  fetchUsers();
};

const handlePageChange = (page: number) => {
  pagination.page = page;
  fetchUsers();
};

const handleSizeChange = (size: number) => {
  pagination.size = size;
  pagination.page = 1;
  fetchUsers();
};

const openCreateDialog = () => {
  createDialogVisible.value = true;
};

const onReset = () => {
  createFormRef.value?.resetFields();
};

const onCreateUser = async () => {
  const valid = await createFormRef.value?.validate();
  if (!valid) {
    return;
  }

  submitLoading.value = true;
  try {
    const response = await axios.post('/users', createForm);
    if (response.data?.success) {
      ElMessage.success('用户创建成功');
      onReset();
      createDialogVisible.value = false;
      fetchUsers();
      return;
    }
    ElMessage.error(response.data?.message || '创建失败');
  } catch (error: any) {
    const message = error?.response?.data?.message || '创建失败';
    ElMessage.error(message);
  } finally {
    submitLoading.value = false;
  }
};

const toggleUserStatus = async (row: UserItem) => {
  const adminId = getAdminId();
  if (!adminId) {
    ElMessage.error('缺少管理员身份信息，请重新登录');
    return;
  }

  const willFreeze = row.status === 1;
  await ElMessageBox.confirm(
    willFreeze ? `确认冻结用户 ${row.username} 吗？` : `确认解冻用户 ${row.username} 吗？`,
    '提示',
    { type: 'warning' }
  );

  try {
    const api = willFreeze ? `/admin/users/${row.userId}/freeze` : `/admin/users/${row.userId}/unfreeze`;
    const response = await axios.put(api, null, { params: { adminId } });
    if (!response.data?.success) {
      throw new Error(response.data?.message || '操作失败');
    }
    ElMessage.success(response.data?.message || '操作成功');
    fetchUsers();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || error?.response?.data?.message || '操作失败');
    }
  }
};

const openResetPasswordDialog = (row: UserItem) => {
  selectedUserId.value = row.userId;
  resetForm.newPassword = '';
  resetDialogVisible.value = true;
};

const onResetPassword = async () => {
  const adminId = getAdminId();
  if (!adminId || !selectedUserId.value) {
    ElMessage.error('缺少必要参数，请刷新后重试');
    return;
  }

  const valid = await resetFormRef.value?.validate();
  if (!valid) {
    return;
  }

  submitLoading.value = true;
  try {
    const response = await axios.put(
      `/admin/users/${selectedUserId.value}/reset-password`,
      null,
      {
        params: {
          newPassword: resetForm.newPassword,
          adminId
        }
      }
    );

    if (!response.data?.success) {
      throw new Error(response.data?.message || '重置密码失败');
    }
    ElMessage.success(response.data?.message || '密码已重置');
    resetDialogVisible.value = false;
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '重置密码失败');
  } finally {
    submitLoading.value = false;
  }
};

const openRoleDialog = (row: UserItem) => {
  selectedUserId.value = row.userId;
  roleForm.newRole = row.role;
  roleDialogVisible.value = true;
};

const onChangeRole = async () => {
  const adminId = getAdminId();
  if (!adminId || !selectedUserId.value) {
    ElMessage.error('缺少必要参数，请刷新后重试');
    return;
  }

  submitLoading.value = true;
  try {
    const response = await axios.put(
      `/admin/users/${selectedUserId.value}/role`,
      null,
      {
        params: {
          newRole: roleForm.newRole,
          adminId
        }
      }
    );

    if (!response.data?.success) {
      throw new Error(response.data?.message || '修改角色失败');
    }
    ElMessage.success(response.data?.message || '角色已修改');
    roleDialogVisible.value = false;
    fetchUsers();
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '修改角色失败');
  } finally {
    submitLoading.value = false;
  }
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.page-wrapper {
  padding: 0;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: center;
}

.search-input {
  width: 260px;
}

.actions {
  display: flex;
  gap: 12px;
}

.row-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.card-header {
  font-weight: 600;
}

@media (max-width: 768px) {
  .toolbar {
    flex-wrap: wrap;
  }

  .search-input {
    width: 100%;
  }
}
</style>
