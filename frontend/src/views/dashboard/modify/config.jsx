export const LAYOUT_MAX = 50

// 看板 layout 配置
export const LayoutOptions = {
  colNum: 20,
  rowHeight: 1,
  margin: [10, 10],
  graggable: true,
  resizable: false,
}

// 看板管理 layout 配置
export const ManageLayoutOptions = {
  colNum: 20,
  rowHeight: 8,
  margin: [4, 4],
  graggable: true,
  resizable: false,
}

export const DASHBORD_TO_REPORT_NAME = 'chartFiltersFromDashboardBox'

// 自动刷新频率配置
export const autoRefreshOptions = [
  { label: '1h', title: '1小时', value: 60 * 60 },
  { label: '30min', title: '30分钟', value: 30 * 60 },
  { label: '20min', title: '20分钟', value: 20 * 60 },
  { label: '15min', title: '15分钟', value: 15 * 60 },
  { label: '10min', title: '10分钟', value: 10 * 60 },
  { label: '5min', title: '5分钟', value: 5 * 60 },
]
