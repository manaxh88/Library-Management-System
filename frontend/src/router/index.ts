import { createRouter, createWebHistory } from 'vue-router';
import { getTokenFromLocalStorage, getRoleFromToken } from '../util/authUtil';

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue'),
    meta: { requiresAuth: true, roles: [0, 1, 2], title: '首页' }
  },
  // 管理员模块
  {
    path: '/admin',
    name: 'admin-dashboard',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true, roles: [0], title: '管理员仪表板' }
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: () => import('../views/admin/UserManagement.vue'),
    meta: { requiresAuth: true, roles: [0], title: '用户管理' }
  },
  {
    path: '/admin/books',
    name: 'admin-books',
    component: () => import('../views/admin/BookManagement.vue'),
    meta: { requiresAuth: true, roles: [0], title: '图书管理' }
  },
  {
    path: '/admin/statistics',
    name: 'admin-statistics',
    component: () => import('../views/admin/Statistics.vue'),
    meta: { requiresAuth: true, roles: [0], title: '统计分析' }
  },
  {
    path: '/admin/config',
    name: 'admin-config',
    component: () => import('../views/admin/ConfigRecommendation.vue'),
    meta: { requiresAuth: true, roles: [0], title: '推荐配置' }
  },
  {
    path: '/admin/renew-approvals',
    name: 'admin-renew-approvals',
    component: () => import('../views/admin/RenewApproval.vue'),
    meta: { requiresAuth: true, roles: [0], title: '续借审批' }
  },
  // 教师模块
  {
    path: '/teacher',
    redirect: '/teacher/batch-borrow'
  },
  {
    path: '/teacher/batch-borrow',
    name: 'teacher-batch-borrow',
    component: () => import('../views/teacher/BatchBorrow.vue'),
    meta: { requiresAuth: true, roles: [1], title: '批量借阅' }
  },
  {
    path: '/teacher/recommendations',
    name: 'teacher-recommendations',
    component: () => import('../views/teacher/Recommendations.vue'),
    meta: { requiresAuth: true, roles: [1], title: '推荐列表' }
  },
  {
    path: '/teacher/renew-requests',
    name: 'teacher-renew-requests',
    component: () => import('../views/teacher/RenewRequests.vue'),
    meta: { requiresAuth: true, roles: [1], title: '续借申请' }
  },
  // 学生模块
  {
    path: '/student',
    redirect: '/student/recommendations'
  },
  {
    path: '/student/recommendations',
    name: 'student-recommendations',
    component: () => import('../views/student/Recommendations.vue'),
    meta: { requiresAuth: true, roles: [2], title: '个性化推荐' }
  },
  {
    path: '/student/reminders',
    name: 'student-reminders',
    component: () => import('../views/student/Reminders.vue'),
    meta: { requiresAuth: true, roles: [2], title: '借阅提醒' }
  },
  {
    path: '/student/borrow-history',
    name: 'student-borrow-history',
    component: () => import('../views/student/BorrowHistory.vue'),
    meta: { requiresAuth: true, roles: [2], title: '借阅历史' }
  },
  // 通用模块
  {
    path: '/books',
    name: 'books',
    component: () => import('../views/BooksView.vue'),
    meta: { requiresAuth: true, roles: [0, 1, 2], title: '图书浏览' }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { requiresAuth: true, roles: [0, 1, 2], title: '个人资料' }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = getTokenFromLocalStorage();
  const nowPage = to.path;

  // 获取当前用户角色
  const userRole = token ? getRoleFromToken(token) : null;

  // 如果路由不需要认证
  if (!to.meta.requiresAuth) {
    next();
    return;
  }

  // 如果没有token且路由需要认证，跳转到登录
  if (!token) {
    next({ path: '/login', query: { redirect: nowPage } });
    return;
  }

  // 检查角色权限
  const allowedRoles = (to.meta.roles as number[]) || [];
  if (allowedRoles.length > 0 && userRole !== null && !allowedRoles.includes(userRole)) {
    // 没有权限，显示403或跳转到首页
    next({ path: '/' });
    return;
  }

  next();
});

export default router;
