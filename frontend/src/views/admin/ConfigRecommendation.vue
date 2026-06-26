<template>
  <div class="page-wrapper">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>⚙️ 推荐参数配置</span>
          <el-button text @click="loadConfig">刷新</el-button>
        </div>
      </template>

      <el-form :model="form" label-position="top" class="config-form">
        <div class="form-grid">
          <el-form-item label="协同过滤权重">
            <el-input-number v-model="form.collaborativeWeight" :min="0" :max="1" :step="0.05" :precision="2" />
          </el-form-item>
          <el-form-item label="内容分析权重">
            <el-input-number v-model="form.personalizedWeight" :min="0" :max="1" :step="0.05" :precision="2" />
          </el-form-item>
          <el-form-item label="用户画像权重">
            <el-input-number v-model="form.trendingWeight" :min="0" :max="1" :step="0.05" :precision="2" />
          </el-form-item>
          <el-form-item label="最小相似度阈值">
            <el-input-number v-model="form.minSimilarity" :min="0" :max="1" :step="0.05" :precision="2" />
          </el-form-item>
          <el-form-item label="冷启动策略">
            <el-select v-model="form.coldStartStrategy" style="width: 100%">
              <el-option label="热门推荐" value="TRENDING" />
              <el-option label="分类推荐" value="CATEGORY" />
              <el-option label="随机推荐" value="RANDOM" />
            </el-select>
          </el-form-item>
          <el-form-item label="生成数量 TopN">
            <el-input-number v-model="topN" :min="5" :max="100" :step="5" />
          </el-form-item>
        </div>

        <div class="tips">
          权重总和建议接近 1.00，当前：{{ weightSum.toFixed(2) }}
        </div>

        <div class="actions">
          <el-button type="primary" :loading="saving" @click="saveConfig">保存配置</el-button>
          <el-button :loading="rebuilding" @click="rebuildAll">重算全部学生</el-button>
          <el-input v-model="targetUserId" class="target-user" placeholder="按用户重算（输入用户ID）" clearable />
          <el-button type="success" plain :loading="rebuilding" @click="rebuildForUser">按用户重算</el-button>
        </div>
      </el-form>

      <div class="note">
        混合模型说明：协同过滤 + 内容分析 + 用户画像加权融合，并自动过滤已借阅图书。
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { getUserIdFromLocalStorage } from '../../util/authUtil';

interface RecommendationConfig {
  configId?: number;
  collaborativeWeight: number;
  personalizedWeight: number;
  trendingWeight: number;
  minSimilarity: number;
  coldStartStrategy: string;
}

const loading = ref(false);
const saving = ref(false);
const rebuilding = ref(false);
const topN = ref(20);
const targetUserId = ref('');

const form = reactive<RecommendationConfig>({
  collaborativeWeight: 0.45,
  personalizedWeight: 0.35,
  trendingWeight: 0.2,
  minSimilarity: 0.1,
  coldStartStrategy: 'TRENDING'
});

const weightSum = computed(() =>
  Number(form.collaborativeWeight || 0) + Number(form.personalizedWeight || 0) + Number(form.trendingWeight || 0)
);

const getAdminId = () => {
  const id = getUserIdFromLocalStorage();
  const parsed = Number(id);
  if (Number.isNaN(parsed) || parsed <= 0) {
    return null;
  }
  return parsed;
};

const loadConfig = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/admin/config/recommendation');
    if (!response.data?.success) {
      throw new Error(response.data?.message || '加载推荐配置失败');
    }

    const data = response.data.data;
    Object.assign(form, {
      configId: data?.configId,
      collaborativeWeight: Number(data?.collaborativeWeight ?? 0.45),
      personalizedWeight: Number(data?.personalizedWeight ?? 0.35),
      trendingWeight: Number(data?.trendingWeight ?? 0.2),
      minSimilarity: Number(data?.minSimilarity ?? 0.1),
      coldStartStrategy: data?.coldStartStrategy || 'TRENDING'
    });
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '加载推荐配置失败');
  } finally {
    loading.value = false;
  }
};

const saveConfig = async () => {
  const adminId = getAdminId();
  if (!adminId) {
    ElMessage.error('未获取到管理员信息，请重新登录');
    return;
  }

  if (weightSum.value <= 0) {
    ElMessage.warning('请至少设置一个有效权重');
    return;
  }

  saving.value = true;
  try {
    const response = await axios.put('/admin/config/recommendation', form, {
      params: { adminId }
    });
    if (!response.data?.success) {
      throw new Error(response.data?.message || '保存配置失败');
    }
    ElMessage.success('推荐配置已保存');
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '保存配置失败');
  } finally {
    saving.value = false;
  }
};

const rebuildAll = async () => {
  const adminId = getAdminId();
  if (!adminId) {
    ElMessage.error('未获取到管理员信息，请重新登录');
    return;
  }

  rebuilding.value = true;
  try {
    const response = await axios.post('/admin/recommendation/rebuild', null, {
      params: {
        adminId,
        topN: topN.value
      }
    });
    if (!response.data?.success) {
      throw new Error(response.data?.message || '重算失败');
    }
    ElMessage.success(response.data?.data || '推荐重算完成');
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '重算失败');
  } finally {
    rebuilding.value = false;
  }
};

const rebuildForUser = async () => {
  const adminId = getAdminId();
  const userId = Number(targetUserId.value);

  if (!adminId) {
    ElMessage.error('未获取到管理员信息，请重新登录');
    return;
  }
  if (Number.isNaN(userId) || userId <= 0) {
    ElMessage.warning('请输入有效的用户ID');
    return;
  }

  rebuilding.value = true;
  try {
    const response = await axios.post('/admin/recommendation/rebuild', null, {
      params: {
        adminId,
        userId,
        topN: topN.value
      }
    });
    if (!response.data?.success) {
      throw new Error(response.data?.message || '重算失败');
    }
    ElMessage.success(response.data?.data || '推荐重算完成');
  } catch (error: any) {
    ElMessage.error(error?.message || error?.response?.data?.message || '重算失败');
  } finally {
    rebuilding.value = false;
  }
};

onMounted(() => {
  loadConfig();
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

.config-form {
  margin-top: 4px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.tips {
  color: #606266;
  margin-bottom: 12px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.target-user {
  width: 220px;
}

.note {
  margin-top: 14px;
  color: #606266;
}
</style>
