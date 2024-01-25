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
  return dayjs(dayjs(date).utcOffset(utcOffset).format('YYYY-MM-DD HH:mm:ss'))
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
  utcOffset = typeof utcOffset === 'number' ? utcOffset : +utcOffset

  const map = {
    day: () => dayjs().utcOffset(utcOffset).add(offset, 'day'),
    week: () => dayjs().utcOffset(utcOffset).add(offset, 'week').day(1),
    month: () => dayjs().utcOffset(utcOffset).add(offset, 'month').date(1),
  }

  return map[type]()
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
  utcOffset = typeof utcOffset === 'number' ? utcOffset : +utcOffset

  const map = {
    day: o =>
      dayjs()
        .utcOffset(utcOffset)
        .subtract(o === 0 ? 0 : 1, 'day'),
    week: () => {
      if (offset === 0) {
        return dayjs().utcOffset(utcOffset).endOf('day')
      } else {
        return dayjs().utcOffset(utcOffset).subtract(1, 'week').day(7)
      }
    },
    month: () => {
      if (offset === 0) {
        return dayjs().utcOffset(utcOffset)
      } else {
        return dayjs().utcOffset(utcOffset).subtract(1, 'month').endOf('month')
      }
    },
  }

  return map[type](offset)
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

  if (mode === 0) {
    const crt = extra.current
    if (crt) {
      const [tp, of = 0] = crt.split('_')
      const s = getStartDate({ type: tp.toLowerCase(), offset: +of }, timeOffset)
      const e = getEndDate({ type: tp.toLowerCase(), offset: +of }, timeOffset)

      return [s, e].map(t => dayjs(t).format(format))
    }

    return offset.map(t => dayjs().subtract(t, 'day').format(format))
  } else {
    return date.map(t => dayjs(t).format(format))
  }
}
