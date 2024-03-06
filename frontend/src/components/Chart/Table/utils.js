import { COMPARE } from '@/CONST.dict'
import { toPercent } from 'common/utils/number'
import { formatFieldDisplay } from '@/components/Chart/utils/index.js'

/**
 * 根据分页截取数据
 * @param {array<T>} data
 * @param {{ current: number, size: number }} param
 * @returns {array<T>}
 * @example
 *  getByPager([1, 2, 3, 4, 5], { current: 2, size: 2 }) // [3, 4]
 */
export const getByPager = (data, { current, size }) => {
  return data.slice((current - 1) * size, current * size)
}

/**
 *
 * @param {number} colIndex 当前列索引
 * @param {number} allCols 所有列索引
 * @param {array<number>} fixedColumnSpan 列冻结位置
 * @returns {''|'left'|'right'}
 */
export const getFixedPlace = (colIndex, allCols, fixedColumnSpan = []) => {
  const [start, end] = fixedColumnSpan

  if (isNaN(start)) {
    return ''
  } else {
    // 前几列 + 后几列 > 总列数 => 全部冻结
    if (start + end >= allCols) return 'left'
    // 当前列 <= 前几列 => 左冻结
    if (colIndex + 1 <= start) return 'left'
    // 总列数 - 当前列 = 剩余列 <= 后几列 => 右冻结
    if (allCols - colIndex <= end) return 'right'
  }
}

export function createSortByOrder(isUp = false, prop) {
  const isDate = e => isNaN(e) && !isNaN(Date.parse(e))

  return function (a, b) {
    const aV = typeof prop !== 'undefined' ? a[prop] : a
    const bV = typeof prop !== 'undefined' ? b[prop] : b

    if (typeof aV === 'string') {
      if (isDate(aV)) {
        return isUp
          ? new Date(aV).getTime() - new Date(bV).getTime()
          : new Date(bV).getTime() - new Date(aV).getTime()
      } else {
        let aa = aV || '',
          bb = bV || ''
        return isUp ? aa.localeCompare(bb) : bb.localeCompare(aa)
      }
    } else {
      return isUp ? aV - bV : bV - aV
    }
  }
}

/**
 * 显示对比值
 * @param {number} origin 原值
 * @param {number} target 当前值
 * @param {number} mode 显示模式 0 原值 1 差值 2 差值百分比
 * @returns { (field: T, fields: Array<T>) => string}  (field, fields) => string
 */
export const getCompareDisplay = (origin, target, mode = 0) => {
  return function (field = {}, fields = []) {
    if (mode === COMPARE.MODE.DIFF_PERSENT) {
      const v = toPercent((target - origin) / origin, 2)

      return parseInt(v) > 0 ? '+' + v : v
    } else if (mode === COMPARE.MODE.DIFF) {
      const v = formatFieldDisplay(target - origin, field, fields)

      return parseInt(v) > 0 ? '+' + v : v
    } else {
      return formatFieldDisplay(origin, field, fields)
    }
  }
}
