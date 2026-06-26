<template>
  <div class="home">
    <section class="hero">
      <div>
        <p class="kicker">智慧图书馆</p>
        <h2>图书管理系统首页</h2>
        <p class="desc">一站式完成馆藏维护、读者管理与借阅处理。</p>
        <div class="actions">
          <router-link class="btn primary" to="/books">图书浏览</router-link>
          <router-link class="btn ghost" to="/profile">用户中心</router-link>
        </div>
      </div>
      <div class="stat-card">
        <div>
          <p>今日借阅</p>
          <h3>{{ formatCount(overview.todayBorrowCount) }}</h3>
        </div>
        <div>
          <p>在馆图书</p>
          <h3>{{ formatCount(overview.availableBookCount) }}</h3>
        </div>
        <div>
          <p>活跃读者</p>
          <h3>{{ formatCount(overview.activeReaderCount) }}</h3>
        </div>
      </div>
    </section>

    <section class="grid">
      <div class="grid-header">
        <h3>热门图书</h3>
        <p>展示当前借阅热度与最近入库图书。</p>
      </div>
      <div class="books-panels">
        <div class="book-panel">
          <h4>借阅热榜 TOP 6</h4>
          <div 
            class="book-row clickable" 
            v-for="(item, index) in overview.hotBooks" 
            :key="`hot-${index}`"
            @click="showBookDetail(item)"
          >
            <span class="rank">{{ index + 1 }}</span>
            <div class="book-main">
              <p class="book-title">{{ item.title }}</p>
              <p class="book-meta">
                {{ item.author }} · {{ item.category }}
                <span v-if="item.rating" class="rating">⭐ {{ item.rating }}</span>
              </p>
            </div>
            <span class="book-extra">借阅 {{ item.borrowCount }}</span>
          </div>
          <div class="empty-line" v-if="!overview.hotBooks.length">暂无借阅热榜数据</div>
        </div>

        <div class="book-panel">
          <h4>新书速递</h4>
          <div 
            class="book-row clickable" 
            v-for="(item, index) in overview.newBooks" 
            :key="`new-${index}`"
            @click="showBookDetail(item)"
          >
            <span class="rank">{{ index + 1 }}</span>
            <div class="book-main">
              <p class="book-title">{{ item.title }}</p>
              <p class="book-meta">
                {{ item.author }} · {{ item.category }}
                <span v-if="item.rating" class="rating">⭐ {{ item.rating }}</span>
              </p>
            </div>
            <span class="book-extra" :class="item.availableStock > 0 ? 'in-stock' : 'out-of-stock'">
              可借 {{ item.availableStock }}
            </span>
          </div>
          <div class="empty-line" v-if="!overview.newBooks.length">暂无新书数据</div>
        </div>
      </div>
    </section>

    <section class="grid">
      <div class="grid-header">
        <h3>最近动态</h3>
        <p>快速了解系统内的关键操作。</p>
      </div>
      <div class="timeline">
        <div class="timeline-item" v-for="(item, index) in overview.recentActivities" :key="index">
          <div class="dot"></div>
          <div>
            <p class="timeline-title">{{ item.title }}</p>
            <p class="timeline-meta">{{ item.content }}</p>
          </div>
          <span class="timeline-time">{{ formatTime(item.time) }}</span>
        </div>
        <div class="timeline-item" v-if="!loading && overview.recentActivities.length === 0">
          <div class="dot"></div>
          <div>
            <p class="timeline-title">暂无动态</p>
            <p class="timeline-meta">当前系统尚未产生可展示的最新记录。</p>
          </div>
          <span class="timeline-time">--:--</span>
        </div>
      </div>
    </section>

    <!-- 图书详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      :title="selectedBook?.title || '图书详情'" 
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedBook" class="book-detail">
        <div class="detail-main">
          <div class="detail-cover" v-if="selectedBook.coverUrl">
            <img :src="selectedBook.coverUrl" :alt="selectedBook.title" />
          </div>
          <div class="detail-info">
            <div class="info-row">
              <label>书名：</label>
              <span>{{ selectedBook.title }}</span>
            </div>
            <div class="info-row">
              <label>作者：</label>
              <span>{{ selectedBook.author }}</span>
            </div>
            <div class="info-row">
              <label>分类：</label>
              <span >{{ selectedBook.category }}</span>
            </div>
            <div class="info-row" v-if="selectedBook.isbn">
              <label>ISBN：</label>
              <span>{{ selectedBook.isbn }}</span>
            </div>
            <div class="info-row" v-if="selectedBook.publisher">
              <label>出版社：</label>
              <span>{{ selectedBook.publisher }}</span>
            </div>
            <div class="info-row" v-if="selectedBook.rating">
              <label>评分：</label>
              <span class="rating-display">⭐ {{ selectedBook.rating }} / 5.0</span>
            </div>
            <div class="info-row">
              <label>馆藏位置：</label>
              <span>{{ selectedBook.location }}</span>
            </div>
            <div class="info-row">
              <label>库存：</label>
              <span>
                <span class="stock-info">可借 {{ selectedBook.availableStock }}</span> / 
                总计 {{ selectedBook.totalStock }}
              </span>
            </div>
            <div class="info-row" v-if="selectedBook.borrowCount !== undefined">
              <label>借阅次数：</label>
              <span>{{ selectedBook.borrowCount }} 次</span>
            </div>
          </div>
        </div>
        <div class="detail-description" v-if="selectedBook.description">
          <label>简介：</label>
          <p>{{ selectedBook.description }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

type HomeActivity = {
  type: string;
  title: string;
  content: string;
  time: string;
};

type BookItem = {
  bookId: number;
  isbn?: string;
  title: string;
  author: string;
  category: string;
  location: string;
  totalStock: number;
  availableStock: number;
  coverUrl?: string;
  publisher?: string;
  rating?: number;
  description?: string;
  borrowCount: number;
};

type HomeOverview = {
  todayBorrowCount: number;
  availableBookCount: number;
  activeReaderCount: number;
  recentActivities: HomeActivity[];
  hotBooks: BookItem[];
  newBooks: BookItem[];
};

const overview = reactive<HomeOverview>({
  todayBorrowCount: 0,
  availableBookCount: 0,
  activeReaderCount: 0,
  recentActivities: [],
  hotBooks: [],
  newBooks: []
});

const loading = ref(false);
const detailDialogVisible = ref(false);
const selectedBook = ref<BookItem | null>(null);

const fetchOverview = async () => {
  try {
    loading.value = true;
    const response = await axios.get('/home/overview');
    if (!response.data?.success) {
      throw new Error(response.data?.message || '加载首页数据失败');
    }

    const data = response.data?.data || {};
    overview.todayBorrowCount = Number(data.todayBorrowCount || 0);
    overview.availableBookCount = Number(data.availableBookCount || 0);
    overview.activeReaderCount = Number(data.activeReaderCount || 0);
    overview.recentActivities = Array.isArray(data.recentActivities)
      ? data.recentActivities.map((item: any) => ({
          type: String(item?.type || ''),
          title: String(item?.title || '系统动态'),
          content: String(item?.content || '-'),
          time: String(item?.time || '')
        }))
      : [];
    overview.hotBooks = Array.isArray(data.hotBooks)
      ? data.hotBooks.map((item: any) => mapToBookItem(item))
      : [];
    overview.newBooks = Array.isArray(data.newBooks)
      ? data.newBooks.map((item: any) => mapToBookItem(item))
      : [];
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载首页数据失败');
  } finally {
    loading.value = false;
  }
};

const mapToBookItem = (item: any): BookItem => {
  return {
    bookId: Number(item?.bookId || 0),
    isbn: item?.isbn,
    title: String(item?.title || '-'),
    author: String(item?.author || '-'),
    category: String(item?.category || '未分类'),
    location: String(item?.location || '未知位置'),
    totalStock: Number(item?.totalStock || 0),
    availableStock: Number(item?.availableStock || 0),
    coverUrl: item?.coverUrl,
    publisher: item?.publisher,
    rating: item?.rating ? Number(item.rating) : undefined,
    description: item?.description,
    borrowCount: Number(item?.borrowCount || 0)
  };
};

const showBookDetail = (book: BookItem) => {
  selectedBook.value = book;
  detailDialogVisible.value = true;
};

const formatCount = (value: number) => Number(value || 0).toLocaleString('zh-CN');

const formatTime = (time: string) => {
  if (!time) {
    return '--';
  }
  const date = new Date(time);
  if (Number.isNaN(date.getTime())) {
    return '--';
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');

  return `${year}/${month}/${day} ${hour}:${minute}`;
};

onMounted(fetchOverview);
</script>

<style scoped>
.home {
  display: grid;
  gap: 24px;
  align-content: start;
  padding: 0;
  color: #0f172a;
  font-family: "Noto Serif SC", "Microsoft YaHei", serif;
}

.hero {
  width: 100%;
  margin: 0;
  border-radius: 18px;
  padding: 32px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
}

.kicker {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #64748b;
  margin: 0 0 8px;
}

.desc {
  color: #475569;
  margin: 8px 0 24px;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn {
  padding: 10px 18px;
  border-radius: 999px;
  text-decoration: none;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn.primary {
  background: #0f172a;
  color: #fff;
}

.btn.ghost {
  border: 1px solid #cbd5f5;
  color: #0f172a;
  background: #fff;
}

.stat-card {
  display: grid;
  gap: 12px;
  background: #0f172a;
  color: #fff;
  padding: 20px;
  border-radius: 20px;
}

.stat-card p {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
}

.stat-card h3 {
  margin: 6px 0 0;
  font-size: 28px;
}

.grid {
  width: 100%;
  margin: 0;
  border-radius: 18px;
  padding: 24px;
}

.grid-header h3 {
  margin: 0;
  font-size: 20px;
}

.grid-header p {
  margin: 6px 0 0;
  color: #64748b;
}

.grid-cards {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.books-panels {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.book-panel {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.book-panel h4 {
  margin: 0 0 12px;
  font-size: 16px;
}

.book-row {
  display: grid;
  grid-template-columns: 24px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #e2e8f0;
}

.book-row.clickable {
  cursor: pointer;
  padding: 10px 12px;
  margin: 0 -12px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.book-row.clickable:hover {
  background: #f8fafc;
  transform: translateX(4px);
}

.book-row:last-of-type {
  border-bottom: none;
}

.rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 999px;
  background: #0f172a;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.book-main {
  min-width: 0;
}

.book-title {
  margin: 0;
  font-size: 14px;
  color: #0f172a;
}

.book-meta {
  margin: 2px 0 0;
  color: #64748b;
  font-size: 12px;
}

.book-extra {
  color: #334155;
  font-size: 12px;
  font-weight: 600;
}

.book-extra.in-stock {
  color: #16a34a;
}

.book-extra.out-of-stock {
  color: #dc2626;
}

.rating {
  margin-left: 8px;
  color: #f59e0b;
  font-size: 11px;
}

.empty-line {
  color: #94a3b8;
  font-size: 13px;
  padding: 8px 0;
}

/* 图书详情对话框样式 */
.book-detail {
  font-family: "Noto Serif SC", "Microsoft YaHei", serif;
}

.detail-main {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.detail-cover {
  width: 120px;
  height: 160px;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  display: grid;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  font-size: 14px;
}

.info-row label {
  font-weight: 600;
  color: #64748b;
  min-width: 80px;
  flex-shrink: 0;
}

.info-row span {
  color: #0f172a;
}

.category-tag {
  display: inline-block;
  padding: 2px 10px;
  background: #0f172a;
  color: #fff;
  border-radius: 12px;
  font-size: 12px;
}

.rating-display {
  color: #f59e0b;
  font-weight: 600;
}

.stock-info {
  font-weight: 600;
  color: #16a34a;
}

.detail-description {
  padding-top: 16px;
  border-top: 1px dashed #e2e8f0;
}

.detail-description label {
  font-weight: 600;
  color: #64748b;
  display: block;
  margin-bottom: 8px;
}

.detail-description p {
  margin: 0;
  color: #475569;
  line-height: 1.6;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.feature-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 18px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  text-decoration: none;
  color: inherit;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.action-btn {
  width: 100%;
  border: none;
  cursor: pointer;
  text-align: left;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 30px rgba(15, 23, 42, 0.12);
}

.feature-card h4 {
  margin: 0 0 6px;
  font-size: 16px;
}

.feature-card p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.feature-tag {
  align-self: flex-start;
  background: #0f172a;
  color: #fff;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 999px;
}

.timeline {
  margin-top: 16px;
  display: grid;
  gap: 14px;
}

.timeline-item {
  display: grid;
  grid-template-columns: 16px 1fr auto;
  gap: 12px;
  align-items: start;
  background: #ffffff;
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #0f172a;
  margin-top: 6px;
}

.timeline-title {
  margin: 0;
  font-weight: 600;
}

.timeline-meta {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.timeline-time {
  font-size: 12px;
  color: #94a3b8;
}

@media (max-width: 768px) {
  .hero,
  .grid {
    padding: 20px;
  }

  .timeline-item {
    grid-template-columns: 16px 1fr;
  }

  .timeline-time {
    grid-column: 2;
  }

  .detail-main {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .detail-cover {
    margin: 0 auto;
  }
}
</style>
