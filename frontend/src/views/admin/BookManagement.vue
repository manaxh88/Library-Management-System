<template>
  <div class="page-wrapper">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>📚 图书管理</span>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="按书名搜索"
          clearable
          class="search-input"
          @keyup.enter="fetchBooks"
        />
        <el-button type="primary" @click="fetchBooks">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="openCreate">新增图书</el-button>
        <el-button type="primary" plain @click="openBorrowActionDialog('borrow')">借书</el-button>
        <el-button type="warning" plain @click="openBorrowActionDialog('return')">还书</el-button>
      </div>

      

      <el-table :data="books" v-loading="tableLoading" border stripe>
        <el-table-column prop="bookId" label="ID" width="70" />
        <el-table-column prop="title" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="location" label="馆藏位置" min-width="120" />
        <el-table-column prop="totalStock" label="总库存" width="90" />
        <el-table-column prop="availableStock" label="在馆" width="80" />
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="scope">
            <el-button text type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button text type="danger" @click="onDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="ISBN" prop="isbn">
          <el-input
            ref="bookIsbnInputRef"
            :model-value="form.isbn"
            @update:model-value="handleBookIsbnInput"
            placeholder="请输入 ISBN"
            class="book-isbn-input"
          >
            <template #append>
              <el-button @click="openScanDialog('book')">扫描ISBN</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="馆藏位置" prop="location">
          <el-input v-model="form.location" placeholder="如 A区-3架-2层" />
        </el-form-item>

        <div class="stock-row">
          <el-form-item label="总库存" prop="totalStock">
            <el-input-number v-model="form.totalStock" :min="0" />
          </el-form-item>
          <el-form-item label="在馆库存" prop="availableStock">
            <el-input-number v-model="form.availableStock" :min="0" />
          </el-form-item>
        </div>

        <el-form-item label="封面链接" prop="coverUrl">
          <el-input v-model="form.coverUrl" placeholder="可选" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="borrowActionDialogVisible" :title="borrowActionDialogTitle" width="420px">
      <el-form :model="borrowActionForm" label-position="top">
        <el-form-item label="用户名" required>
          <el-input v-model="borrowActionForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="ISBN" required>
          <el-input v-model="borrowActionForm.isbn" placeholder="请输入ISBN" clearable>
            <template #append>
              <el-button @click="openScanDialog('borrowAction')">扫描ISBN</el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowActionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="borrowActionLoading" @click="submitBorrowAction">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="scanDialogVisible" :title="scanDialogTitle" width="560px" @closed="handleScanDialogClosed">
      <div class="scanner-box">
        <video ref="scanVideoRef" class="scan-video" muted playsinline></video>
      </div>
      <div class="scan-tip">
        请将图书背面的条形码对准摄像头，识别成功后会自动填入 ISBN。
      </div>
      <template #footer>
        <el-button @click="scanDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules, InputInstance } from 'element-plus';
import { BrowserMultiFormatReader } from '@zxing/browser';
import { BarcodeFormat, DecodeHintType } from '@zxing/library';

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

type PagedResponse<T> = {
  content: T[];
  totalElements: number;
};

const books = ref<Book[]>([]);
const keyword = ref('');
const tableLoading = ref(false);
const saving = ref(false);

const borrowActionDialogVisible = ref(false);
const borrowActionLoading = ref(false);
const borrowActionType = ref<'borrow' | 'return'>('borrow');
const borrowActionForm = reactive({
  username: '',
  isbn: ''
});
const borrowActionDialogTitle = computed(() =>
  borrowActionType.value === 'borrow' ? '借书操作' : '还书操作'
);

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

const dialogVisible = ref(false);
const dialogTitle = ref('新增图书');
const formRef = ref<FormInstance>();
const bookIsbnInputRef = ref<InputInstance>();
const scanDialogVisible = ref(false);
const pendingBookIsbn = ref('');
const scanTarget = ref<'book' | 'borrowAction'>('book');
const scanDialogTitle = computed(() =>
  scanTarget.value === 'book' ? '扫描ISBN（图书信息）' : '扫描ISBN（借书/还书）'
);
const scanVideoRef = ref<HTMLVideoElement | null>(null);
const scanner = new BrowserMultiFormatReader(
  new Map([[DecodeHintType.POSSIBLE_FORMATS, [BarcodeFormat.EAN_13, BarcodeFormat.UPC_A, BarcodeFormat.CODE_128]]])
);
let scanControls: { stop: () => void } | null = null;

