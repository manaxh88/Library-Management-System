<template>
  <div class="page-wrapper">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>📊 统计分析</span>
          <div class="header-actions">
            <el-select v-model="trendPeriod" style="width: 140px">
              <el-option label="日趋势" value="day" />
              <el-option label="周趋势" value="week" />
              <el-option label="月趋势" value="month" />
            </el-select>
            <el-button type="primary" :loading="loading" @click="fetchStatistics">刷新数据</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="summary-card">
            <div class="label">总借阅次数</div>
            <div class="value">{{ formatNumber(stats.totalBorrowCount) }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="summary-card">
            <div class="label">日均借阅</div>
            <div class="value">{{ stats.dailyAverageBorrow }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="summary-card">
            <div class="label">逾期率</div>
            <div class="value">{{ stats.overdueRate }}%</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="summary-card">
            <div class="label">重复借阅率</div>
            <div class="value">{{ stats.repeatBorrowRate }}%</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="mt16">
        <el-col :xs="24" :lg="14">
          <div class="panel-card">
            <div class="panel-title">借阅走势图（{{ trendPeriodLabel }}）</div>
            <div ref="trendChartRef" class="chart-box"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="panel-card">
            <div class="panel-title">热门分类饼图</div>
            <div ref="categoryChartRef" class="chart-box"></div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="mt16">
        <el-col :xs="24" :lg="12">
          <div class="panel-card">
            <div class="panel-title">活跃用户排行榜</div>
            <div ref="activeChartRef" class="chart-box"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="panel-card">
            <div class="panel-title">逾期控制面板</div>
            <div class="overdue-grid">
              <div class="metric-item">
                <span>逾期用户数</span>
                <strong>{{ formatNumber(stats.overdueUserCount) }}</strong>
              </div>
              <div class="metric-item">
                <span>逾期图书数</span>
                <strong>{{ formatNumber(stats.overdueBookCount) }}</strong>
              </div>
            </div>
            <ul class="ranking-list" v-if="overdueTopList.length">
              <li v-for="item in overdueTopList" :key="item.userId" class="ranking-item">
                <span>{{ item.realName || item.username }}</span>
                <span>{{ item.overdueCount }} 本 / {{ item.maxOverdueDays }} 天</span>
              </li>
            </ul>
            <div v-else class="empty-text">暂无逾期排行数据</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="mt16">
        <el-col :xs="24" :lg="12">
          <div class="panel-card">
            <div class="panel-title">逾期治理统计</div>
            <div class="govern-grid">
              <div class="metric-item">
                <span>治理对象（逾期用户）</span>
                <strong>{{ formatNumber(stats.overdueUserCount) }}</strong>
              </div>
              <div class="metric-item">
                <span>治理事件（逾期图书）</span>
                <strong>{{ formatNumber(stats.overdueBookCount) }}</strong>
              </div>
              <div class="metric-item">
                <span>逾期率</span>
                <strong>{{ stats.overdueRate }}%</strong>
              </div>
              <div class="metric-item">
                <span>重复借阅率</span>
                <strong>{{ stats.repeatBorrowRate }}%</strong>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="panel-card">
            <div class="panel-title">库存周转率分析</div>
            <div class="govern-grid">
              <div class="metric-item">
                <span>总库存</span>
                <strong>{{ formatNumber(stockStats.totalStock) }}</strong>
              </div>
              <div class="metric-item">
                <span>在馆库存</span>
                <strong>{{ formatNumber(stockStats.availableStock) }}</strong>
              </div>
              <div class="metric-item">
                <span>流转库存</span>
                <strong>{{ formatNumber(stockStats.circulatingStock) }}</strong>
              </div>
              <div class="metric-item">
                <span>库存周转率</span>
                <strong>{{ stockStats.turnoverRate }}%</strong>
              </div>
            </div>
            <el-progress :percentage="Number(stockStats.turnoverRate)" :stroke-width="14" />
          </div>
        </el-col>
      </el-row>

      <div v-if="!loading && !hasAnyData" class="empty-text page-empty">
        暂无统计数据
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';

type TrendItem = { date: string; count: number };
type CategoryItem = { category: string; count: number };
type ActiveItem = { userId: number; username: string; realName: string; borrowCount: number };
type OverdueItem = { userId: number; username: string; realName: string; overdueCount: number; maxOverdueDays: number };

type BorrowStatisticsResponse = {
  totalBorrowCount: number;
  dailyAverageBorrow: number | string;
  overdueRate: number | string;
  repeatBorrowRate: number | string;
  borrowTrend: TrendItem[];
  categoryDistribution: CategoryItem[];
  activeUsers: ActiveItem[];
  overdueUserCount: number;
  overdueBookCount: number;
  overdueRanking: OverdueItem[];
};

type BookInfo = {
  totalStock: number;
  availableStock: number;
};

type PagedResponse<T> = {
  content: T[];
  totalPages: number;
};

const loading = ref(false);
const trendPeriod = ref<'day' | 'week' | 'month'>('day');

const stats = reactive<BorrowStatisticsResponse>({
  totalBorrowCount: 0,
  dailyAverageBorrow: 0,
  overdueRate: 0,
  repeatBorrowRate: 0,
  borrowTrend: [],
  categoryDistribution: [],
  activeUsers: [],
  overdueUserCount: 0,
  overdueBookCount: 0,
  overdueRanking: []
});

const stockStats = reactive({
  totalStock: 0,
  availableStock: 0,
  circulatingStock: 0,
  turnoverRate: '0.00'
});

const trendChartRef = ref<HTMLElement | null>(null);
const categoryChartRef = ref<HTMLElement | null>(null);
const activeChartRef = ref<HTMLElement | null>(null);

let trendChart: echarts.ECharts | null = null;
let categoryChart: echarts.ECharts | null = null;
let activeChart: echarts.ECharts | null = null;

const formatNumber = (value: number) => Number(value || 0).toLocaleString('zh-CN');

const trendPeriodLabel = computed(() => {
  if (trendPeriod.value === 'week') return '周';
  if (trendPeriod.value === 'month') return '月';
  return '日';
});

const overdueTopList = computed(() => (stats.overdueRanking || []).slice(0, 8));

const hasAnyData = computed(() =>
  stats.totalBorrowCount > 0 ||
  stats.overdueBookCount > 0 ||
  stats.activeUsers.length > 0 ||
  stats.categoryDistribution.length > 0
);

const toDayLabel = (dateText: string) => dateText.slice(5);

const getWeekKey = (dateText: string) => {
  const date = new Date(dateText);
  const day = date.getDay() || 7;
  date.setDate(date.getDate() - day + 1);
  const year = date.getFullYear();
  const start = new Date(year, 0, 1);
  const diff = Math.floor((date.getTime() - start.getTime()) / 86400000);
  const week = Math.floor(diff / 7) + 1;
  return `${year}-W${String(week).padStart(2, '0')}`;
};

const getMonthKey = (dateText: string) => dateText.slice(0, 7);

const trendSeries = computed(() => {
  const source = (stats.borrowTrend || []).slice().sort((a, b) => a.date.localeCompare(b.date));
  if (!source.length) {
    return { labels: [] as string[], values: [] as number[] };
  }

  if (trendPeriod.value === 'day') {
    return {
      labels: source.map((item) => toDayLabel(item.date)),
      values: source.map((item) => Number(item.count || 0))
    };
  }

  const mapper = trendPeriod.value === 'week' ? getWeekKey : getMonthKey;
  const grouped = new Map<string, number>();
  source.forEach((item) => {
    const key = mapper(item.date);
    grouped.set(key, (grouped.get(key) || 0) + Number(item.count || 0));
  });

  const labels = Array.from(grouped.keys()).sort();
  return {
    labels,
    values: labels.map((label) => grouped.get(label) || 0)
  };
});

const renderTrendChart = () => {
  if (!trendChartRef.value) return;
  if (!trendChart) trendChart = echarts.init(trendChartRef.value);

  trendChart.setOption(
    {
      tooltip: { trigger: 'axis' },
      grid: { left: 35, right: 16, top: 22, bottom: 32 },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: trendSeries.value.labels,
        axisLabel: { color: '#909399' },
        axisLine: { lineStyle: { color: '#dcdfe6' } }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: '#909399' },
        splitLine: { lineStyle: { color: '#f0f2f5' } }
      },
      series: [
        {
          type: 'line',
          smooth: true,
          data: trendSeries.value.values,
          symbol: 'circle',
          symbolSize: 7,
          lineStyle: { color: '#409eff', width: 2 },
          itemStyle: { color: '#409eff' },
          areaStyle: { color: 'rgba(64, 158, 255, 0.15)' }
        }
      ]
    },
    true
  );
};

