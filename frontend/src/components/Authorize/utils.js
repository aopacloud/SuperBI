import dayjs from 'dayjs'

/**
 * 天转秒
 * @param {number} d 天
 * @returns {number} 秒
 */
export function dayToSec(d) {
  return d * 24 * 60 * 60
}

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
