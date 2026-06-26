<template>
  <div class="page-wrapper">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>📚 批量借阅（教师专用）</span>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="isbnInput"
          placeholder="请输入ISBN并回车"
          class="isbn-input"
          clearable
          @keyup.enter="addIsbnFromInput"
        />
        <el-button type="primary" @click="addIsbnFromInput">添加</el-button>
        <el-button @click="openScanDialog">扫码添加</el-button>
        <el-button @click="clearItems" :disabled="!borrowItems.length">清空列表</el-button>
      </div>

      <div class="summary-row">
        <el-tag type="info">已选 {{ borrowItems.length }} / 20</el-tag>
        <el-tag type="success">可借 {{ borrowableCount }}</el-tag>
        <el-tag type="danger">不可借 {{ borrowItems.length - borrowableCount }}</el-tag>
      </div>

      <el-table :data="borrowItems" border stripe v-loading="checking" class="borrow-table">
        <el-table-column type="index" label="#" width="56" />
        <el-table-column prop="isbn" label="ISBN" min-width="160" />
        <el-table-column prop="title" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" min-width="120" />
        <el-table-column label="库存" width="120">
          <template #default="{ row }">
            <span v-if="row.totalStock !== null">{{ row.availableStock }}/{{ row.totalStock }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.canBorrow ? 'success' : 'danger'">
              {{ row.canBorrow ? '可借阅' : '不可借' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="说明" min-width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button text type="danger" @click="removeItem(row.isbn)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="actions">
        <el-button type="primary" :loading="submitting" :disabled="!borrowableCount" @click="submitBatchBorrow">
          生成借阅单并提交
        </el-button>
      </div>

      <el-dialog v-model="scanDialogVisible" title="扫码添加ISBN" width="560px" @closed="stopScanner">
        <div class="scanner-box">
          <video ref="scanVideoRef" class="scan-video" muted playsinline></video>
        </div>
        <p class="scan-tip">识别成功后将自动添加到列表（重复ISBN不会重复添加）。</p>
        <template #footer>
          <el-button @click="scanDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="receiptVisible" title="批量借阅单" width="760px">
        <div id="batch-receipt" class="receipt" v-if="receipt">
          <h3>图书馆批量借阅单</h3>
          <p>单号：{{ receipt.receiptNo }}</p>
          <p>用户ID：{{ receipt.userId }}</p>
          <p>时间：{{ formatDateTime(receipt.createdAt) }}</p>
          <p>结果：成功 {{ receipt.successCount }} 本，失败 {{ receipt.failedCount }} 本</p>
          <el-table :data="receipt.items" size="small" border>
            <el-table-column prop="isbn" label="ISBN" min-width="150" />
            <el-table-column prop="title" label="书名" min-width="180" />
            <el-table-column prop="status" label="状态" width="100" />
            <el-table-column prop="message" label="说明" min-width="160" />
            <el-table-column label="应还日期" min-width="150">
              <template #default="{ row }">{{ row.dueDate ? formatDateTime(row.dueDate) : '-' }}</template>
            </el-table-column>
          </el-table>
        </div>
        <template #footer>
          <el-button @click="printReceipt" :disabled="!receipt">打印</el-button>
          <el-button @click="exportReceipt" :disabled="!receipt">导出CSV</el-button>
          <el-button type="primary" @click="receiptVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { BrowserMultiFormatReader } from '@zxing/browser';
import { BarcodeFormat, DecodeHintType } from '@zxing/library';

type BorrowItem = {
  isbn: string;
  bookId: number | null;
  title: string;
  author: string;
  totalStock: number | null;
  availableStock: number | null;
  canBorrow: boolean;
  message: string;
};

type ReceiptItem = {
  isbn: string;
  title: string;
  status: string;
  message: string;
  dueDate: string | null;
};

type Receipt = {
  receiptNo: string;
  createdAt: string;
  userId: number;
  requestedCount: number;
  successCount: number;
  failedCount: number;
  items: ReceiptItem[];
};

const MAX_BATCH_COUNT = 20;
const isbnInput = ref('');
const borrowItems = ref<BorrowItem[]>([]);
const checking = ref(false);
const submitting = ref(false);

const receiptVisible = ref(false);
const receipt = ref<Receipt | null>(null);

const scanDialogVisible = ref(false);
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

const borrowableCount = computed(() => borrowItems.value.filter((item) => item.canBorrow).length);

const getUserId = () => {
  const raw = localStorage.getItem('userId');
  const parsed = Number(raw);
  if (Number.isNaN(parsed) || parsed <= 0) {
    return null;
  }
  return parsed;
};

const formatDateTime = (value: string) => {
  if (!value) return '-';
  return new Date(value).toLocaleString('zh-CN', { hour12: false });
};

const createFallbackItem = (isbn: string, message: string): BorrowItem => ({
  isbn,
  bookId: null,
  title: '-',
  author: '-',
  totalStock: null,
  availableStock: null,
  canBorrow: false,
  message
});

const fetchBookByIsbn = async (isbn: string): Promise<BorrowItem> => {
  try {
    const response = await axios.get('/borrow/books/by-isbn', { params: { isbn } });
    if (!response.data?.success || !response.data?.data) {
      return createFallbackItem(isbn, response.data?.message || '图书不存在');
    }
    const data = response.data.data;
    return {
      isbn,
      bookId: data.bookId ?? null,
      title: data.title || '-',
      author: data.author || '-',
      totalStock: data.totalStock ?? null,
      availableStock: data.availableStock ?? null,
      canBorrow: Boolean(data.canBorrow),
      message: data.canBorrow ? '可借阅' : '库存不足'
    };
  } catch (error: any) {
    return createFallbackItem(isbn, error?.response?.data?.message || '查询失败');
  }
};

const addIsbn = async (rawIsbn: string) => {
  const isbn = rawIsbn.trim();
  if (!isbn) {
    ElMessage.warning('请输入ISBN');
    return;
  }
  if (borrowItems.value.length >= MAX_BATCH_COUNT) {
    ElMessage.warning('一次最多借阅20本');
    return;
  }
  if (borrowItems.value.some((item) => item.isbn === isbn)) {
    ElMessage.warning('该ISBN已在列表中');
    return;
  }

  checking.value = true;
  const item = await fetchBookByIsbn(isbn);
  borrowItems.value.push(item);
  checking.value = false;
};

const addIsbnFromInput = async () => {
  const current = isbnInput.value;
  isbnInput.value = '';
  await addIsbn(current);
};

const removeItem = (isbn: string) => {
  borrowItems.value = borrowItems.value.filter((item) => item.isbn !== isbn);
};

const clearItems = () => {
  borrowItems.value = [];
};

const submitBatchBorrow = async () => {
  const userId = getUserId();
  if (!userId) {
    ElMessage.error('未获取到用户信息，请重新登录');
    return;
  }
  const borrowableIsbns = borrowItems.value.filter((item) => item.canBorrow).map((item) => item.isbn);
  if (!borrowableIsbns.length) {
    ElMessage.warning('暂无可借阅图书');
    return;
  }

  submitting.value = true;
  try {
    const response = await axios.post('/borrow/batch', {
      userId,
      isbns: borrowableIsbns
    });
    if (!response.data?.success || !response.data?.data) {
      throw new Error(response.data?.message || '批量借阅失败');
    }

    receipt.value = response.data.data as Receipt;
    receiptVisible.value = true;
    ElMessage.success(`批量借阅完成：成功 ${receipt.value.successCount} 本`);

    const failedIsbnSet = new Set(
      (receipt.value.items || [])
        .filter((item) => item.status !== 'SUCCESS')
        .map((item) => item.isbn)
    );
    borrowItems.value = borrowItems.value.filter((item) => failedIsbnSet.has(item.isbn));
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '批量借阅失败');
  } finally {
    submitting.value = false;
  }
};

const openScanDialog = async () => {
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
      async (result) => {
      if (!result) {
        return;
      }
      const isbn = result.getText();
      if (!isbn) {
        return;
      }
      await addIsbn(isbn);
      ElMessage.success(`已添加：${isbn}`);
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

const printReceipt = () => {
  const target = document.getElementById('batch-receipt');
  if (!target) {
    ElMessage.warning('暂无可打印内容');
    return;
  }

  const printWindow = window.open('', '_blank');
  if (!printWindow) {
    ElMessage.error('无法打开打印窗口');
    return;
  }

  printWindow.document.write(`
    <html>
      <head>
        <title>批量借阅单</title>
        <style>
          body { font-family: Arial, "Microsoft YaHei", sans-serif; padding: 16px; }
          table { width: 100%; border-collapse: collapse; margin-top: 12px; }
          th, td { border: 1px solid #ddd; padding: 8px; font-size: 12px; }
          h3 { margin: 0 0 10px; }
        </style>
      </head>
      <body>${target.innerHTML}</body>
    </html>
  `);
  printWindow.document.close();
  printWindow.focus();
  printWindow.print();
};

const exportReceipt = () => {
  if (!receipt.value) {
    return;
  }
  const header = ['单号', '时间', '用户ID', 'ISBN', '书名', '状态', '说明', '应还日期'];
  const rows = receipt.value.items.map((item) => [
    receipt.value?.receiptNo,
    formatDateTime(receipt.value?.createdAt || ''),
    String(receipt.value?.userId || ''),
    item.isbn,
    item.title || '-',
    item.status,
    item.message || '-',
    item.dueDate ? formatDateTime(item.dueDate) : '-'
  ]);

  const csv = [header, ...rows]
    .map((row) => row.map((cell) => `"${String(cell).replace(/"/g, '""')}"`).join(','))
    .join('\n');

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `batch-borrow-${receipt.value.receiptNo}.csv`;
  link.click();
  window.URL.revokeObjectURL(url);
};

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
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.isbn-input {
  width: 360px;
}

.summary-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.borrow-table {
  margin-bottom: 12px;
}

.actions {
  display: flex;
  justify-content: flex-end;
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
}

.receipt h3 {
  margin-top: 0;
}

@media (max-width: 768px) {
  .isbn-input {
    width: 100%;
  }
}
</style>
