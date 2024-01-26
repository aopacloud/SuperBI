export const EXPOSURE_TIME = 2 * 1000

export const permissionMap = {
  AUTHORIZED: {
    text: '开通提醒',
    status: '已开通',
    color: '#52c41a',
    tip: '权限',
  },
  EXPIRED: {
    text: '过期提醒',
    status: '已过期',
    color: 'rgba(0, 0, 0, 0.25)',
    tip: '权限',
  },
  EXPIRING: {
    text: '到期提醒',
    status: '已到期',
    color: 'rgba(0, 0, 0, 0.25)',
    tip: '权限',
  },
}

export const approveMap = {
  PASSED: {
    text: '申请通过提醒',
    status: '已通过',
    color: '#52c41a',
    tip: '权限申请',
  },
  REJECTED: {
    text: '申请驳回提醒',
    status: '被驳回',
    color: '#ff4d4f',
    tip: '权限申请',
  },
  UNDER_REVIEW: {
    text: '审批提醒',
    status: '待审批',
    color: '#1677ff',
    tip: '权限申请',
  },
}

export const resourceMap = {
  DATASOURCE: '数据源',
  DATASET: '数据集',
  REPORT: '图表',
  DASHBOARD: '看板',
}

export const tipMap = {
  PERMISSION: user => `可联系数据负责人${user}进行续期，也可直接点击右侧按钮重新申请权限`,
  APPROVAL: user => `可联系数据负责人${user}进行咨询`,
}
