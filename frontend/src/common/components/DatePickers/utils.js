import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import weekday from 'dayjs/plugin/weekday'
dayjs.extend(utc)
dayjs.extend(weekday)

/**
 * 获取当前时间的 utc 偏移时间
 * @param {string} utcOffset
 * @param {Dayjs|DateStr} date
 * @returns
 */
export const getUtcDate = (utcOffset = 8, date) => {
  return dayjs(date).utcOffset(+utcOffset)
}

/**
 * 获取开始日期
 * @param {{type: 'day'|'week'|'month', offset: number}}
 * @param {string} type 类型
 * @param {number} offset 偏移量
 * @returns {dayjs}
 * @example
 *  getStartDateStr({ type: 'day', value: -2 }) => 过去两天
 */
export const getStartDateStr = ({ type, offset = 0 }, utcOffset = 8) => {
  return getUtcDate(utcOffset).add(+offset, type).startOf(type).format('YYYY-MM-DD')
}

/**
 * 获取结束日期
 * @param {{type: 'day'|'week'|'month', offset: number}}
 * @param {string} type 类型
 * @param {number} offset 偏移量
 * @returns {dayjs}
 */
export const getEndDateStr = ({ type, offset = 0 }, utcOffset = 8) => {
  return getUtcDate(utcOffset)
    .subtract(+offset ? 1 : 0, type)
    .endOf(offset ? type : 'day')
    .format('YYYY-MM-DD')
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
  hms = ['', ''],
  format = 'YYYY-MM-DD',
  extra = {},
  timeOffset = 8,
} = {}) => {
  if (extra.dt) return ['有数的一天']

  const utcOffset = +timeOffset
  const validateHms = hms.map(t => (!!t ? t : ''))

  const joinDateWithHms = (d, i) => {
    const dateStr = dayjs(d).format(format)
    const hmsStr = validateHms[i]
    if (!hmsStr) return dateStr

    return dateStr + ' ' + hmsStr
  }

  // 自某日至*，需要将其中的动态时间转为静态时间
  if (!!extra.until) {
    const e = getEndDateStr(
      { type: 'day', offset: extra.until.split('_')[1] },
      utcOffset
    )

    return [date[0], e].map(joinDateWithHms)
  } else if (mode === 0) {
    const crt = extra.current
    if (crt) {
      const [tp, of = 0] = crt.split('_')
      const s = getStartDateStr({ type: tp.toLowerCase(), offset: +of }, utcOffset)
      const e = getEndDateStr({ type: tp.toLowerCase(), offset: +of }, utcOffset)

      return [s, e].map(joinDateWithHms)
    }

    return offset.map(
      (t, i) =>
        getUtcDate(utcOffset).subtract(t, 'day').format(format) +
        (validateHms[i] ? ' ' + validateHms[i] : '')
    )
  } else {
    return date.map(joinDateWithHms)
  }
}
