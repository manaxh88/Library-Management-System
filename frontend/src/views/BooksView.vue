<template>
  <div class="books-page">
    <section class="topbar">
      <div>
        <p class="kicker">Library</p>
        <h2>图书浏览</h2>
      </div>
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索书名"
          clearable
          class="w-search"
          @keyup.enter="runSearch"
        />
        <el-select v-model="category" clearable placeholder="分类" class="w-select">
          <el-option label="计算机科学" value="计算机科学" />
          <el-option label="文学" value="文学" />
          <el-option label="历史" value="历史" />
          <el-option label="经济管理" value="经济管理" />
          <el-option label="哲学" value="哲学" />
          <el-option label="艺术" value="艺术" />
          <el-option label="教育" value="教育" />
          <el-option label="自然科学" value="自然科学" />
          <el-option label="社会科学" value="社会科学" />
          <el-option label="其他" value="其他" />
        </el-select>
        <el-select v-model="stock" placeholder="库存状态" class="w-select">
          <el-option label="全部" value="all" />
          <el-option label="可借" value="available" />
          <el-option label="库存紧张" value="tight" />
          <el-option label="已借空" value="out" />
        </el-select>
        <el-button type="primary" @click="runSearch">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </section>

    <section class="summary">
      <div class="chip">本页图书 {{ books.length }}</div>
      <div class="chip">可借 {{ availableSum }}</div>
      <div class="chip">紧张 {{ tightCount }}</div>
      <div class="chip">借空 {{ outCount }}</div>
    </section>

    <section class="list" v-loading="loading">
      <article v-for="book in shownBooks" :key="book.bookId" class="book-item">
        <div class="poster" @click="openDetail(book)">
          <img v-if="book.coverUrl" :src="book.coverUrl" alt="cover" />
          <div v-else class="poster-empty">BOOK</div>
        </div>
        <div class="info">
          <h3>{{ book.title }}</h3>
          <p>作者：{{ book.author }}</p>
          <p>分类：{{ book.category }}</p>
          <p>馆藏：{{ book.location }}</p>
          <div class="stock-row">
            <span>在馆 {{ book.availableStock }}/{{ book.totalStock }}</span>
            <el-tag :type="stockTagType(book)">{{ stockTagText(book) }}</el-tag>
          </div>
          <el-progress :percentage="stockPercent(book)" :stroke-width="10" />
          <div class="actions">
            <el-button size="small" @click="openDetail(book)">查看详情</el-button>
          </div>
        </div>
      </article>
      <div v-if="!loading && !shownBooks.length" class="empty">暂无匹配图书</div>
    </section>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :page-sizes="[8, 12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @current-change="fetchBooks"
        @size-change="onSizeChange"
      />
    </div>

    <el-drawer v-model="detailVisible" title="图书详情" size="420px">
      <div v-if="activeBook" class="detail-box">
        <h3>{{ activeBook.title }}</h3>
        <p>ISBN：{{ activeBook.isbn || '暂无' }}</p>
        <p>作者：{{ activeBook.author }}</p>
        <p>分类：{{ activeBook.category }}</p>
        <p>馆藏位置：{{ activeBook.location }}</p>
        <p>库存：{{ activeBook.availableStock }}/{{ activeBook.totalStock }}</p>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

type Book = {
  bookId: number;
  isbn: string;
  title: string;
  author: string;
  category: string;
  location: string;
  totalStock: number;
  availableStock: number;
  coverUrl?: string;
};

const books = ref<Book[]>([]);
const keyword = ref('');
const category = ref('');
const stock = ref<'all' | 'available' | 'tight' | 'out'>('all');
const page = ref(1);
const size = ref(12);
const total = ref(0);
const loading = ref(false);

const detailVisible = ref(false);
const activeBook = ref<Book | null>(null);

const shownBooks = computed(() => {
  return books.value.filter((book) => {
    if (category.value && book.category !== category.value) {
      return false;
    }
    if (stock.value === 'available') {
      return book.availableStock > 0;
    }
    if (stock.value === 'tight') {
      return book.availableStock > 0 && book.availableStock < 5;
    }
    if (stock.value === 'out') {
      return book.availableStock <= 0;
    }
    return true;
  });
});

const availableSum = computed(() => books.value.reduce((sum, item) => sum + Number(item.availableStock || 0), 0));
const tightCount = computed(() => books.value.filter((item) => item.availableStock > 0 && item.availableStock < 5).length);
const outCount = computed(() => books.value.filter((item) => item.availableStock <= 0).length);

const fetchBooks = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/books', {
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value || undefined
      }
    });

    if (!response.data?.success) {
      throw new Error(response.data?.message || '加载图书失败');
    }

    books.value = response.data?.data?.content || [];
    total.value = response.data?.data?.totalElements || 0;
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '加载图书失败');
  } finally {
    loading.value = false;
  }
};

const runSearch = () => {
  page.value = 1;
  fetchBooks();
};

const resetFilters = () => {
  keyword.value = '';
  category.value = '';
  stock.value = 'all';
  page.value = 1;
  fetchBooks();
};

const openDetail = (book: Book) => {
  activeBook.value = book;
  detailVisible.value = true;
};

const onSizeChange = () => {
  page.value = 1;
  fetchBooks();
};

const stockTagType = (book: Book) => {
  if (book.availableStock <= 0) return 'danger';
  if (book.availableStock < 5) return 'warning';
  return 'success';
};

const stockTagText = (book: Book) => {
  if (book.availableStock <= 0) return '已借空';
  if (book.availableStock < 5) return '库存紧张';
  return '可借';
};

const stockPercent = (book: Book) => {
  if (!book.totalStock || book.totalStock <= 0) return 0;
  return Math.round((Number(book.availableStock || 0) / Number(book.totalStock)) * 100);
};

onMounted(fetchBooks);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&display=swap");

.books-page {
  min-height: auto;
  padding: 0;
  font-family: "Noto Serif SC", "Microsoft YaHei", serif;
}

.topbar,
.summary,
.list,
.pagination {
  width: 100%;
  max-width: none;
  margin: 0;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  padding: 16px;
}

.kicker {
  margin: 0 0 4px;
  color: #64748b;
  font-size: 12px;
  letter-spacing: 2px;
}

h2 {
  margin: 0;
  color: #0f172a;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.w-search {
  width: 220px;
}

.w-select {
  width: 140px;
}

.summary {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
}

.chip {
  background: #fff;
  border-radius: 999px;
  padding: 8px 12px;
  color: #475569;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.06);
}

.list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
  padding: 12px;
}

.book-item {
  background: #fff;
  border-radius: 14px;
  padding: 12px;
  display: grid;
  grid-template-columns: 96px 1fr;
  gap: 12px;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.06);
}

.poster {
  width: 96px;
  height: 132px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
}

.poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.poster-empty {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background: #e2e8f0;
  color: #334155;
  font-weight: 700;
}

.info h3 {
  margin: 0 0 6px;
  color: #0f172a;
}

.info p {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
  font-size: 13px;
}

.stock-row {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #475569;
  font-size: 13px;
}

.actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.empty {
  text-align: center;
  color: #64748b;
  padding: 28px 0;
}

.pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  padding: 0 8px;
}

.detail-box {
  color: #334155;
  line-height: 1.8;
}

@media (max-width: 768px) {
  .topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .filters {
    width: 100%;
  }

  .w-search,
  .w-select {
    width: 100%;
  }

  .book-item {
    grid-template-columns: 78px 1fr;
  }

  .poster {
    width: 78px;
    height: 108px;
  }
}
</style>
