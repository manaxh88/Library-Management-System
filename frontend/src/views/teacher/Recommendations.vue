<template>
  <div class="page-wrapper">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>⭐ 个性化推荐</span>
          <el-button text @click="loadRecommendations">刷新</el-button>
        </div>
      </template>

      <el-table :data="recommendations" border stripe v-loading="loading">
        <el-table-column prop="title" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" width="140" />
        <el-table-column label="推荐来源" width="120">
          <template #default="scope">
            <el-tag type="success">{{ getTypeLabel(scope.row.recommendType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="融合评分" width="120">
          <template #default="scope">
            {{ formatScore(scope.row.score) }}
          </template>
        </el-table-column>
        <el-table-column prop="recommendReason" label="推荐理由" min-width="280" />
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

      <div v-if="!loading && recommendations.length === 0" class="empty-state">
        暂无推荐结果，请稍后重试。
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { getUserIdFromLocalStorage, getTokenFromLocalStorage, getUserInfoFromToken } from '../../util/authUtil';

type RecommendationItem = {
  recommendId: number;
  bookId: number;
  title: string;
  author: string;
  coverUrl?: string;
  recommendReason: string;
  score: number;
  recommendType: number;
};

type PagedResponse<T> = {
  content: T[];
  totalElements: number;
};

const loading = ref(false);
const recommendations = ref<RecommendationItem[]>([]);

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

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

  return null;
};

const loadRecommendations = async () => {
  const userId = getUserId();
  if (!userId) {
    ElMessage.error('未获取到用户信息，请重新登录');
    return;
  }

  loading.value = true;
  try {
    const response = await axios.get('/teacher/recommendations', {
      params: {
        userId,
        page: pagination.page,
        size: pagination.size
      }
    });

    if (!response.data?.success) {
      throw new Error(response.data?.message || '加载推荐失败');
    }

    const pageData = (response.data.data || {}) as PagedResponse<RecommendationItem>;
    recommendations.value = pageData.content || [];
    pagination.total = Number(pageData.totalElements || 0);
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '加载推荐失败');
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (value: number) => {
  pagination.page = value;
  loadRecommendations();
};

const handleSizeChange = (value: number) => {
  pagination.size = value;
  pagination.page = 1;
  loadRecommendations();
};

const formatScore = (value: number) => {
  const parsed = Number(value);
  if (Number.isNaN(parsed)) {
    return '0.0000';
  }
  return parsed.toFixed(4);
};

const getTypeLabel = (type: number) => {
  if (type === 0) {
    return '个性化';
  }
  if (type === 1) {
    return '热门';
  }
  if (type === 2) {
    return '相关';
  }
  return '未知';
};

onMounted(() => {
  loadRecommendations();
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

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  color: #606266;
  padding: 24px 0 8px;
}
</style>
