import dayjs from 'dayjs'
import weekday from 'dayjs/plugin/weekday'
dayjs.extend(weekday)

/**
 * 获取当前时间的 utc 偏移时间
 * @param {string} utcOffset
 * @param {Dayjs|DateStr} date
 * @returns
 */
export const getUtcDate = (utcOffset = 8, date) => {
  return dayjs(date).utcOffset(utcOffset)
}

/**
 * 获取开始日期
 * @param {{type: 'day'|'week'|'month', offset: number}}
 * @param {string} type 类型
 * @param {number} offset 偏移量
 * @returns {dayjs}
 * @example
 *  getStartDate({ type: 'day', value: -2 }) => 过去两天
 */
export const getStartDate = ({ type, offset = 0 }, utcOffset = 8) => {
  // ?? 这里是否需要用到 getUtcDate

  return dayjs().utcOffset(+utcOffset).add(offset, type).startOf(type)
}

/**
 * 获取开始日期
 * @param {{type: 'day'|'week'|'month', offset: number}}
 * @param {string} type 类型
 * @param {number} offset 偏移量
 * @returns {dayjs}
 */
export const getEndDate = ({ type, offset = 0 }, utcOffset = 8) => {
  // ?? 这里是否需要用到 getUtcDate

  return dayjs()
    .utcOffset(+utcOffset)
    .subtract(offset ? 1 : 0, type)
    .endOf(offset ? type : 'day')
}

/**
 * 根据日期选择器的值格式化显示文本
 * @param {{mode: number, offset: number[], date: string[], format?: string, extra: Object, timeOffset: number}} config 参数
 * @returns {string[]}
 */
export const displayDateFormat = ({
  mode = 0,
  offset = [],
  date = [],
  format = 'YYYY-MM-DD',
  extra = {},
  timeOffset = 8,
} = {}) => {
  if (extra.dt) return ['有数的一天']

  const utcOffset = +timeOffset

  if (mode === 0) {
    const crt = extra.current
    if (crt) {
      const [tp, of = 0] = crt.split('_')
      const s = getStartDate({ type: tp.toLowerCase(), offset: +of }, utcOffset)
      const e = getEndDate({ type: tp.toLowerCase(), offset: +of }, utcOffset)

      return [s, e].map(t => dayjs(t).format(format))
    }

    return offset.map(t => getUtcDate(utcOffset).subtract(t, 'day').format(format))
  } else {
    return date.map(t => dayjs(t).format(format))
  }
}
