export const resourceMap = {
  DASHBOARD: '看板',
  DATASET: '数据集',
}

// 权限粒度
export const levels = [
  {
    label: '表级',
    value: 'TABLE',
  },
  {
    label: '字段级',
    value: 'COLUMN',
  },
  {
    label: '行级',
    value: 'ROW',
  },
  {
    label: '字段与行级',
    value: 'COLUMN_ROW',
  },
]

// 权限粒度
export const levelMap = levels.reduce((acc, cur) => {
  acc[cur.value] = cur.label

  return acc
}, {})

/**
 * 天转秒
 * @param {number} d 天
 * @returns {number} 秒
 */
export function dayToSec(d) {
  return d * 24 * 60 * 60
}

export const expireDays = [
  { label: '7天', day: 7, value: dayToSec(7) },
  { label: '30天', day: 30, value: dayToSec(30) },
  { label: '90天', day: 90, value: dayToSec(90) },
  { label: '180天', day: 180, value: dayToSec(180) },
  { label: '永久', day: 0, value: 0 },
]
