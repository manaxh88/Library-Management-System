<template>
  <div class="app">
    <header v-if="showNav" class="nav">
      <div class="nav-top">
        <div class="brand">
          <div class="mark">图</div>
          <span>图书管理系统</span>
        </div>
        <button
          class="mobile-toggle"
          type="button"
          :aria-expanded="mobileMenuOpen"
          aria-label="切换导航菜单"
          @click="mobileMenuOpen = !mobileMenuOpen"
        >
          {{ mobileMenuOpen ? '✕' : '☰' }}
        </button>
      </div>
      <nav class="links" :class="{ open: mobileMenuOpen }">
        <router-link
          v-for="item in navLinks"
          :key="item.path"
          :to="item.path"
          @click="onNavItemClick"
        >
          {{ item.label }}
        </router-link>
      </nav>
    </header>
    <main :class="{ 'app-main': showNav }">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getRoleFromToken, getTokenFromLocalStorage } from './util/authUtil';

type NavItem = {
  path: string;
  label: string;
};

const route = useRoute();
const showNav = computed(() => route.path !== '/login');
const mobileMenuOpen = ref(false);

const authToken = ref(getTokenFromLocalStorage());

const updateAuthToken = () => {
  authToken.value = getTokenFromLocalStorage();
};

const handleStorage = (event: StorageEvent) => {
  if (!event.key || event.key === 'token') {
    updateAuthToken();
  }
};

onMounted(() => {
  window.addEventListener('storage', handleStorage);
  window.addEventListener('auth-changed', updateAuthToken as EventListener);
});

onBeforeUnmount(() => {
  window.removeEventListener('storage', handleStorage);
  window.removeEventListener('auth-changed', updateAuthToken as EventListener);
});

watch(
  () => route.path,
  () => {
    mobileMenuOpen.value = false;
  }
);

const onNavItemClick = () => {
  mobileMenuOpen.value = false;
};

const navLinks = computed<NavItem[]>(() => {
  const token = authToken.value;
  const role = token ? getRoleFromToken(token) : null;

  const commonLinks: NavItem[] = [
    { path: '/', label: '首页' },
    { path: '/books', label: '图书浏览' },
    { path: '/profile', label: '个人资料' }
  ];

  if (role === 0) {
    return [
      { path: '/admin', label: '管理员仪表板' },
      { path: '/admin/users', label: '用户管理' },
      { path: '/admin/books', label: '图书管理' },
      { path: '/admin/statistics', label: '统计分析' },
      { path: '/admin/config', label: '推荐配置' },
      { path: '/admin/renew-approvals', label: '续借审批' },
      { path: '/books', label: '图书浏览' },
      { path: '/profile', label: '个人资料' }
    ];
  }

  if (role === 1) {
    return [
      { path: '/', label: '首页' },
      { path: '/teacher/batch-borrow', label: '批量借阅' },
      { path: '/teacher/recommendations', label: '推荐列表' },
      { path: '/teacher/renew-requests', label: '续借申请' },
      { path: '/books', label: '图书浏览' },
      { path: '/profile', label: '个人资料' }
    ];
  }

  if (role === 2) {
    return [
      { path: '/', label: '首页' },
      { path: '/student/recommendations', label: '个性化推荐' },
      { path: '/student/reminders', label: '借阅提醒' },
      { path: '/student/borrow-history', label: '借阅历史' },
      { path: '/books', label: '图书浏览' },
      { path: '/profile', label: '个人资料' }
    ];
  }

  return commonLinks;
});
</script>

<style scoped>
.app {
  min-height: 100vh;
}

.nav {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.24);
  font-family: "Noto Serif SC", "Microsoft YaHei", serif;
}

.nav-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  color: #0f172a;
}

.mark {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: #0f172a;
  color: #fff;
  font-size: 14px;
}

.links {
  display: flex;
  gap: 16px;
  font-weight: 600;
}

.links a {
  text-decoration: none;
  color: #475569;
  padding: 6px 10px;
  border-radius: 999px;
  transition: all 0.2s ease;
}

.links a.router-link-active {
  background: #1e293b;
  color: #fff;
}

.links a:hover {
  color: #0f172a;
}

.mobile-toggle {
  display: none;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.45);
  background: #fff;
  color: #0f172a;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
}

.logout {
  border: 1px solid rgba(148, 163, 184, 0.4);
  background: transparent;
  color: #0f172a;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout:hover {
  border-color: #0f172a;
  background: #0f172a;
  color: #fff;
}

@media (max-width: 720px) {
  .nav {
    display: grid;
    gap: 10px;
    padding: 10px 12px;
  }

  .mobile-toggle {
    display: grid;
    place-items: center;
  }

  .links {
    display: none;
    width: 100%;
    padding: 8px;
    border-radius: 12px;
    border: 1px solid rgba(148, 163, 184, 0.24);
    background: rgba(255, 255, 255, 0.92);
  }

  .links.open {
    display: grid;
    grid-template-columns: 1fr;
  }

  .links a {
    width: 100%;
    border-radius: 10px;
    padding: 8px 10px;
  }
}
</style>