const isSecureScanContext = () => {
  const hostname = window.location.hostname;
  const isLocalhost = hostname === 'localhost' || hostname === '127.0.0.1';
  return window.isSecureContext || isLocalhost;
};

const form = reactive<Book>({
  bookId: 0,
  isbn: '',
  title: '',
  author: '',
  category: '',
  location: '',
  totalStock: 0,
  availableStock: 0,
  coverUrl: ''
});

const rules: FormRules<Book> = {
  isbn: [{ required: true, message: '请输入 ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  location: [{ required: true, message: '请输入馆藏位置', trigger: 'blur' }],
  totalStock: [{ required: true, message: '请输入总库存', trigger: 'blur' }],
  availableStock: [{ required: true, message: '请输入在馆库存', trigger: 'blur' }]
};

const fetchBooks = async () => {
  tableLoading.value = true;
  try {
    const response = await axios.get('/books', {
      params: {
        page: pagination.page,
        size: pagination.size,
        keyword: keyword.value || undefined
      }
    });

    if (!response.data?.success) {
      throw new Error(response.data?.message || '加载图书失败');
    }

    const pageData = (response.data.data || {}) as PagedResponse<Book>;
    books.value = pageData.content || [];
    pagination.total = Number(pageData.totalElements || 0);
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '加载图书失败');
  } finally {
    tableLoading.value = false;
  }
};

const resetSearch = () => {
  keyword.value = '';
  pagination.page = 1;
  fetchBooks();
};

const openCreate = () => {
  dialogTitle.value = '新增图书';
  Object.assign(form, {
    bookId: 0,
    isbn: '',
    title: '',
    author: '',
    category: '',
    location: '',
    totalStock: 0,
    availableStock: 0,
    coverUrl: ''
  });
  dialogVisible.value = true;
};

const openEdit = (row: Book) => {
  dialogTitle.value = '编辑图书';
  Object.assign(form, row);
  dialogVisible.value = true;
};

const openBorrowActionDialog = (type: 'borrow' | 'return') => {
  borrowActionType.value = type;
  borrowActionForm.username = '';
  borrowActionForm.isbn = '';
  borrowActionDialogVisible.value = true;
};

const submitBorrowAction = async () => {
  const username = borrowActionForm.username.trim();
  const isbn = borrowActionForm.isbn.trim();
  if (!username || !isbn) {
    ElMessage.warning('请输入用户名和ISBN');
    return;
  }

  borrowActionLoading.value = true;
  try {
    const endpoint = borrowActionType.value === 'borrow'
      ? '/borrow/by-identity'
      : '/borrow/by-identity/return';
    const actionLabel = borrowActionType.value === 'borrow' ? '借书' : '还书';
    const response = await axios.post(endpoint, { username, isbn });
    if (!response.data?.success) {
      throw new Error(response.data?.message || `${actionLabel}失败`);
    }
    ElMessage.success(`${actionLabel}成功`);
    borrowActionDialogVisible.value = false;
    fetchBooks();
  } catch (error: any) {
    const actionLabel = borrowActionType.value === 'borrow' ? '借书' : '还书';
    ElMessage.error(error?.message || error?.response?.data?.message || `${actionLabel}失败`);
  } finally {
    borrowActionLoading.value = false;
  }
};

const openScanDialog = async (target?: 'book' | 'borrowAction' | Event) => {
  scanTarget.value = target === 'borrowAction' ? 'borrowAction' : 'book';
  scanDialogVisible.value = true;
  await nextTick();
  startScanner();
};

const startScanner = async () => {
  if (!scanVideoRef.value) {
    return;
  }

  if (!isSecureScanContext()) {
    ElMessage.error('手机扫码需要 HTTPS 或 localhost，当前地址不安全，浏览器会禁止摄像头访问');
    return;
  }

  if (!navigator.mediaDevices?.getUserMedia) {
    ElMessage.error('当前浏览器环境无法调用摄像头，请使用 Chrome/Safari 并确认是 HTTPS 或 localhost');
    return;
  }

  stopScanner();
  try {
    scanControls = await scanner.decodeFromConstraints(
      {
        audio: false,
        video: {
          facingMode: { ideal: 'environment' }
        }
      },
      scanVideoRef.value,
      (result) => {
      if (!result) {
        return;
      }

      const isbn = result.getText();
      if (!isbn) {
        return;
      }

      const shouldFillBookForm = scanTarget.value === 'book' || dialogVisible.value;
      if (shouldFillBookForm) {
        pendingBookIsbn.value = isbn;
        syncBookIsbnInput(isbn);
        ElMessage.success(`识别成功：${isbn}，已填入新增图书ISBN`);
      } else {
        borrowActionForm.isbn = isbn;
        ElMessage.success(`识别成功：${isbn}，已填入借书/还书ISBN`);
      }
      scanDialogVisible.value = false;
      stopScanner();
      }
    );
  } catch (error: any) {
    const errorName = error?.name || '';
    if (errorName === 'NotAllowedError') {
      ElMessage.error('摄像头权限被拒绝，请在浏览器设置中允许摄像头访问');
      return;
    }
    if (errorName === 'NotFoundError') {
      ElMessage.error('未检测到可用摄像头设备');
      return;
    }
    ElMessage.error(error?.message || '无法启动摄像头，请检查浏览器权限与网络协议');
  }
};

const stopScanner = () => {
  if (scanControls) {
    scanControls.stop();
    scanControls = null;
  }
};

const handleScanDialogClosed = () => {
  stopScanner();
  if (!pendingBookIsbn.value || !dialogVisible.value) {
    return;
  }
  syncBookIsbnInput(pendingBookIsbn.value);
  pendingBookIsbn.value = '';
};

const syncBookIsbnInput = (isbn: string) => {
  form.isbn = isbn.trim();
  nextTick(() => {
    const inputEl = bookIsbnInputRef.value?.input;
    if (!inputEl || inputEl.value === form.isbn) {
      return;
    }
    inputEl.value = form.isbn;
    inputEl.dispatchEvent(new Event('input', { bubbles: true }));
    inputEl.dispatchEvent(new Event('change', { bubbles: true }));
  });
};

const handleBookIsbnInput = (value: string | number | null | undefined) => {
  form.isbn = String(value ?? '');
};

const onSave = async () => {
  const valid = await formRef.value?.validate();
  if (!valid) {
    return;
  }

  saving.value = true;
  try {
    if (form.availableStock > form.totalStock) {
      throw new Error('在馆库存不能大于总库存');
    }

    if (form.bookId) {
      const response = await axios.put(`/books/${form.bookId}`, form);
      if (!response.data?.success) {
        throw new Error(response.data?.message || '更新失败');
      }
      ElMessage.success('更新成功');
    } else {
      const response = await axios.post('/books', form);
      if (!response.data?.success) {
        throw new Error(response.data?.message || '新增失败');
      }
      ElMessage.success('新增成功');
    }

    dialogVisible.value = false;
    fetchBooks();
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '保存失败');
  } finally {
    saving.value = false;
  }
};

const onDelete = async (row: Book) => {
  try {
    await ElMessageBox.confirm(`确认删除《${row.title}》吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });

    const response = await axios.delete(`/books/${row.bookId}`);
    if (!response.data?.success) {
      throw new Error(response.data?.message || '删除失败');
    }

    ElMessage.success('删除成功');
    fetchBooks();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || error?.response?.data?.message || '删除失败');
    }
  }
};

const handlePageChange = (page: number) => {
  pagination.page = page;
  fetchBooks();
};

const handleSizeChange = (size: number) => {
  pagination.size = size;
  pagination.page = 1;
  fetchBooks();
};

onMounted(() => {
  fetchBooks();
});

onBeforeUnmount(() => {
  stopScanner();
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

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.stock-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.card-header {
  font-weight: 600;
}

.scanner-box {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
}

.scan-video {
  width: 100%;
  height: 320px;
  object-fit: cover;
  display: block;
}

.scan-tip {
  margin-top: 10px;
  color: #606266;
  font-size: 13px;
}

@media (max-width: 768px) {
  .toolbar {
    flex-wrap: wrap;
  }

  .search-input {
    width: 100%;
  }

  .stock-row {
    grid-template-columns: 1fr;
  }
}
</style>
