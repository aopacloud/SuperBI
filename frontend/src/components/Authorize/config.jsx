import { Badge } from 'ant-design-vue'
import dayjs from 'dayjs'

// 权限粒度
export const levels = {
  TABLE: '表级',
  COLUMN: '字段级',
  ROW: '行级',
  COLUMN_ROW: '字段与行级',
}

/**
 * 天转毫秒
 * @param {number} d 天
 * @returns {number} 毫秒
 */
export function dayToMillsec(d) {
  return dayToSec(d) * 1000
}

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

/**
 * 计算过期时间
 * @param {Date} start 开始时间
 * @param {number} duration 过期间隔
 * @returns {string} 过期时间
 */
export function calcuExpire(start, duration) {
  const startStamp = start ? dayjs(start).valueOf() : Date.now()
  const endStamp = startStamp + duration * 1000

  return dayjs(endStamp).format('YYYY-MM-DD HH:mm:ss')
}

// 即将过期
export const isWillExpire = (start, duration) => {
  const endM = dayjs(calcuExpire(start, duration))
  const startM = dayjs()
  const diff = endM.diff(startM)

  return diff > 0 && diff < dayToSec(7) * 1000
}

/**
 * 判断是否到期
 * @param {string} start 开始时间
 * @param {number} duration 有效期（秒）
 * @returns {boolean}
 */
export const isExpired = (start, duration) => {
  // 永久
  if (duration === 0) return false

  const s = dayjs(start)
  const n = dayjs()

  return n.diff(s) > duration * 1000
}

export const tableColumns = [
  {
    title: '用户',
    width: 220,
    dataIndex: 'usernameAlias',
  },
  {
    title: '类型',
    dataIndex: 'permission',
    width: 100,
  },
  {
    title: '粒度',
    width: 90,
    dataIndex: 'privilegeType',
    customRender: ({ value }) => {
      return levels[value] || '-'
    },
  },
  {
    title: '状态',
    dataIndex: 'expired',
    width: 100,
    customRender: ({ value }) => {
      return (
        <Badge
          status={value ? 'error' : 'success'}
          text={value ? '已到期' : '正常使用'}
        />
      )
    },
  },
  {
    title: '到期时间',
    dataIndex: 'expireDuration',
    width: 170,
    customRender: ({ record }) => {
      const { startTime, expireDuration } = record
      if (!expireDuration) return '永久'

      return calcuExpire(startTime, expireDuration)
    },
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 120,
  },
]

// 操作符号
export const allOperators = {
  TIME: [
    { label: '等于', value: '1' },
    { label: '不等于', value: '2' },
    { label: '开头是', value: '3' },
    { label: '结尾是', value: '4' },
    { label: '包含', value: '5' },
    { label: '不包含', value: '6' },
    { label: '有值', value: '13' },
    { label: '无值', value: '14' },
  ],
  TEXT: [
    { label: '等于', value: '1' },
    { label: '不等于', value: '2' },
    { label: '开头是', value: '3' },
    { label: '结尾是', value: '4' },
    { label: '包含', value: '5' },
    { label: '不包含', value: '6' },
    { label: '有值', value: '13' },
    { label: '无值', value: '14' },
  ],
  NUMBER: [
    { label: '=', value: '1' },
    { label: '≠', value: '2' },
    { label: '>', value: '7' },
    { label: '<', value: '8' },
    { label: '≥', value: '9' },
    { label: '≤', value: '10' },
    { label: '有值', value: '13' },
    { label: '无值', value: '14' },
  ],
}
