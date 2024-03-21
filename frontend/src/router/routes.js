// alias 为兼容旧版本的地址

export default [
  {
    path: '/dashboard',
    name: 'DashboardList',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '看板列表' },
  },
  {
    path: '/dashboard/:id(\\d+)',
    alias: '/dashboard/preview_dashboard/:id(\\d+)',
    name: 'DashboardPreview',
    component: () => import('@/views/dashboard/modify/index.vue'),
    meta: { title: '看板预览' },
  },
  {
    path: '/dashboard/m/:id(\\d+)?',
    alias: '/dashboard/create_dashboard/:id(\\d+)?',
    name: 'DashboardModify',
    component: () => import('@/views/dashboard/modify/index.vue'),
    meta: { title: '看板编辑' },
  },
  {
    path: '/dataset',
    name: 'DatasetList',
    component: () => import('@/views/dataset/index.vue'),
    meta: { title: '数据集列表', related: ['DatasetAnalysis'] },
  },
  {
    path: '/dataset/m/:id(\\d+)?',
    name: 'DatasetModify',
    component: () => import('@/views/dataset/modify/index.vue'),
    meta: { title: '数据集编辑' },
  },
  {
    path: '/analysis/:id(\\d+)',
    name: 'DatasetAnalysis',
    component: () => import('@/views/analysis/index.vue'),
    meta: { title: '数据集分析' },
  },
  {
    path: '/report',
    name: 'ReportList',
    component: () => import('@/views/report/index.vue'),
    meta: { title: '图表' },
  },
  {
    path: '/report/:id(\\d+)',
    name: 'ReportDetail',
    component: () => import('@/views/analysis/index.vue'),
    meta: { title: '图表详情' },
  },

  // 兼容旧版本路由记录
  {
    path: '/dataset/analysis_data_set/:id(\\d+)',
    query: { chartId: '', from: '' },
    name: 'DatasetAnalysisDetail',
    component: () => import('@/views/analysis/index.vue'),
    meta: { title: '图表分析详情' },
  },
  {
    path: '/authority',
    name: 'AuthorityCenter',
    redirect: '/authority/apply',
    meta: { title: '权限中心' },
  },
  {
    path: '/authority/apply',
    name: 'AuthorityApply',
    component: () => import('@/views/authority/apply/index.vue'),
    meta: { title: '我的申请' },
  },
  {
    path: '/authority/applyManage',
    alias: '/authority/manage',
    name: 'AuthorityApplyManage',
    component: () => import('@/views/authority/applyManage/index.vue'),
    meta: { title: '申请管理' },
  },
  {
    path: '/system/workspace',
    alias: '/workspace',
    name: 'SystemWorkspace',
    component: () => import('@/views/system/workspace/index.vue'),
    meta: { title: '空间管理' },
  },
]
