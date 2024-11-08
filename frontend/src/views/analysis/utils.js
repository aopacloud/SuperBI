import { message } from 'ant-design-vue'
import { CATEGORY } from '@/CONST.dict'
import { VS_FIELD_SUFFIX } from '@/components/Chart/utils'
import {
  defaultTableOptions,
  defaultChartOptions,
  defaultIndexCardOptions
} from './defaultOptions'
import { deepClone } from 'common/utils/help'
import { quickCalculateOptions } from '@/views/dataset/config.field'

// 字段与聚合方式连接符(后端返回的拼接规则)
export const AGGREGATOR_SPLIT = '@'

//
export const VS_SPLIT = '#'

// 获取拼接了聚合方式的字段名
export const getNameByJoinAggregator = ({ name, aggregator }) => {
  return name + '.' + aggregator
}

/**
 * 去重指标(名称相同，聚合方式相同)
 * @param {<T>} list
 * @param {Function} cb 相同后的处理
 * @returns <T>
 */
export const repeatIndex = (list = [], cb) => {
  return list.reduce((acc, cur) => {
    if (!acc.length) {
      acc.push(cur)
    } else {
      const unique = acc.every(t => {
        if (t.name !== cur.name) return true
        if (t.aggregator !== cur.aggregator) return true
        if (t.fastCompute !== cur.fastCompute) return true
        return false
      })

      if (unique) {
        acc.push(cur)
      } else {
        cb(cur)
      }
    }

    return acc
  }, [])
}

/**
 * 对维度进行排序
 * @param list 维度列表，默认为空数组
 * @param reversed 是否反转排序顺序，默认为false
 * @returns 排序后的维度列表，按照先行后列的顺序或者先列后行的顺序返回
 */
export const sortDimension = (list = [], reversed) => {
  const { rows, columns } = list.reduce(
    (acc, item) => {
      if (item._group === 'column') {
        acc.columns.push(item)
      } else {
        acc.rows.push(item)
      }

      return acc
    },
    { rows: [], columns: [] }
  )

  return reversed ? [...columns, ...rows] : [...rows, ...columns]
}

// 处理渲染列
export const transformColumns = ({ fields = [], fieldNames = [] } = {}) => {
  return fieldNames.map(fieldName => {
    let originName = '' // 原始字段名，用于查找选中的字段
    let aggregator = '' // 指标汇总方式
    let vs = '' // vs
    let fullName = '' // 拼接指标汇总方式 和 vs 的全部名
    let quick = ''

    if (fieldName.endsWith(AGGREGATOR_SPLIT)) {
      // 维度字段
      fullName = originName = fieldName.slice(0, -1)
    } else {
      const [pre, ...res] = fieldName.split(VS_SPLIT)
      if (res.length) {
        fieldName = pre
        vs = VS_FIELD_SUFFIX + res.join('.')
      }

      const [oName, oAggregator, oQuick] = fieldName.split(AGGREGATOR_SPLIT)
      originName = oName
      aggregator = oAggregator
      quick = oQuick
    }

    fullName =
      originName +
      (aggregator ? '.' + aggregator : '') +
      (quick ? '.' + quick : '') +
      vs

    // 去除同环比标识的fullName
    const _fullName = fullName.replace(vs, '')

    const field = fields.find(t => {
      if (t.category === CATEGORY.PROPERTY) {
        return t.name === _fullName
      } else {
        return getNewNameByMeasure(t) === _fullName
      }
    })

    return {
      ...field,
      renderName: fullName
    }
  })
}

/**
 * 渲染表格
 * @param {string} type 渲染类型
 * @returns {boolean}
 */
export const isRenderTable = type => {
  return ['table', 'groupTable', 'intersectionTable'].includes(type)
}

export const validateSummaryOptions = option => {
  const { renderType, table } = option
  if (!isRenderTable(renderType)) return true

  const summary = table.summary
  if (typeof summary !== 'object') return true
  const list = summary.enable ? summary.list : []
  if (list.length < 2) return true

  if (list.some(t => !t.name?.length)) {
    message.warn('汇总配置指标不能为空')
    return false
  }

  const names = list.map(t => t.name).flat()
  const distinctNames = [...new Set(names)]
  if (distinctNames.length === names.length) return true

  message.warn('同一指标不能有多个汇总配置')
  return false
}

