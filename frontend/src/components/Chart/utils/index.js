import {
  formatterOptions,
  FORMAT_DEFAULT_CODE
} from '@/views/dataset/config.field'
import {
  ratioOptions,
  dateGroupTypeMap,
  DEFAULT_RATIO_TYPE,
  DEFAULT_WEEK_DISPLAY,
  DEFAULT_DAY_DISPLAY,
  DEFAULT_MONTH_DISPLAY,
  DEFAULT_DATE_GROUP,
  GROUP_WEEK,
  GROUP_DAY,
  GROUP_HOUR,
  GROUP_MINUTE,
  GROUP_MONTH
} from '@/views/analysis/config'
import {
  listDataToTreeByKeys,
  displayQuickCalculateValue
} from '../Table/utils'
import dayjs from 'dayjs'
import quarterOfYear from 'dayjs/plugin/quarterOfYear'
import weekday from 'dayjs/plugin/weekday'
import { CATEGORY } from '@/CONST.dict'
dayjs.extend(quarterOfYear)
dayjs.extend(weekday)

// 对比字段
export const VS_FIELD_SUFFIX = ':VS:'

// 是否为对比字段
export const is_vs = (key = '') => key.includes(VS_FIELD_SUFFIX)

/**
 * 根据字段显示其值的格式化数据
 * @param {Number | String} value 值
 * @param {Object} field 指标字段
 * @param {Array} originFields 所有字段
 * @param {Array} formatters 格式化数据
 * @param {Function} cb 自定义格式化
 * @returns
 */
export function formatFieldDisplay(
  value = 0,
  field,
  datasetFields,
  formatters = [],
  cb
) {
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

  const fieldName = field.name + '.' + field.aggregator
  let formatItem = formatters.find(t => t.field === fieldName)
  if (!formatItem || formatItem.code === FORMAT_DEFAULT_CODE) {
    formatItem = {
      code: originField.dataFormat,
      config: originField.customFormatConfig || '{}'
    }
  }

  if (typeof cb === 'function') {
    const r = cb(value, formatItem)

    if (r) return r
  }

  let item = formatterOptions.find(v => v.value == formatItem.code)
  if (!item) return value

  return item.format(value, formatItem.config)
}

/**
 * 处理对比字段
 * @param {array} param.fields 字段
 * @param {array} param.vs 对比字段设置
 * @returns
 */
