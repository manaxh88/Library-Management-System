<template>
  <div class="admin-dashboard">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>管理员仪表板</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card user-stat-card">
            <div class="stat-value">{{ displayStats.totalUserCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card user-stat-card">
            <div class="stat-value">{{ displayStats.adminCount }}</div>
            <div class="stat-label">管理员数</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card user-stat-card">
            <div class="stat-value">{{ displayStats.teacherCount }}</div>
            <div class="stat-label">老师人数</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card user-stat-card">
            <div class="stat-value">{{ displayStats.studentCount }}</div>
            <div class="stat-label">学生人数</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="summary-row">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-value">{{ displayStats.totalBorrowCount }}</div>
            <div class="stat-label">总借阅次数</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-value">{{ displayStats.overdueUserCount }}</div>
            <div class="stat-label">逾期用户</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-value">{{ displayStats.overdueBookCount }}</div>
            <div class="stat-label">逾期图书</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-value">{{ displayStats.totalBookCount }}</div>
            <div class="stat-label">在库图书</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="detail-row">
        <el-col :xs="24" :md="12">
          <div class="panel-card">
            <div class="panel-title">最近7天借阅趋势</div>
            <div ref="trendChartRef" class="trend-echart"></div>
            <div v-if="!(stats.borrowTrend || []).length" class="panel-empty">暂无趋势数据</div>
          </div>
        </el-col>
        <el-col :xs="24" :md="12">
          <div class="panel-card">
            <div class="panel-title">活跃用户 TOP5</div>
            <ul class="panel-list" v-if="activeUserList.length">
              <li v-for="item in activeUserList" :key="item.userId" class="panel-item">
                <span>{{ item.realName || item.username }}</span>
                <strong>{{ item.borrowCount }}</strong>
              </li>
            </ul>
            <div v-else class="panel-empty">暂无活跃用户数据</div>
          </div>
        </el-col>
      </el-row>

    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { ElCard, ElRow, ElCol, ElMessage } from 'element-plus';
import axios from 'axios';
import * as echarts from 'echarts';

type BorrowStats = {
  totalUserCount: number;
  adminCount: number;
  teacherCount: number;
  studentCount: number;
  totalBorrowCount: number;
  overdueUserCount: number;
  overdueBookCount: number;
  totalBookCount: number;
  borrowTrend: Array<{ date: string; count: number }>;
  activeUsers: Array<{ userId: number; username: string; realName: string; borrowCount: number }>;
};

const stats = ref<BorrowStats>({
  totalUserCount: 0,
  adminCount: 0,
  teacherCount: 0,
  studentCount: 0,
  totalBorrowCount: 0,
  overdueUserCount: 0,
  overdueBookCount: 0,
  totalBookCount: 0,
  borrowTrend: [],
  activeUsers: []
});
const loading = ref(true);
const trendChartRef = ref<HTMLElement | null>(null);
let trendChartInstance: echarts.ECharts | null = null;

const formatNumber = (value: number) => value.toLocaleString('zh-CN');

const displayStats = computed(() => {
  if (loading.value) {
    return {
      totalUserCount: '--',
      adminCount: '--',
      teacherCount: '--',
      studentCount: '--',
      totalBorrowCount: '--',
      overdueUserCount: '--',
      overdueBookCount: '--',
      totalBookCount: '--'
    };
  }

  return {
    totalUserCount: formatNumber(stats.value.totalUserCount),
    adminCount: formatNumber(stats.value.adminCount),
    teacherCount: formatNumber(stats.value.teacherCount),
    studentCount: formatNumber(stats.value.studentCount),
    totalBorrowCount: formatNumber(stats.value.totalBorrowCount),
    overdueUserCount: formatNumber(stats.value.overdueUserCount),
    overdueBookCount: formatNumber(stats.value.overdueBookCount),
    totalBookCount: formatNumber(stats.value.totalBookCount)
  };
});

const renderTrendChart = () => {
  if (!trendChartRef.value) {
    return;
  }

  if (!trendChartInstance) {
    trendChartInstance = echarts.init(trendChartRef.value);
  }

  const trend = (stats.value.borrowTrend || []).map((item) => ({
    date: String(item.date || ''),
    count: Number(item.count || 0)
  }));

  trendChartInstance.setOption(
    {
      grid: { left: 30, right: 16, top: 20, bottom: 32 },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: trend.map((item) => item.date.slice(5)),
        axisLine: { lineStyle: { color: '#dcdfe6' } },
        axisLabel: { color: '#909399' }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLine: { show: false },
        axisLabel: { color: '#909399' },
        splitLine: { lineStyle: { color: '#f0f2f5' } }
      },
      series: [
        {
          name: '借阅次数',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 7,
          data: trend.map((item) => item.count),
          lineStyle: { color: '#409eff', width: 2 },
          itemStyle: { color: '#409eff' },
          areaStyle: { color: 'rgba(64, 158, 255, 0.12)' }
        }
      ]
    },
    true
  );
};

const resizeTrendChart = () => {
  trendChartInstance?.resize();
};

const activeUserList = computed(() =>
  (stats.value.activeUsers || []).slice(0, 5).map((item) => ({
    userId: item.userId,
    username: item.username,
    realName: item.realName,
    borrowCount: formatNumber(Number(item.borrowCount || 0))
  }))
);

const fetchDashboardStats = async () => {
  loading.value = true;
  try {
    const [userResponse, borrowResponse, booksResponse] = await Promise.all([
      axios.get('/admin/statistics/users'),
      axios.get('/admin/statistics/borrow'),
      axios.get('/books', { params: { page: 1, size: 1 } })
    ]);

    if (!userResponse.data?.success) {
      throw new Error(userResponse.data?.message || '加载用户统计失败');
    }

    if (!borrowResponse.data?.success) {
      throw new Error(borrowResponse.data?.message || '加载借阅统计失败');
    }

    if (!booksResponse.data?.success) {
      throw new Error(booksResponse.data?.message || '加载图书统计失败');
    }

    const userData = userResponse.data?.data || {};
    stats.value.totalUserCount = Number(userData.totalUserCount ?? 0);
    stats.value.adminCount = Number(userData.adminCount ?? 0);
    stats.value.teacherCount = Number(userData.teacherCount ?? 0);
    stats.value.studentCount = Number(userData.studentCount ?? 0);

    const borrowData = borrowResponse.data?.data || {};
    stats.value.totalBorrowCount = Number(borrowData.totalBorrowCount ?? 0);
    stats.value.overdueUserCount = Number(borrowData.overdueUserCount ?? 0);
    stats.value.overdueBookCount = Number(borrowData.overdueBookCount ?? 0);
    stats.value.borrowTrend = Array.isArray(borrowData.borrowTrend) ? borrowData.borrowTrend : [];
    stats.value.activeUsers = Array.isArray(borrowData.activeUsers) ? borrowData.activeUsers : [];

    const totalElements = booksResponse.data?.data?.totalElements ?? 0;
    stats.value.totalBookCount = Number(totalElements);
  } catch (error: any) {
    ElMessage.error(error?.message || '加载统计数据失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchDashboardStats();
  nextTick(() => {
    renderTrendChart();
  });
  window.addEventListener('resize', resizeTrendChart);
});

watch(
  () => stats.value.borrowTrend,
  () => {
    nextTick(() => {
      renderTrendChart();
    });
  },
  { deep: true }
);

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeTrendChart);
  if (trendChartInstance) {
    trendChartInstance.dispose();
    trendChartInstance = null;
  }
});
</script>

<style scoped>
.admin-dashboard {
  padding: 0;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.2);
  padding: 20px;
  border-radius: 14px;
  text-align: center;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.08);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.detail-row {
  margin-top: 20px;
}

.summary-row {
  margin-top: 12px;
}

.user-stat-card .stat-value {
  color: #67c23a;
}

.panel-card {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.08);
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}

.panel-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.panel-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f2f5;
  color: #606266;
}

.panel-item:last-child {
  border-bottom: none;
}

.panel-empty {
  color: #909399;
  font-size: 14px;
}

.trend-echart {
  width: 100%;
  height: 220px;
}

</style>
