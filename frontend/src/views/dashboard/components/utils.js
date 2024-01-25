import dayjs from 'dayjs'

export const getNextPush = (options = {}, format = 'YYYY-MM-DD HH:mm') => {
  const { frequency, onceValue, dayValue, weekValue, monthValue } = options

  // 当前时间
  const now = dayjs()
  const year = now.year(), // 年
    month = now.month(), // 月
    date = now.date() // 日

  // 仅一次时
  if (frequency === 'once') {
    return onceValue ? onceValue.format(format) : ' - '
  } else {
    if (!dayValue) {
      return ' - '
    }
  }

  // 下一次推送的时分（需要用来计算是不是今天）
  let [nextHour, nextMinute] = dayValue
    .format('HH:mm')
    .split(':')
    .map(n => +n) // 转化为数字

  // 下一次推送的年月日
  let nextYear = year, // 下次推送年
    nextMonth = month, // 下次推送月
    nextDate = date // 下次推送日

  let time = ''

  // 下次推送的日期
  let nextPush = dayjs()
    .year(nextYear)
    .month(nextMonth)
    .date(nextDate)
    .hour(nextHour)
    .minute(nextMinute)

  // 判断是否需要下一个周期
  const shouldNext = () => nextPush.isBefore(now)

  if (frequency === 'daily') {
    if (shouldNext()) {
      const d = nextPush.date()
      nextPush = nextPush.date(d + 1)
    }
  } else {
    if (!monthValue.length && !weekValue.length) {
      return ' - '
    }

    if (frequency === 'monthly') {
      const _date = nextPush.date()
      const monthDays = monthValue.sort((a, b) => a - b)

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
      const weekDays = weekValue.map(n => (n === 7 ? 0 : n)).sort((a, b) => a - b)

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