const renderCategoryChart = () => {
  if (!categoryChartRef.value) return;
  if (!categoryChart) categoryChart = echarts.init(categoryChartRef.value);

  categoryChart.setOption(
    {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, textStyle: { color: '#606266' } },
      series: [
        {
          type: 'pie',
          radius: ['35%', '68%'],
          data: (stats.categoryDistribution || []).map((item) => ({
            name: item.category || '未分类',
            value: Number(item.count || 0)
          }))
        }
      ]
    },
    true
  );
};

const renderActiveChart = () => {
  if (!activeChartRef.value) return;
  if (!activeChart) activeChart = echarts.init(activeChartRef.value);

  const topUsers = (stats.activeUsers || []).slice(0, 10);
  activeChart.setOption(
    {
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 16, top: 20, bottom: 48 },
      xAxis: {
        type: 'category',
        axisLabel: { color: '#909399', interval: 0, rotate: 25 },
        axisLine: { lineStyle: { color: '#dcdfe6' } },
        data: topUsers.map((item) => item.realName || item.username)
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: '#909399' },
        splitLine: { lineStyle: { color: '#f0f2f5' } }
      },
      series: [
        {
          type: 'bar',
          data: topUsers.map((item) => Number(item.borrowCount || 0)),
          itemStyle: { color: '#67c23a' },
          barMaxWidth: 32
        }
      ]
    },
    true
  );
};

