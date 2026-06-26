<template>
  <div class="page-wrapper">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>🔄 续借管理</span>
          <el-button text @click="loadData">刷新</el-button>
        </div>
      </template>

      <div class="summary-row">
        <el-tag type="warning">即将逾期（<3天） {{ nearDueCount }}</el-tag>
        <el-tag type="danger">预计逾期 {{ overdueCount }}</el-tag>
        <el-tag type="info">续借申请 {{ renewRequests.length }}</el-tag>
      </div>

      <el-alert
        v-if="nearDueCount > 0"
        type="warning"
        show-icon
        :closable="false"
        class="mb-12"
        :title="`有 ${nearDueCount} 本图书即将逾期，请及时申请续借`"
      />
      <el-alert
        v-if="overdueCount > 0"
        type="error"
        show-icon
        :closable="false"
        class="mb-12"
        :title="`有 ${overdueCount} 本图书预计/已逾期，请优先处理`"
      />

      <el-tabs v-model="activeTab">
        <el-tab-pane label="待处理借阅" name="current">
          <el-table :data="currentBorrowRecords" border stripe v-loading="loading">
            <el-table-column prop="bookTitle" label="书名" min-width="180" />
            <el-table-column prop="author" label="作者" min-width="120" />
            <el-table-column label="应还日期" min-width="140">
              <template #default="{ row }">{{ formatDateTime(row.dueDate) }}</template>
            </el-table-column>
            <el-table-column label="剩余天数" width="110">
              <template #default="{ row }">
                <el-tag :type="getDaysTagType(row.daysLeft)">{{ row.daysLeft }} 天</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="续借状态" width="130">
              <template #default="{ row }">
                <el-tag :type="getRenewStatusType(row.renewStatus)">{{ getRenewStatusText(row.renewStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  :loading="applyingRecordId === row.recordId"
                  :disabled="!canApplyRenew(row)"
                  @click="applyRenew(row)"
                >
                  申请续借14天
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="申请状态" name="status">
          <el-table :data="renewRequests" border stripe v-loading="loading">
            <el-table-column prop="bookTitle" label="书名" min-width="180" />
            <el-table-column prop="recordId" label="借阅记录ID" width="110" />
            <el-table-column label="申请时间" min-width="160">
              <template #default="{ row }">{{ formatDateTime(row.requestDate) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getRenewStatusType(row.status)">{{ getRenewStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="延期后应还" min-width="160">
              <template #default="{ row }">{{ row.newDueDate ? formatDateTime(row.newDueDate) : '-' }}</template>
            </el-table-column>
            <el-table-column prop="reviewRemarks" label="备注" min-width="160" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

type BorrowRecord = {
  recordId: number;
  bookTitle: string;
  author: string;
  dueDate: string;
  status: number;
  isRenew: number;
  daysLeft: number;
  renewStatus: number | null;
};

type RenewRequest = {
  renewId: number;
  recordId: number;
  userId: number;
  requestDate: string;
  status: number;
  reviewRemarks?: string;
  newDueDate?: string;
  bookTitle: string;
};

const activeTab = ref('current');
const loading = ref(false);
const applyingRecordId = ref<number | null>(null);
const currentBorrowRecords = ref<BorrowRecord[]>([]);
const renewRequests = ref<RenewRequest[]>([]);

const nearDueCount = computed(
  () => currentBorrowRecords.value.filter((item) => item.daysLeft >= 0 && item.daysLeft < 3).length
);
const overdueCount = computed(() => currentBorrowRecords.value.filter((item) => item.daysLeft < 0).length);

const getUserId = () => {
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

const calculateDaysLeft = (dueDate: string) => {
  const due = new Date(dueDate).getTime();
  const now = Date.now();
  return Math.ceil((due - now) / (1000 * 60 * 60 * 24));
};

const getRenewStatusText = (status: number | null) => {
  if (status === 0) return '待审批';
  if (status === 1) return '已批准';
  if (status === 2) return '已拒绝';
  return '未申请';
};

const getRenewStatusType = (status: number | null) => {
  if (status === 0) return 'warning';
  if (status === 1) return 'success';
  if (status === 2) return 'danger';
  return 'info';
};

const getDaysTagType = (daysLeft: number) => {
  if (daysLeft < 0) return 'danger';
  if (daysLeft < 3) return 'warning';
  return 'success';
};

const canApplyRenew = (record: BorrowRecord) => {
  if (record.isRenew === 1) return false;
  if (record.renewStatus === 0 || record.renewStatus === 1) return false;
  return true;
};

const loadData = async () => {
  const userId = getUserId();
  if (!userId) {
    ElMessage.error('未获取到用户信息，请重新登录');
    return;
  }

  loading.value = true;
  try {
    const [activeRes, allRes, renewRes] = await Promise.all([
      axios.get('/borrow/records', { params: { userId, status: '0,2' } }),
      axios.get('/borrow/records', { params: { userId } }),
      axios.get('/teacher/renew/status', { params: { userId } })
    ]);

    const activeData = activeRes.data?.data || [];
    const allData = allRes.data?.data || [];
    const renewData = renewRes.data?.data || [];

    const bookTitleMap = new Map<number, string>();
    allData.forEach((item: any) => {
      bookTitleMap.set(Number(item.borrowRecordId), item.bookTitle || '-');
    });

    const latestRenewStatusByRecord = new Map<number, number>();
    renewData.forEach((request: any) => {
      const recordId = Number(request.recordId);
      if (!latestRenewStatusByRecord.has(recordId)) {
        latestRenewStatusByRecord.set(recordId, Number(request.status));
      }
    });

    currentBorrowRecords.value = activeData.map((record: any) => ({
      recordId: Number(record.borrowRecordId),
      bookTitle: record.bookTitle || '-',
      author: record.author || '-',
      dueDate: record.dueDate,
      status: Number(record.status),
      isRenew: Number(record.isRenew || 0),
      daysLeft: calculateDaysLeft(record.dueDate),
      renewStatus: latestRenewStatusByRecord.get(Number(record.borrowRecordId)) ?? null
    }));

    renewRequests.value = renewData.map((request: any) => ({
      renewId: Number(request.renewId),
      recordId: Number(request.recordId),
      userId: Number(request.userId),
      requestDate: request.requestDate,
      status: Number(request.status),
      reviewRemarks: request.reviewRemarks || '',
      newDueDate: request.newDueDate,
      bookTitle: bookTitleMap.get(Number(request.recordId)) || '-'
    }));
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载续借数据失败');
  } finally {
    loading.value = false;
  }
};

const applyRenew = async (record: BorrowRecord) => {
  const userId = getUserId();
  if (!userId) {
    ElMessage.error('未获取到用户信息，请重新登录');
    return;
  }
  if (!canApplyRenew(record)) {
    ElMessage.warning('该记录不可再次申请续借');
    return;
  }

  applyingRecordId.value = record.recordId;
  try {
    const response = await axios.post('/teacher/renew', null, {
      params: {
        userId,
        recordId: record.recordId
      }
    });

    if (!response.data?.success) {
      throw new Error(response.data?.message || '续借申请失败');
    }

    ElMessage.success('续借申请已提交，延长14天（待审批）');
    await loadData();
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '续借申请失败');
  } finally {
    applyingRecordId.value = null;
  }
};

onMounted(() => {
  loadData();
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
}

.summary-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.mb-12 {
  margin-bottom: 12px;
}
</style>
