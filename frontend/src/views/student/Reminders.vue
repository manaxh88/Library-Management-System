<template>
  <div class="page-wrapper">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>🔔 借阅提醒中心</span>
          <div class="header-actions">
            <el-button text @click="loadReminders" :loading="loading">刷新</el-button>
            <el-button type="primary" plain @click="markAllRead" :disabled="unreadCount === 0">全部标记已读</el-button>
          </div>
        </div>
      </template>

      <div class="summary-row">
        <el-tag type="danger">逾期提醒：{{ overdueCount }}</el-tag>
        <el-tag type="warning">即将逾期：{{ dueSoonCount }}</el-tag>
        <el-tag type="info">未读提醒：{{ unreadCount }}</el-tag>
      </div>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="未读" name="unread" />
        <el-tab-pane label="已逾期" name="overdue" />
        <el-tab-pane label="即将逾期" name="dueSoon" />
      </el-tabs>

      <el-table :data="filteredReminders" stripe v-loading="loading">
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.reminderType === 1 ? 'danger' : 'warning'">
              {{ row.reminderType === 1 ? '已逾期' : '即将逾期' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="bookTitle" label="图书" min-width="160" />

        <el-table-column label="提醒内容" min-width="260">
          <template #default="{ row }">
            <div class="message-cell">{{ row.message }}</div>
            <div class="sub-line">应还日期：{{ formatDate(row.dueDate) }}</div>
          </template>
        </el-table-column>

        <el-table-column label="通知方式" width="160">
          <template #default="{ row }">
            <div class="channel-tags">
              <el-tag size="small" v-if="row.notificationChannels.includes('IN_APP')">
                站内信{{ row.inAppNotified ? '已发送' : '未发送' }}
              </el-tag>
              <el-tag size="small" type="success" v-if="row.notificationChannels.includes('EMAIL')">
                邮件{{ row.emailNotified ? '已发送' : '未发送' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'primary'">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="markRead(row)" :disabled="row.isRead">
              标记已读
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredReminders.length === 0 && !loading" class="empty-state">
        当前筛选条件下暂无提醒
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { getUserIdFromLocalStorage, getTokenFromLocalStorage, getUserInfoFromToken } from '../../util/authUtil';

interface ReminderItem {
  reminderId: number;
  recordId: number;
  bookTitle: string;
  reminderType: number;
  message: string;
  dueDate: string;
  overdayCount?: number;
  fineAmount?: number;
  isRead: boolean;
  notificationChannels: string[];
  inAppNotified: boolean;
  emailNotified: boolean;
  createTime: string;
}

const normalizeReminder = (item: any): ReminderItem => ({
  reminderId: Number(item?.reminderId || 0),
  recordId: Number(item?.recordId || 0),
  bookTitle: item?.bookTitle || '-',
  reminderType: Number(item?.reminderType ?? 0),
  message: item?.message || '暂无提醒内容',
  dueDate: item?.dueDate || '',
  overdayCount: item?.overdayCount,
  fineAmount: item?.fineAmount,
  isRead: Boolean(item?.isRead),
  notificationChannels: Array.isArray(item?.notificationChannels) ? item.notificationChannels : [],
  inAppNotified: Boolean(item?.inAppNotified),
  emailNotified: Boolean(item?.emailNotified),
  createTime: item?.createTime || ''
});

const reminders = ref<ReminderItem[]>([]);
const loading = ref(false);
const activeTab = ref<'all' | 'unread' | 'overdue' | 'dueSoon'>('all');

const getUserId = (): number | null => {
  const storedUserId = getUserIdFromLocalStorage();
  if (storedUserId) {
    const parsed = Number(storedUserId);
    if (!Number.isNaN(parsed) && parsed > 0) {
      return parsed;
    }
  }

  const token = getTokenFromLocalStorage();
  if (token) {
    const info = getUserInfoFromToken(token);
    const tokenUserId = Number(info?.userId);
    if (!Number.isNaN(tokenUserId) && tokenUserId > 0) {
      return tokenUserId;
    }
  }

  return null;
};

const unreadCount = computed(() => reminders.value.filter(item => !item.isRead).length);
const overdueCount = computed(() => reminders.value.filter(item => item.reminderType === 1).length);
const dueSoonCount = computed(() => reminders.value.filter(item => item.reminderType === 0).length);

const filteredReminders = computed(() => {
  if (activeTab.value === 'unread') {
    return reminders.value.filter(item => !item.isRead);
  }
  if (activeTab.value === 'overdue') {
    return reminders.value.filter(item => item.reminderType === 1);
  }
  if (activeTab.value === 'dueSoon') {
    return reminders.value.filter(item => item.reminderType === 0);
  }
  return reminders.value;
});

const formatDate = (dateStr: string) => {
  if (!dateStr) {
    return '-';
  }
  return new Date(dateStr).toLocaleString('zh-CN');
};

const loadReminders = async () => {
  const userId = getUserId();
  if (!userId) {
    ElMessage.error('未获取到用户信息');
    return;
  }

  try {
    loading.value = true;
    const res = await axios.get('/student/reminders', { params: { userId } });
    if (!res.data?.success) {
      throw new Error(res.data?.message || '加载提醒失败');
    }
    const list = Array.isArray(res.data?.data) ? res.data.data : [];
    reminders.value = list.map(normalizeReminder);
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载提醒失败');
  } finally {
    loading.value = false;
  }
};

const markRead = async (row: ReminderItem) => {
  const userId = getUserId();
  if (!userId) {
    ElMessage.error('未获取到用户信息');
    return;
  }

  try {
    await axios.put(`/student/reminders/${row.reminderId}/read`, null, { params: { userId } });
    row.isRead = true;
    ElMessage.success('已标记为已读');
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '标记已读失败');
  }
};

const markAllRead = async () => {
  const userId = getUserId();
  if (!userId) {
    ElMessage.error('未获取到用户信息');
    return;
  }

  try {
    await axios.put('/student/reminders/read-all', null, { params: { userId } });
    reminders.value = reminders.value.map(item => ({ ...item, isRead: true }));
    ElMessage.success('已全部标记为已读');
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '全部标记已读失败');
  }
};

onMounted(() => {
  loadReminders();
});
</script>

<style scoped>
.page-wrapper {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.summary-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.message-cell {
  color: #303133;
}

.sub-line {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.channel-tags {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.empty-state {
  text-align: center;
  color: #909399;
  padding: 24px 0 8px;
}
</style>
