import dayjs from 'dayjs'

/**
 * 根据配置生成 cron 表达式
 * @param {{frequency: string, hour: string, minute: string, week: string, month: string, datetime: string}} param 配置参数
 * @param {string} frequency 频率 ['daily', 'weekly', 'monthly']
 * @param {string} minute 分钟
 * @param {string} hour 小时
 * @param {string} week 周
 * @param {string} month 月
 * @param {string} datetime 仅一次
 * @returns {Cron} cron 表达式
 */
export const getCron = ({ frequency, hour, minute, week, month, datetime } = {}) => {
  const transformWeekDay = n => (n === 7 ? 1 : n + 1)

  if (frequency === 'daily') {
    return `0 ${minute} ${hour} ? * *`
  } else if (frequency === 'weekly') {
    const days = week.map(transformWeekDay).join(',')

    return `0 ${minute} ${hour} ? * ${days}`
  } else if (frequency === 'monthly') {
    const days = month.join(',')

    return `0 ${minute} ${hour} ${days} * ?`
  } else if (frequency === 'hourly') {
    const hours = hour.join(',')

    return `0 ${minute} ${hours} * * ?`
  } else {
    const [[year, month, day], [hour, minute]] = datetime
      .split(' ')
      .map(v => v.split('-'))

    return `0 ${minute} ${hour} ${day} ${month} ? ${year}`
  }
}

/**
 * 获取下一次推送的时间
 * @param {{frequency: string, onceValue: Dayjs, dailyValue: Dayjs, weeklyValue: number[], monthlyValue: number[]}} params
 * @param {string} params.frequency 频率
 * @param {Dayjs} params.onceValue 仅一次（确定的时间）
 * @param {Dayjs} params.dailyValue 日期
 * @param {Array<number>} params.weeklyValue 周几
 * @param {Array<number>} params.monthlyValue 每月的几号
 * @param {string} format 格式化的显示
 * @returns {string}
 */
export const getNext = (
  {
    frequency,
    onceValue,
    minutelyValue,
    hourlyValue,
    dailyValue,
    weeklyValue,
    monthlyValue,
  } = {},
  format = 'YYYY-MM-DD HH:mm'
) => {
  // 仅一次时
  if (frequency === 'once') {
    return onceValue ? onceValue.format(format) : ' - '
  } else {
    if (!dailyValue) {
      return ' - '
    }
  }

  // 当前时间
  const now = dayjs()
  const year = now.year(), // 年
    month = now.month(), // 月
    date = now.date() // 日

  // 下一次推送的时分（需要用来计算是不是今天）
  let [nextHour, nextMinute] = dailyValue
    .format('HH:mm')
    .split(':')
    .map(n => +n) // 转化为数字

  let time = ' - '

  // 下次推送的日期
  let nextPush = dayjs()
    .year(year)
    .month(month)
    .date(date)
    .hour(nextHour)
    .minute(nextMinute)

  // 判断是否需要下一个周期
  const shouldNext = () => nextPush.isBefore(now)

  if (frequency === 'hourly') {
    if (!hourlyValue?.length) return time

    nextPush = now.minute(minutelyValue)

    const hour = now.hour()
    const hours = [...hourlyValue].sort((a, b) => a - b)
    const nextHour = hours.find(h => {
      if (shouldNext()) {
        return h > hour
      } else {
        return h >= hour
      }
    })

    nextPush = nextPush.hour(nextHour || hours[0])

    if (shouldNext()) {
      nextPush = nextPush.date(nextPush.date() + 1)
    }
  } else if (frequency === 'daily') {
    if (shouldNext()) {
      const d = nextPush.date()
      nextPush = nextPush.date(d + 1)
    }
  } else {
    if (!monthlyValue.length && !weeklyValue.length) {
      return time
    }

    if (frequency === 'monthly') {
      const _date = nextPush.date()
      const monthDays = monthlyValue.sort((a, b) => a - b)

      // 下一个月的周几
      const nextDate = monthDays.find(d => {
        if (shouldNext()) {
          return d > _date
        } else {
          return d >= _date
        }
      })

      nextPush = nextPush.date(nextDate || monthDays[0])

      if (shouldNext()) {
        nextPush = nextPush.date(nextPush.date() + 31)
      }
    } else {
      const _day = nextPush.day()
      const weekDays = weeklyValue.map(n => (n === 7 ? 0 : n)).sort((a, b) => a - b)

      // 下一个周几
      const nextDay = weekDays.find(d => {
        if (shouldNext()) {
          return d > _day
        } else {
          return d >= _day
        }
      })

      nextPush = nextPush.day(nextDay || weekDays[0] + 7)

      if (shouldNext()) {
        nextPush = nextPush.day(nextPush.day() + 7)
      }
    }
  }

  time = nextPush.format(format)

  return time
}