const resizeCharts = () => {
  trendChart?.resize();
  categoryChart?.resize();
  activeChart?.resize();
};

const fetchBookStockSummary = async () => {
  let currentPage = 1;
  const size = 200;
  let totalPages = 1;
  let totalStock = 0;
  let availableStock = 0;

  while (currentPage <= totalPages) {
    const response = await axios.get('/books', { params: { page: currentPage, size } });
    if (!response.data?.success) {
      throw new Error(response.data?.message || '加载图书库存失败');
    }

    const pageData = (response.data.data || {}) as PagedResponse<BookInfo>;
    const content = pageData.content || [];
    totalPages = Number(pageData.totalPages || 1);

    content.forEach((book) => {
      totalStock += Number(book.totalStock || 0);
      availableStock += Number(book.availableStock || 0);
    });

    currentPage += 1;
  }

  stockStats.totalStock = totalStock;
  stockStats.availableStock = availableStock;
  stockStats.circulatingStock = Math.max(totalStock - availableStock, 0);
  stockStats.turnoverRate =
    totalStock > 0 ? ((Number(stats.totalBorrowCount || 0) / totalStock) * 100).toFixed(2) : '0.00';
};

const fetchStatistics = async () => {
  loading.value = true;
  try {
    const borrowResponse = await axios.get('/admin/statistics/borrow');
    if (!borrowResponse.data?.success) {
      throw new Error(borrowResponse.data?.message || '加载借阅统计失败');
    }

    const data = borrowResponse.data?.data || {};
    stats.totalBorrowCount = Number(data.totalBorrowCount || 0);
    stats.dailyAverageBorrow = data.dailyAverageBorrow ?? 0;
    stats.overdueRate = data.overdueRate ?? 0;
    stats.repeatBorrowRate = data.repeatBorrowRate ?? 0;
    stats.borrowTrend = Array.isArray(data.borrowTrend) ? data.borrowTrend : [];
    stats.categoryDistribution = Array.isArray(data.categoryDistribution) ? data.categoryDistribution : [];
    stats.activeUsers = Array.isArray(data.activeUsers) ? data.activeUsers : [];
    stats.overdueUserCount = Number(data.overdueUserCount || 0);
    stats.overdueBookCount = Number(data.overdueBookCount || 0);
    stats.overdueRanking = Array.isArray(data.overdueRanking) ? data.overdueRanking : [];

    await fetchBookStockSummary();

    await nextTick();
    renderTrendChart();
    renderCategoryChart();
    renderActiveChart();
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '加载统计数据失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchStatistics();
  window.addEventListener('resize', resizeCharts);
});

watch(trendPeriod, () => {
  nextTick(() => renderTrendChart());
});

watch(
  () => [stats.borrowTrend, stats.categoryDistribution, stats.activeUsers],
  () => {
    nextTick(() => {
      renderTrendChart();
      renderCategoryChart();
      renderActiveChart();
    });
  },
  { deep: true }
);

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts);
  trendChart?.dispose();
  categoryChart?.dispose();
  activeChart?.dispose();
  trendChart = null;
  categoryChart = null;
  activeChart = null;
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
  gap: 8px;
  align-items: center;
}

.summary-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px;
}

.summary-card .label {
  color: #909399;
  font-size: 13px;
}

.summary-card .value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.mt16 {
  margin-top: 16px;
}

.panel-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px;
}

.panel-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.overdue-grid,
.govern-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.metric-item {
  background: #f8fafc;
  border-radius: 8px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.metric-item strong {
  color: #303133;
  font-size: 18px;
}

.ranking-list {
  list-style: none;
  margin: 12px 0 0;
  padding: 0;
}

.ranking-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f2f5;
  color: #606266;
  font-size: 13px;
}

.ranking-item:last-child {
  border-bottom: none;
}

.empty-text {
  color: #909399;
  font-size: 13px;
}

.page-empty {
  margin-top: 14px;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions :deep(.el-select) {
    width: 100% !important;
  }

  .chart-box {
    height: 260px;
  }
}
</style>
