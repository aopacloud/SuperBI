import { versionJs } from '@/versions'

/**
 * 创建自定义范围
 * @param {number} start 起始
 * @param {number} end 结束
 * @returns Array<number>
 * @example
 *  createRange(2, 5) => [2, 3, 4]
 */
const createRange = (start = 1, end = 100) =>
  Array.from({ length: end - start }, (_, i) => i + start)

export const presetOptions = [
  {
    label: '有数的一天',
    value: 'dt',
    type: 'dt',
    col: 50,
    hidden: () => !versionJs.ComponentsDatepickers.hasExtraDt,
  },
  { label: '今天', value: 0, type: 'day', hidden: props => !props.single },
  { label: '昨天', value: -1, type: 'day', col: 50 },
  { label: '上周', value: -1, type: 'week', col: 50, hidden: props => props.single },
  { label: '本周', value: 0, type: 'week', col: 50, hidden: props => props.single },
  {
    label: '上月',
    value: -1,
    type: 'month',
    col: 50,
    hidden: props => props.single,
  },
  { label: '本月', value: 0, type: 'month', col: 50, hidden: props => props.single },
  { label: '过去7天', value: -7, type: 'day', hidden: props => props.single },
  { label: '过去15天', value: -15, type: 'day', hidden: props => props.single },
  { label: '过去30天', value: -30, type: 'day', hidden: props => props.single },
  {
    label: '自某日至昨日',
    value: -1,
    type: 'until',
    col: 50,
    hidden: props => props.single,
  },
  {
    label: '自某日至今日',
    value: 0,
    type: 'until',
    col: 50,
    hidden: props => props.single,
  },
  {
    label: '自定义',
    value: 'custom',
    type: 'custom',
    hidden: props => props.single,
  },
]

// 自定义选择范围
export const customRangeOptions = createRange().map(t => ({
  label: t,
  value: t * -1,
}))

// 自定义模式
export const customMode = [
  { label: '天', value: 'day' },
  { label: '周', value: 'week' },
  { label: '月', value: 'month' },
]
