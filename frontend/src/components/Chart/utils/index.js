import { findBy } from 'common/utils/help'
import * as numberUtils from 'common/utils/number'
import { formatterOptions } from '@/views/dataset/config.field'
import {
  ratioOptions,
  dateGroupTypeMap,
  DEFAULT_DATE_GROUP,
  DEFAULT_RATIO_TYPE,
  COMPARE_RATIO_PERIOD,
} from '@/views/analysis/config'
import dayjs from 'dayjs'
import quarterOfYear from 'dayjs/plugin/quarterOfYear'
import weekday from 'dayjs/plugin/weekday'
dayjs.extend(quarterOfYear)
dayjs.extend(weekday)

// 对比字段
export const VS_FIELD_SUFFIX = ':VS:'

// 是否为对比字段
export const is_vs = (key = '') => key.includes(VS_FIELD_SUFFIX)

/**
 * 获取对比值
 * @param {Number} pre 前一个值（被对比值）
 * @param {Number} cur 当前值（对比值）
 * @returns {} {0, 1, '1-1', 2, '2-2'}
 */
export function getDiffvalue(pre, cur) {
  // 差值
  const dValue = cur - pre
  // 分母为0 -
  const isDZero = pre === 0
  // 差值百分比
  const dPercent = isDZero ? '-' : dValue / pre

  return {
    0: numberUtils.toThousand(cur),
    1: dValue,
    '1-1': numberUtils.toThousand(dValue),
    2: dPercent,
    '2-2': isDZero ? '-' : numberUtils.toPercent(dPercent, 2),
  }
}

/**
 * 根据字段显示其值的格式化数据
 * @param {Number | String} value 值
 * @param {Object} field 指标字段
 * @param {Array} originFields 所有字段
 * @returns
 */
export function formatFieldDisplay(value = 0, field, datasetFields) {
  if (!field) {
    console.warn('field 为空')

    return value
  }

  // 获取原始字段
  const originField = datasetFields.find(t => t.name === field.name)

  if (!originField) {
    console.warn(`找不到${field}的原始字段`)

    return value
  }
  const { dataFormat, customFormatConfig = '{}' } = originField

  let func = formatterOptions.find(v => v.value == dataFormat)?.format
  if (!func) func = s => s

  return func(value, JSON.parse(customFormatConfig))
}

/**
 * 根据排序字段创建排序方法
 * @param {boolean} isUp 是否升序
 * @param {string|number} prop 排序字段
 * @returns
 */
export function createSortByOrder(isUp = false, prop) {
  const isDate = e => isNaN(e) && !isNaN(Date.parse(e))

  return function (a, b) {
    const aV = typeof prop !== 'undefined' ? a[prop] : a
    const bV = typeof prop !== 'undefined' ? b[prop] : b

    if (typeof aV === 'string') {
      if (isDate(aV)) {
        return isUp ? new Date(aV).getTime() - new Date(bV).getTime() : new Date(bV).getTime() - new Date(aV).getTime()
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
 * 处理对比字段
 * @param {array} param.fields 字段
 * @param {array} param.vs 对比字段设置
 * @returns
 */
export const transformFieldsByVs = ({ fields = [], compare = {} }) => {
  const { type, timeField, measures = [] } = compare
  const tField = fields.find(f => f.name === timeField)

  // 将同环比配置字段进行处理
  const measuresFields = measures.map(t => {
    const {
      aggregator,
      ratioType = type || DEFAULT_RATIO_TYPE, // 兼容历史及兜底
      period = COMPARE_RATIO_PERIOD.DEFAULT,
    } = t

    return {
      ...t,
      ratioType,
      period,
      renderValue: t.name + '.' + aggregator + VS_FIELD_SUFFIX + ratioType + '.' + period,
    }
  })

  return fields.map(item => {
    const { displayName, renderName } = item
    let newDisplayName = displayName

    // 配置了同环比的字段
    const mField = measuresFields.find(t => t.renderValue === renderName)
    if (mField) {
      // 同环比配置项
      const optItem = ratioOptions.find(o => o.value === mField.ratioType)
      // 对比周期配置
      const periodOption = dateGroupTypeMap[tField.dateTrunc || DEFAULT_DATE_GROUP][mField.ratioType]
      // 对比周期
      const periodItem = periodOption?.find(p => p.value === mField.period)

      newDisplayName = newDisplayName + '-' + optItem.label + (periodItem ? '(' + periodItem.label + ')' : '')
    }

    return {
      ...item,
      displayName: newDisplayName,
      _isVs: !!mField,
    }
  })
}

const WEEK_MAP = {
  0: '日',
  1: '一',
  2: '二',
  3: '三',
  4: '四',
  5: '五',
  6: '六',
}
/**
 * 根据配置显示日期
 * @param {string} value 值
 * @param {object} field 包含配置信息的字段
 * @returns {string}
 */
export const formatDtWithOption = (value, field) => {
  const { dateTrunc, firstDayOfWeek, viewModel } = field

  if (dateTrunc === 'ORIGIN') return value
  if (dateTrunc === 'YEAR') return dayjs(value).year() + '年'
  if (dateTrunc === 'QUARTER') return '第' + dayjs(value).quarter() + '季度'
  if (dateTrunc === 'MONTH') return dayjs(value).format('YYYY-MM')

  if (dateTrunc === 'DAY') {
    if (viewModel === 'DAY_SEQUENCE') {
      return dayjs(value).format('YYYY/MM/DD')
    }

    if (viewModel === 'DAY_WEEK') {
      const d = dayjs(value)

      return d.format('YYYY/MM/DD') + '周' + WEEK_MAP[d.day()]
    }

    return dayjs(value).format('YYYY-MM-DD')
  }
  if (dateTrunc === 'WEEK') {
    const dayValue = dayjs(value).clone()
    // dayjs 将0设置为一天的第一天, 6为最后一天，而0表示周一，所以需要减1
    const start = dayValue.startOf('week').weekday(firstDayOfWeek - 1)
    const end = dayValue.endOf('week').weekday(firstDayOfWeek - 1 + 6)

    if (viewModel === 'WEEK_SEQUENCE') {
      return '第' + end.week() + '周'
    }

    if (viewModel === 'WEEK_SEQUENCE_YEAR') {
      return end.year() + '年 第' + end.week() + '周'
    }

    if (viewModel === 'WEEK_RANGE') {
      return start.format('YYYY/MM/DD') + ' - ' + end.format('YYYY/MM/DD')
    }

    if (viewModel === 'WEEK_RANGE_WITHOUT_YEAR') {
      return start.format('MM/DD') + ' - ' + end.format('MM/DD')
    }
  }

  return value
}
