/*
 * @Author: huanghe Huang.He@olaola.chat
 * @Date: 2024-04-03 16:04:59
 * @LastEditors: huanghe Huang.He@olaola.chat
 * @LastEditTime: 2024-04-07 14:02:53
 * @FilePath: /dm-BDP-front/src/views/analysis/LayoutContent/components/Filter/utils.js
 */
import { displayDateFormat } from 'common/components/DatePickers/utils'
import { operatorMap, isTime_HHMMSS } from '@/views/dataset/config.field'

/**
 * 显示过滤条件信息
 * @param {{ dataType: string, conditions: Array<过滤条件>, logical: string, filterMode: string }} field 过滤字段
 * @param {number} timeOffset 时区配置
 * @returns
 */
export const displayFilter = (field, { timeOffset = +8, format = 'YY/MM/DD' }) => {
  const { dataType = '', conditions = [], logical } = field

  // if (!dataType) return

  // 日期
  if (dataType.includes('TIME')) {
    const { timeType, args, useLatestPartitionValue, timeParts, _this, _until } =
      conditions[0] || {}

    return displayDateFormat({
      mode: timeType === 'RELATIVE' ? 0 : 1,
      offset: args,
      date: args,
      hms: isTime_HHMMSS(dataType) ? timeParts : undefined,
      extra: {
        dt: useLatestPartitionValue,
        current: _this,
        until: _until,
      },
      timeOffset,
      format,
    }).join(' - ')
  } else {
    return conditions
      .map(t => {
        const { functionalOperator, args = [] } = t
        const isNumber = args.every(t => typeof t === 'number')
        const hasV = isNumber ? args.length : args.filter(Boolean).length > 0

        const label = functionalOperator
          ? operatorMap.DEFAULT[functionalOperator] + ' '
          : ''

        if (!hasV) return label

        if (isNumber) return label + args.join('、')

        const ags = args.map(t => t?.trim())

        return label + "'" + ags.join('、') + "'"
      })
      .filter(Boolean)
      .join(logical === 'AND' ? ' 且 ' : ' 或 ')
  }
}
