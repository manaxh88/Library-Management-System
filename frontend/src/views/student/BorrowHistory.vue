<template>
  <div class="page-wrapper">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>📖 当前借阅</span>
          <el-button text @click="refreshList">刷新</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="借阅中" name="borrowing">
          <el-table :data="borrowingList" stripe v-loading="loading">
            <el-table-column prop="bookTitle" label="书名" min-width="150" />
            <el-table-column prop="author" label="作者" min-width="100" />
            <el-table-column prop="borrowDateStr" label="借阅日期" width="120" />
            <el-table-column prop="dueDateStr" label="应还日期" width="120">
              <template #default="{ row }">
                <span :class="getDateClass(row.dueDate)">{{ row.dueDateStr }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="daysLeft" label="剩余天数" width="100">
              <template #default="{ row }">
                <el-tag :type="getDaysLeftType(row.daysLeft)">{{ row.daysLeft }} 天</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="borrowingList.length === 0" class="empty-state">
            暂无借阅中的图书
          </div>
        </el-tab-pane>

        <el-tab-pane label="已归还" name="returned">
          <el-table :data="returnedList" stripe v-loading="loading">
            <el-table-column prop="bookTitle" label="书名" min-width="150" />
            <el-table-column prop="author" label="作者" min-width="100" />
            <el-table-column prop="borrowDateStr" label="借阅日期" width="120" />
            <el-table-column prop="returnDateStr" label="归还日期" width="120" />
            <el-table-column prop="borrowedDays" label="借阅天数" width="100" />
          </el-table>
          <div v-if="returnedList.length === 0" class="empty-state">
            暂无已归还的图书
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { getUserIdFromLocalStorage, getTokenFromLocalStorage, getUserInfoFromToken } from '../../util/authUtil';

interface BorrowRecord {
  recordId: number;
  userId: number;
  bookId: number;
  bookTitle: string;
  author: string;
  borrowDate: string;
  borrowDateStr: string;
  dueDate: string;
  dueDateStr: string;
  returnDate?: string;
  returnDateStr?: string;
  status: number;
  isRenew: number;
  daysLeft?: number;
  borrowedDays?: number;
}

const activeTab = ref('borrowing');
const loading = ref(false);
const borrowingList = ref<BorrowRecord[]>([]);
const returnedList = ref<BorrowRecord[]>([]);

// 获取用户ID（兼容当前与历史本地存储格式）
const getUserId = () => {
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

  const legacyUser = localStorage.getItem('user');
  if (legacyUser) {
    try {
      const user = JSON.parse(legacyUser);
      const legacyUserId = Number(user?.userId);
      if (!Number.isNaN(legacyUserId) && legacyUserId > 0) {
        return legacyUserId;
      }
    } catch {
      return null;
    }
  }
  return null;
};

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN');
};

// 计算剩余天数
const calculateDaysLeft = (dueDate: string) => {
  const due = new Date(dueDate);
  const now = new Date();
  const diffTime = due.getTime() - now.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays;
};

// 计算借阅天数
const calculateBorrowedDays = (borrowDate: string, returnDate: string) => {
  const borrow = new Date(borrowDate);
  const ret = new Date(returnDate);
  const diffTime = ret.getTime() - borrow.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays;
};

// 获取日期样式
const getDateClass = (dateStr: string) => {
  const daysLeft = calculateDaysLeft(dateStr);
  if (daysLeft < 0) return 'overdue';
  if (daysLeft < 7) return 'warning';
  return '';
};

// 获取剩余天数标签类型
const getDaysLeftType = (days: number) => {
  if (days < 0) return 'danger';
  if (days < 7) return 'warning';
  return 'success';
};

// 加载借阅记录
const loadBorrowRecords = async () => {
  try {
    loading.value = true;
    const userId = getUserId();
    if (!userId) {
      ElMessage.error('未获取到用户信息');
      return;
    }

    // 获取借阅中的记录 (status=0为借阅中，status=2为逾期)
    const borrowingRes = await axios.get('/borrow/records', {
      params: { userId, status: '0,2' }
    });

    // 获取已归还的记录 (status=1为已归还)
    const returnedRes = await axios.get('/borrow/records', {
      params: { userId, status: '1' }
    });

    // 处理借阅中的记录
    if (borrowingRes.data.data) {
      borrowingList.value = borrowingRes.data.data.map((record: any) => ({
        recordId: record.borrowRecordId,
        bookTitle: record.bookTitle,
        author: record.author || '',
        borrowDate: record.borrowDate,
        borrowDateStr: formatDate(record.borrowDate),
        dueDate: record.dueDate,
        dueDateStr: formatDate(record.dueDate),
        status: record.status,
        daysLeft: calculateDaysLeft(record.dueDate)
      }));
    }

    // 处理已归还的记录
    if (returnedRes.data.data) {
      returnedList.value = returnedRes.data.data.map((record: any) => ({
        recordId: record.borrowRecordId,
        bookTitle: record.bookTitle,
        author: record.author || '',
        borrowDate: record.borrowDate,
        borrowDateStr: formatDate(record.borrowDate),
        returnDate: record.returnDate,
        returnDateStr: formatDate(record.returnDate),
        borrowedDays: calculateBorrowedDays(record.borrowDate, record.returnDate)
      }));
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载借阅记录失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 刷新列表
const refreshList = async () => {
  await loadBorrowRecords();
};

onMounted(() => {
  loadBorrowRecords();
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

.empty-state {
  text-align: center;
  color: #606266;
  padding: 40px 0;
}

.overdue {
  color: #f56c6c;
  font-weight: bold;
}

.warning {
  color: #e6a23c;
  font-weight: bold;
}

.el-tabs {
  margin-top: 20px;
}
</style>
