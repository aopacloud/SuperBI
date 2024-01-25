// 静态路由
export const staticRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/versions/os/views/login.vue'),
    meta: { title: '登录' },
  },
]

// 系统路由
export const systemRoutes = [
  {
    path: '/system/profile',
    name: 'SystemProfile',
    component: () => import('@/versions/os/views/system.profile/index.vue'),
    meta: { title: '个人中心', icon: 'user' },
  },
  {
    path: '/system/notification',
    name: 'SystemNotification',
    component: () => import('@/versions/os/views/system.notification/index.vue'),
    meta: { title: '消息中心', icon: 'user' },
  },
  {
    path: '/system/user',
    name: 'SystemUser',
    component: () => import('@/versions/os/views/system.user/index.vue'),
    meta: { title: '成员管理', icon: 'user', roles: ['SuperAdmin'] },
  },
  {
    path: '/system/recycleBin',
    name: 'SystemRecycleBin',
    component: () => import('@/views/system/recycleBin/index.vue'),
    meta: { title: '回收站', icon: 'user' },
  },
]

// 动态路由
export const dynamicRoutes = [
  {
    path: '/datasource',
    name: 'Datasource',
    component: () => import('@/versions/os/views/datasource/index.vue'),
    meta: { title: '数据源' },
  },
  {
    path: '/datasource/m/:id?',
    name: 'DatasourceModify',
    component: () => import('@/versions/os/views/datasource/modify/index.vue'),
    meta: { title: '创建数据源' },
  },
  {
    path: '/authority/approve',
    name: 'AuthorityApprove',
    component: () => import('@/views/authority/approve/index.vue'),
    meta: { title: '我的审批', permissions: ['DATASET:VIEW:CREATE'] },
  },
]