export const transformFieldsByVs = ({ fields = [], compare = {} }) => {
  const _getOptions2 = (ratioType = DEFAULT_RATIO_TYPE, timeField) => {
    const { dateTrunc = DEFAULT_DATE_GROUP } = timeField
    const _dateTrunc = dateTrunc.startsWith(GROUP_MINUTE)
      ? GROUP_MINUTE
      : dateTrunc

    return dateGroupTypeMap[_dateTrunc][ratioType] || []
  }

  const { type, timeField, measures = [] } = compare
  const tField = fields.find(f => f.name === timeField) || {}

  // 将同环比配置字段进行处理
  const measuresFields = measures.map(t => {
    const {
      aggregator,
      ratioType = type || DEFAULT_RATIO_TYPE, // 兼容历史及兜底
      period = _getOptions2(ratioType, tField)[0]?.['value']
    } = t

    return {
      ...t,
      ratioType,
      period,
      renderValue:
        t.name + '.' + aggregator + VS_FIELD_SUFFIX + ratioType + '.' + period
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
      const dateTrunc = tField.dateTrunc?.startsWith(GROUP_MINUTE)
        ? GROUP_MINUTE
        : tField.dateTrunc
      // 对比周期配置
      const periodOption =
        dateGroupTypeMap[dateTrunc || DEFAULT_DATE_GROUP][mField.ratioType]
      // 对比周期
      const periodItem = periodOption?.find(p => p.value === mField.period)

      newDisplayName =
        newDisplayName +
        '-' +
        optItem.label +
        (periodItem ? '(' + periodItem.label + ')' : '')

      item.fastCompute = undefined
    }

    return {
      ...item,
      displayName: newDisplayName,
      _isVs: !!mField
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
  6: '六'
}
/**
 * 根据配置显示日期
 * @param {string} value 值
 * @param {object} field 包含配置信息的字段
 * @returns {string}
 */
export const formatDtWithOption = (value, field) => {
  const {
    dateTrunc = '',
    firstDayOfWeek,
    viewModel,
    _weekStart,
    _monthStart
  } = field

  const clonedValue = dayjs(value).clone()

  if (dateTrunc === DEFAULT_DATE_GROUP) return value
  if (dateTrunc === 'YEAR') return clonedValue.year() + '年'
  if (dateTrunc === 'QUARTER') return '第' + clonedValue.quarter() + '季度'
  if (dateTrunc === GROUP_HOUR || dateTrunc.startsWith(GROUP_MINUTE))
    return value

  // 天分组
  if (dateTrunc === GROUP_DAY) {
    if (viewModel === DEFAULT_DAY_DISPLAY) {
      return clonedValue.format('YYYY/MM/DD')
    }

    if (viewModel === 'DAY_WEEK') {
      const d = clonedValue

      return d.format('YYYY/MM/DD') + '周' + WEEK_MAP[d.day()]
    }

    return clonedValue.format('YYYY-MM-DD')
  }

  // 周分组
  if (dateTrunc === GROUP_WEEK) {
    // dayjs 将0设置为一天的第一天, 6为最后一天，而0表示周一，所以需要减1
    const start = clonedValue.startOf('week').weekday(firstDayOfWeek - 1)
    const end = clonedValue.endOf('week').weekday(firstDayOfWeek - 1 + 6)

    if (viewModel === DEFAULT_WEEK_DISPLAY) {
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

    if (viewModel === 'WEEK_DAY_SEQUENCE') {
      if (!_weekStart) return value

      if (_weekStart === 'MONDAY')
        return clonedValue.startOf('week').format('YYYY/MM/DD')

      if (_weekStart === 'SUNDAY')
        return clonedValue.endOf('week').format('YYYY/MM/DD')

      if (_weekStart === 'START') return start.format('YYYY/MM/DD')

      if (_weekStart === 'END') return end.format('YYYY/MM/DD')
    }
  }

  // 月分组
  if (dateTrunc === GROUP_MONTH) {
    if (viewModel === DEFAULT_MONTH_DISPLAY) {
      if (!_monthStart) return value

      if (_monthStart === 'START')
        return clonedValue.startOf('month').format('YYYY/MM/DD')

      if (_monthStart === 'END')
        return clonedValue.endOf('month').format('YYYY/MM/DD')
    }

    if (viewModel === 'MONTH_YEAR') return clonedValue.format('YYYY-MM')

    if (viewModel === 'MONTH_YEAR_CN')
      return clonedValue.year() + '年 ' + (clonedValue.month() + 1) + '月'
  }

  return value
}

export const isEmpty = v => typeof v === 'undefined' || v === null || v === ''

export const transformQuickCompute = ({ list = [], columns = [], summary }) => {
  const dataList = list.map(row => {
    return row.reduce((a, b, i) => {
      a[columns[i].renderName] = b
      return a
    }, {})
  })

  const groupKeys = columns
    .filter(t => t.category === CATEGORY.PROPERTY)
    .map(t => t.renderName)
  // 树结构
  const treeData = listDataToTreeByKeys({
    list: dataList,
    groupKeys,
    summaryMap: summary,
    renderType: 'table'
  })

  const result = list.map((_row, rowIndex) => {
    return _row.reduce((a, b, i) => {
      const row = dataList[rowIndex]
      const col = columns[i]

      const fastCompute = col.fastCompute

      if (!fastCompute) {
        a.push(b)
      } else {
        let quickValue = displayQuickCalculateValue({
          row,
          field: col,
          columns: columns.map(c => ({ ...c, params: { field: c } })),
          listTree: treeData,
          summary
        })

        if (fastCompute.includes('ratio')) {
          quickValue = (quickValue.replace('%', '') * 100).toFixed(2) / 10000
        }

        a.push(quickValue)
      }

      return a
    }, [])
  })

  return result
}