// 获取选择的字段映射
export const getFieldsChoseMap = (res = {}) => {
  const toLower = s => s.toLowerCase() + 's'

  return {
    [CATEGORY.PROPERTY]: res[toLower(CATEGORY.PROPERTY)] ?? [],
    [CATEGORY.INDEX]: res[toLower(CATEGORY.INDEX)] ?? [],
    [CATEGORY.FILTER]: res[toLower(CATEGORY.FILTER)] ?? []
  }
}

export const compatibleHistory = (e, choseMap = {}) => {
  const { renderType, formatters, table = {}, chart = {} } = e
  const { sorter, formatter: tableFormatter = [], summary, showSummary } = table

  const dimensions = choseMap[CATEGORY.PROPERTY] || []
  const measures = choseMap[CATEGORY.INDEX] || []

  // 兼容历史表格的排序
  if (typeof sorter === 'object' && !Array.isArray(sorter)) {
    e.table.sorter = [sorter]
  }

  // 兼容历史，将表格的数据格式放在总的配置中
  if (typeof formatters === 'undefined') {
    if (tableFormatter.length) {
      e.formatters = tableFormatter
    }
  }

  // 兼容汇总版本 - 汇总配置扩展列汇总
  if (typeof summary === 'undefined') {
    e.table.summary = {
      row: {
        enable: showSummary,
        list: []
      },
      column: {
        enable: false
      }
    }
  } else if (Array.isArray(summary)) {
    e.table.summary = {
      row: {
        enable: true,
        list: summary
      },
      column: {
        enable: false
      }
    }
  }

  let { axis = [] } = chart
  // 历史数据无周设置的兼容
  if (renderType === 'bar' || renderType === 'line') {
    if (!axis.length) {
      axis = measures.map(t => ({
        ...t,
        chartType: renderType,
        yAxisPosition: 'left'
      }))
    }
  }

  if (typeof chart.yAxis.axis === 'undefined') {
    e.chart.yAxis.axis = [...axis].map(t => {
      const newItem = measures.find(m => {
        // 历史数据没有aggregator属性，默认使用历史数据配置的聚合方式
        if (!t.aggregator) {
          console.warn('历史图表 aggregator 字段缺失， 请更新图表')

          return m.name === t.name
        }

        return m.name === t.name && m.aggregator === t.aggregator
      })
      return {
        ...t,
        aggregator: newItem?.aggregator ?? t.aggregator
      }
    })

    if (axis.length > 1) {
      const yAxisPositions = axis.map(t => t.yAxisPosition)
      if ([...new Set(yAxisPositions)].length > 1) {
        e.chart.yAxis.multipleY = true
      }
    }
  }

  return e
}
export const compatibleDefault = e => {
  const { table, chart, sorters, indexCard } = e

  if (typeof sorters === 'undefined') e.sorters = {}

  // 表格特殊值
  if (typeof table.special === 'undefined') {
    e.table.special = deepClone(defaultTableOptions.special)
  }

  // x轴配置
  if (typeof chart.xAxis === 'undefined') {
    e.chart.xAxis = deepClone(defaultChartOptions.xAxis)
  }

  // y轴配置
  if (typeof chart.yAxis === 'undefined') {
    e.chart.yAxis = deepClone(defaultChartOptions.yAxis)
  }

  // 折线图的特殊值（空值连接）
  if (typeof chart.lineEmptyWith === 'undefined') {
    e.chart.lineEmptyWith = defaultChartOptions.lineEmptyWith
  }

  // 图表基础样式
  if (typeof chart.style === 'undefined') {
    e.chart.style = deepClone(defaultChartOptions.style)
  }

  // 指标卡
  if (typeof indexCard === 'undefined') {
    e.indexCard = deepClone(defaultIndexCardOptions)
  }

  return e
}

export const isSameMeasure = (a, b) => {
  return (
    a.name === b.name &&
    a.aggregator === b.aggregator &&
    a.fastCompute === b.fastCompute
  )
}

export const getNewNameByMeasure = ({
  name,
  aggregator = '',
  fastCompute = ''
}) => {
  let res = name
  if (aggregator) res += '.' + aggregator
  if (fastCompute) res += '.' + fastCompute
  return res
}

export const transformWithQuickIndex = item => {
  const { category, fastCompute } = item

  if (category === CATEGORY.INDEX && fastCompute) {
    const opt = quickCalculateOptions.find(t => t.value === fastCompute)

    return {
      ...item,
      displayName: item.displayName + '(' + opt.label + ')'
    }
  }

  return item
}
