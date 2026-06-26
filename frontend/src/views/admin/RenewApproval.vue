<template>
  <div class="page-wrapper">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>🔎 续借审批</span>
          <div class="header-actions">
            <el-select v-model="statusFilter" class="status-select" @change="loadRequests">
              <el-option label="全部" value="all" />
              <el-option label="待审批" value="0" />
              <el-option label="已批准" value="1" />
              <el-option label="已拒绝" value="2" />
            </el-select>
            <el-button @click="loadRequests">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="requests" border stripe v-loading="loading">
        <el-table-column prop="renewId" label="申请ID" width="90" />
        <el-table-column prop="bookTitle" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" min-width="120" />
        <el-table-column label="申请人" min-width="130">
          <template #default="{ row }">
            {{ row.realName || row.username }}
          </template>
        </el-table-column>
        <el-table-column label="原应还日期" min-width="160">
          <template #default="{ row }">{{ formatDateTime(row.dueDate) }}</template>
        </el-table-column>
        <el-table-column label="申请时间" min-width="160">
          <template #default="{ row }">{{ formatDateTime(row.requestDate) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="延期后应还" min-width="160">
          <template #default="{ row }">{{ formatDateTime(row.newDueDate) }}</template>
        </el-table-column>
        <el-table-column prop="reviewRemarks" label="备注" min-width="140" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              type="success"
              :disabled="row.status !== 0"
              :loading="processingId === row.renewId && processingAction === 'approve'"
              @click="approve(row.renewId)"
            >
              批准
            </el-button>
            <el-button
              text
              type="danger"
              :disabled="row.status !== 0"
              :loading="processingId === row.renewId && processingAction === 'reject'"
              @click="openRejectDialog(row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="rejectDialogVisible" title="拒绝续借申请" width="420px">
        <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝原因（可选）" />
        <template #footer>
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" :loading="processingAction === 'reject'" @click="confirmReject">确认拒绝</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

type RenewRequest = {
  renewId: number;
  recordId: number;
  userId: number;
  username: string;
  realName: string;
  bookTitle: string;
  author: string;
  dueDate?: string;
  requestDate?: string;
  status: number;
  newDueDate?: string;
  reviewRemarks?: string;
};

const loading = ref(false);
const requests = ref<RenewRequest[]>([]);
const statusFilter = ref<'all' | '0' | '1' | '2'>('0');
const processingId = ref<number | null>(null);
const processingAction = ref<'approve' | 'reject' | ''>('');

const rejectDialogVisible = ref(false);
const rejectReason = ref('');
const currentRejectId = ref<number | null>(null);

const getAdminId = () => {
  const raw = localStorage.getItem('userId');
  const parsed = Number(raw);
  if (Number.isNaN(parsed) || parsed <= 0) {
    return null;
  }
  return parsed;
};

const formatDateTime = (value?: string) => {
  if (!value) return '-';
  return new Date(value).toLocaleString('zh-CN', { hour12: false });
};

const statusText = (status: number) => {
  if (status === 0) return '待审批';
  if (status === 1) return '已批准';
  if (status === 2) return '已拒绝';
  return '未知';
};

const statusTagType = (status: number) => {
  if (status === 0) return 'warning';
  if (status === 1) return 'success';
  if (status === 2) return 'danger';
  return 'info';
};

const loadRequests = async () => {
  loading.value = true;
  try {
    const params: Record<string, number> = {};
    if (statusFilter.value !== 'all') {
      params.status = Number(statusFilter.value);
    }

    const response = await axios.get('/admin/renew/requests', { params });
    if (!response.data?.success) {
      throw new Error(response.data?.message || '加载续借申请失败');
    }

    requests.value = response.data?.data || [];
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '加载续借申请失败');
  } finally {
    loading.value = false;
  }
};

const approve = async (renewId: number) => {
  const adminId = getAdminId();
  if (!adminId) {
    ElMessage.error('未获取到管理员信息，请重新登录');
    return;
  }

  processingId.value = renewId;
  processingAction.value = 'approve';
  try {
    const response = await axios.post(`/admin/renew/${renewId}/approve`, null, { params: { adminId } });
    if (!response.data?.success) {
      throw new Error(response.data?.message || '审批失败');
    }
    ElMessage.success('已批准该续借申请');
    await loadRequests();
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '审批失败');
  } finally {
    processingId.value = null;
    processingAction.value = '';
  }
};

const openRejectDialog = (row: RenewRequest) => {
  currentRejectId.value = row.renewId;
  rejectReason.value = '';
  rejectDialogVisible.value = true;
};

const confirmReject = async () => {
  const renewId = currentRejectId.value;
  if (!renewId) {
    return;
  }
  const adminId = getAdminId();
  if (!adminId) {
    ElMessage.error('未获取到管理员信息，请重新登录');
    return;
  }

  processingId.value = renewId;
  processingAction.value = 'reject';
  try {
    const response = await axios.post(`/admin/renew/${renewId}/reject`, null, {
      params: { adminId, reason: rejectReason.value || undefined }
    });
    if (!response.data?.success) {
      throw new Error(response.data?.message || '拒绝失败');
    }
    ElMessage.success('已拒绝该续借申请');
    rejectDialogVisible.value = false;
    await loadRequests();
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '拒绝失败');
  } finally {
    processingId.value = null;
    processingAction.value = '';
  }
};

onMounted(() => {
  loadRequests();
});
</script>

<style scoped>
.page-wrapper {
  padding: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.status-select {
  width: 140px;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .status-select {
    width: 100%;
  }
}
</style>
