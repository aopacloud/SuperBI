import { displayDateFormat } from 'common/components/DatePickers/utils'
import { operatorMap } from '@/views/dataset/config.field'

/**
 * 显示过滤条件信息
 * @param {{ dataType: string, conditions: Array<过滤条件>, logical: string, filterMode: string }} field 过滤字段
 * @param {number} timeOffset 时区配置
 * @returns
 */
export const displayFilter = (field, { timeOffset = +8, format = 'YY/MM/DD' }) => {
  const { dataType = '', conditions = [], logical, filterMode } = field

  if (!dataType) return

  // 日期
  if (dataType.includes('TIME')) {
    const { timeType, args, useLatestPartitionValue, timeParts, _this, _until } =
      conditions[0] || {}

    return displayDateFormat({
      mode: timeType === 'RELATIVE' ? 0 : 1,
      offset: args,
      date: args,
      single: filterMode === 'single',
      hms: dataType === 'TIME_YYYYMMDD_HHMMSS' ? timeParts : undefined,
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
        const hasV = args.filter(Boolean).length

        return hasV
          ? operatorMap.DEFAULT[functionalOperator] + "'" + args.join('、') + "'"
          : ''
      })
      .filter(Boolean)
      .join(logical === 'AND' ? '且' : '或')
  }
}
