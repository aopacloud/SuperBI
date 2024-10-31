import { CATEGORY } from '@/CONST.dict.js'
import {
  createIsEqualFromKey,
  deepClone,
  getDiffColor,
  createSortByOrder
} from 'common/utils/help'
import { lightenColor } from 'common/utils/color'
import { toDigit, toThousand, toPercent, sum } from 'common/utils/number'
import {
  colors,
  CHART_GRID_HEIGHT
} from 'common/components/Charts/utils/default.js'
import {
  VS_FIELD_SUFFIX,
  formatDtWithOption,
  formatFieldDisplay
} from './index.js'
import { isDateField } from '@/views/dataset/utils'
import { FORMAT_DEFAULT_CODE } from '@/views/dataset/config.field.js'
import { isSameMeasure, getNewNameByMeasure } from '@/views/analysis/utils.js'

// 连接分组的字符(唯一，需要做分割)
export const GROUP_SPLIT = '§'
// 拼接指标name的字符
export const YNAME_SPLIT = '/'

// 获取最大值的占位长度
export function getStrLength(val) {
  return val.toString().length * 7 + 30
}

/**
 * 获取对比值
 * @param {Number} pre 前一个值（被对比值）
 * @param {Number} cur 当前值（对比值）
 * @returns
 */
export function getDiffValue(origin, target, mode) {
  if (mode === 2) {
    const v = toPercent((target - origin) / Math.abs(origin), 2)

    return parseInt(v) > 0 ? '+' + v : v
  } else if (mode === 1) {
    const v = toThousand(target - origin)

    return parseInt(v) > 0 ? '+' + v : v
  } else {
    return toThousand(target)
  }
}

/**
 * 是否都是相同的数值类型的字段
 * @param {array} list 字段列表
 * @returns {boolean}
 */
export function isSameFieldFormat(list = []) {
  if (list.length < 2) return true

  const isSameFormatCode = createIsEqualFromKey('dataFormat')

  if (!isSameFormatCode(list)) return false

  const isSameCustomConfig = createIsEqualFromKey('customFormatConfig')

  if (!isSameCustomConfig(list)) return false

  return true
}

/**
 * 获取图表当前尺寸
 * @param {Echarts} chart Echart 实例
 * @return {{width: number, height: number}} 宽高配置
 */
export function getChartSize(chart) {
  if (!chart) {
    return console.error('getChartSize: chart is not exist')
  }
  const width = chart.getWidth()
  const height = chart.getHeight()

  return { width, height }
}

/**
 * 处理源数据字段和数据的排序（根据维度时间）
 * @param  {{ originFields: array<T>, originData: array<K> }}
 * @param {array<T>} originFields 源字段
 * @param {array<K>} originData 源数据
 * @returns {{fields: array<T>, data: array<K>}}
 */
export const transformOriginBySort = ({
  originFields = [],
  originData = [],
  sorters = { row: [], column: [] }
}) => {
  const pFields = originFields.filter(t => t.category === CATEGORY.PROPERTY)

  let [rowSort] = deepClone(sorters.row || [])

  let xIndex
  if (rowSort) {
    xIndex = pFields.findIndex(t => t.name === rowSort.field)
  } else {
    xIndex = pFields.findIndex(isDateField)
    rowSort = {}
  }
  rowSort.field = 0
  rowSort.order = rowSort.order ?? 'asc'

  const fields = originFields.slice()

  // 维度排序换位到首位
  if (xIndex > 0) {
    fields.splice(xIndex, 1)
    fields.unshift(originFields[xIndex])
  }

  const data = originData
    .slice()
    .map(row => {
      // 维度对应的排序换位到首位
      if (xIndex > 0) {
        const item = row[xIndex]
        row.splice(xIndex, 1)
        row.unshift(item)
      }

      return row
    })
    .sort(createSortByOrder(rowSort))
  // 按照维度的升序进行排序

  return { fields, data }
}

/**
 * 生成数据map
 * @param  {{ originFields: array, yFields: array, data: array, group: array }}
 * @param {array} originFields 原始字段
 * @param {array} yFields y字段
 * @param {array} data 数据
 * @param {array} group 分组
 * @returns {{dataMap: any, xData: array}}
 */
export const generateDataMap = ({
  originFields = [],
  yFields = [],
  data = [],
  group = []
}) => {
  const xData = [
    ...new Set(
      data.map(row => {
        return row[0]
      })
    )
  ]

  // 获取非分组数据
  const getData = () => {
    const resMap = yFields.reduce((acc, cur) => {
      if (typeof acc[cur.renderName] === 'undefined') {
        acc[cur.renderName] = {
          field: cur,
          data: Array(data.length).fill(),
          max: 0
        }
      }

      return acc
    }, {})

    data.forEach((row, i, d) => {
      const yRow = row.slice(1)

      yRow.forEach((t, j) => {
        const field = yFields[j]
        if (!field) return

        resMap[field.renderName]['data'][i] = t
        resMap[field.renderName]['max'] = Math.max(
          t,
          resMap[field.renderName]['max']
        )
      })
    })

    return resMap
  }

  // 生成分组数据
  const getGroupData = () => {
    const groupLen = group.length
    const resMap = {}

    // 日期类型的显示
    const _displayGroupName = (name, i) => {
      const field = originFields[i + 1]

      if (
        field.category === CATEGORY.PROPERTY &&
        field.dataType.includes('TIME')
      ) {
        return formatDtWithOption(name, field)
      }

      return name
    }

    data.forEach(row => {
      // 从数据中取出分组名称
      const groupName =
        row
          .slice(1, groupLen + 1)
          .map(_displayGroupName)
          .join(GROUP_SPLIT) + GROUP_SPLIT

      // 遍历指标字段
      yFields.forEach(t => {
        const mapKey = groupName + t.renderName

        if (typeof resMap[mapKey] === 'undefined') {
          resMap[mapKey] = {
            field: t,
            data: Array(xData.length).fill(),
            max: 0
          }
        }
      })

      const yRow = row.slice(groupLen + 1) // 指标数据

      // 遍历维度，将数据的行索引与列索引进行变换
      xData.forEach((x, i) => {
        if (x === row[0]) {
          yRow.forEach((t, j) => {
            const field = yFields[j] // 获取指标值对应的指标字段

            if (!field) return // 避免数据请求延迟，上一次渲染数据异常
            // const groupName = groupName + GROUP_SPLIT + field.renderName
            const mapKey = groupName + field.renderName

            resMap[mapKey]['data'][i] = t
            resMap[mapKey]['max'] = Math.max(resMap[mapKey]['max'], t)
          })
        }
      })
    })

    return resMap
  }

  const result = group.length ? getGroupData() : getData()

  return {
    dataMap: result,
    xData
  }
}

/**
 * 生成布局配置
 * @param  {{ chart: Echarts, fields: array, grid: T }}
 * @param {Echarts} chart 指标数组
 * @param {array} fields 指标数组
 * @param {T} grid 布局基本信息
 * @returns {Array<T>}
 */
export function generateGrid({ splited, chart, fields = [], grid = {} }) {
  if (!chart) return

  if (!splited || fields.length < 2) {
    chart.resize({ width: 'auto', height: 'auto' })

    return { ...grid }
  }

  const domHeight = chart.getDom().clientHeight
  const gridsHeight = CHART_GRID_HEIGHT * fields.length + 50

  chart.resize({
    width: 'auto',
    height: gridsHeight > domHeight ? gridsHeight : 'auto'
  })

  return fields.map((v, i) => {
    return {
      ...grid,
      top: i * CHART_GRID_HEIGHT + 55,
      height: CHART_GRID_HEIGHT - 55
    }
  })
}

/**
 * 生成X轴
 * @param {{ yFields: array,  xAxis: T }}
 * @param {array} yFields y轴字段
 * @param {T} xAxis x轴配置
 * @returns {Array<T>}
 */
export const generateXAxis = ({ yFields = [], xAxis = {} }) => {
  return yFields.map((_t, i) => {
    return {
      ...xAxis,
      gridIndex: i
    }
  })
}

/**
 * 生成Y轴
 * @param {{ yFields: array,  yAxis: T, datasetFields: array }}
 * @param {array} yFields y轴字段
 * @param {T} yAxis y轴配置
 * @param {array} datasetFields 数据集字段
 * @param {boolean} splited true 拆分, false 聚合
 * @returns {Array<T>}
 */
export const generateYAxis = ({
  yFields = [],
  yAxis = {},
  datasetFields = [],
  axis = [],
  splited = false,
  dataMap,
  formatters = [],
  xData = []
}) => {
  const getDataByGroup = () => {
    return Object.keys(dataMap).reduce((acc, cur) => {
      const name = cur.split(GROUP_SPLIT).slice(-1).join(GROUP_SPLIT)

      if (!acc[name]) {
        acc[name] = []
      }

      acc[name].push(dataMap[cur])

      return acc
    }, {})
  }

  const dataGrouped = getDataByGroup()

  // 拆分显示，直接根据每个字段生成y轴
  if (splited) {
    return yFields.map((field, i) => {
      const axisItem = axis.find(a => a.name === field.name)
      const max = Math.max(
        ...dataGrouped[field.renderName].map(t => t.max || 0)
      )
      // 这里会导致数据格式为默认时，小数点很长（比如经纬度），导致y轴name偏移量过多而无法显示
      const maxFormat = formatFieldDisplay(
        max,
        field,
        datasetFields,
        formatters
      )

      return {
        ...yAxis,
        gridIndex: i,
        position: axisItem?.yAxisPosition || 'left',
        name: yAxis.name && field.displayName,
        nameGap: yAxis.nameGap ?? getStrLength(maxFormat),
        axisLabel: {
          ...yAxis.axisLabel,
          formatter: v => {
            const fastCompute = field.fastCompute
            if (!fastCompute) {
              return formatFieldDisplay(v, field, datasetFields, formatters)
            } else {
              if (fastCompute.includes('ratio')) {
                return (v * 100).toFixed(2) + '%'
              } else {
                return v
              }
            }
          }
        }
      }
    })
  }

  // 左右轴字段
  const [leftAxis, rightAxis] = deepClone(axis).reduce(
    (acc, cur) => {
      if (cur.yAxisPosition === 'right') {
        acc[1].push(cur)
      } else {
        acc[0].push(cur)
      }

      return acc
    },
    [[], []]
  )

  // 坐轴字段
  const leftAxisFields = yFields.filter(a =>
    leftAxis.some(b => isSameMeasure(a, b))
  )
  // 右轴字段
  const rightAxisFields = yFields.filter(a =>
    rightAxis.some(b => isSameMeasure(a, b))
  )

  const createAxis = (fields = []) => {
    if (!fields.length) return {}

    const first = fields[0]

    // 相同的字段数据格式
    let isFormatSamed = false
    if (fields.length === 1) {
      isFormatSamed = true
    } else {
      const mergesFormatters = fields.map(t => {
        const fmt = formatters.find(f => f.field === t.renderName)
        if (fmt) {
          return { ...fmt }
        } else {
          const oField = datasetFields.find(a => a.name === t.name)
          return {
            field: t.renderName,
            code: oField?.dataFormat || FORMAT_DEFAULT_CODE
          }
        }
      })

      isFormatSamed = new Set(mergesFormatters.map(t => t.code)).size === 1
    }

    // 最大值的宽度
    const maxValueWidths = fields.map(t => {
      // 快速计算的 nameGap
      const fastCompute = t?.fastCompute
      if (fastCompute?.includes('ratio')) {
        return getStrLength('100.00%')
      } else if (fastCompute?.includes('rank')) {
        const maxDataLength = Math.max(
          ...dataGrouped[t.renderName].map(a => a.data.length)
        )
        return getStrLength(maxDataLength)
      }

      let maxValue = dataGrouped[t.renderName]?.[0]?.max || 0
      maxValue = maxValue.toFixed(0) // 轴标签不显示小数（格式化去除了尾0）

      const oField = datasetFields.find(a => a.name === t.name)

      const formatItem = formatters.find(a => a.field === t.renderName)
      // 默认格式使用整数参与宽度计算，避免小数点位数过长（例如经纬度）导致y轴name偏移量过多而无法显示
      let mts = formatters
      if (!formatItem || formatItem.code === FORMAT_DEFAULT_CODE) {
        if (!oField || oField?.dataFormat === FORMAT_DEFAULT_CODE) {
          mts = [{ field: t.renderName, code: 'INTEGER' }]
        }
      }
      return getStrLength(formatFieldDisplay(maxValue, t, datasetFields, mts))
    })

    // alignTicks: 在多个 y 轴为数值轴的时候，可以开启该配置项自动对齐刻度。只对'value'和'log'类型的轴有效。
    return {
      ...yAxis,
      position: first?.yAxisPosition,
      name: yAxis.name && fields.map(t => t.displayName).join('/'),
      nameGap: yAxis.nameGap ?? Math.max(...maxValueWidths),
      scale: xData.length > 1 ? true : undefined,
      alignTicks:
        leftAxisFields.length && rightAxisFields.length ? true : undefined,
      axisLabel: {
        ...yAxis.axisLabel,
        formatter: v => {
          if (v === 0) return 0

          const fastCompute = first.fastCompute

          if (!fastCompute) {
            if (!isFormatSamed) {
              return toDigit(v, 2)
            } else {
              return formatFieldDisplay(
                v,
                first,
                datasetFields,
                formatters,
                (_, format = {}) => {
                  // 如果格式化为整数，但是轴刻度为小数，则不进行格式化
                  if (format.code === 'INTEGER' && String(v).includes('.')) {
                    return v
                  }
                }
              )
            }
          } else {
            if (fastCompute.includes('ratio')) {
              return (v * 100).toFixed(2) + '%'
            } else {
              return v
            }
          }
        }
      }
    }
  }

  const left = createAxis(leftAxisFields)
  const right = createAxis(rightAxisFields)

  return [left, right]
}

/**
 * 生成系列数据
 * @param {{ chartType: string, yFields: array, dataMap: any, group: array, config: any, datasetFields: array }}
 * @param {string} chartType 图表类型
 * @param {string[]} yFields y轴字段
 * @param {any} dataMap 数据表
 * @param {string} group 分组
 * @param {any} config 配置
 * @param {string[]} datasetFields 数据集字段
 */
export const generateSeries = ({
  chartType: _chartType = 'line',
  yFields = [],
  dataMap = {},
  group = [],
  config = {},
  datasetFields = [],
  formatters = []
}) => {
  // 数据标签
  const labelVisible = config.labelShow || false

  // 指标拆分
  const splited = config.splited

  // 轴设置
  const axis = (config.yAxis?.axis ?? config.axis) || []

  // 堆积柱图 类型
  const barType = config.style?.bar?.type
  // 柱图指标并列
  const barFlatted = config.style?.bar.flat
  // 堆积柱图
  const isBarStacked = config.renderType === 'bar' && barType === 'stacked'
  // 百分比堆积柱图
  const isPercentStacked =
    config.renderType === 'bar' && barType === 'stackedPercent'

  const getStyle = () => {
    // 线条 lineStyle.type 样式
    const lineStyle = [
      [10, 4],
      [4, 4],
      [24, 12],
      [48, 24],
      [36, 12, 6, 12],
      [36, 12, 6, 12, 6, 12],
      [60, 12, 16, 12]
    ]
    let colorIndex = 0,
      pColor = '',
      vsIndex = 0,
      type // lineStyle.type 默认undefined为直线 = [0, 0] solid
    return yIndex => {
      const field = yFields[yIndex]
      let color = colors[colorIndex]

      if (field._isVs) {
        pColor = colors[colorIndex - 1]
        color = lightenColor(pColor, 0.7 ** (vsIndex + 1))
        pColor = color
        vsIndex++
        type = lineStyle[vsIndex % lineStyle.length]
      } else {
        colorIndex++
        vsIndex = 0
        pColor = ''
        type = 'solid'
      }

      return { color, type }
    }
  }
  // 获取 label
  const getCommonSeriesLabel = () => {
    return {
      show: labelVisible,
      position: 'outside',
      formatter(series) {
        const { seriesName, value } = series
        // 百分比堆叠
        if (isPercentStacked) {
          return Math.round(value * 1000) / 10 + '%'
        } else {
          const ySourceFieldName = group.length
            ? seriesName.split(GROUP_SPLIT).slice(group.length).join('')
            : seriesName
          const field = yFields.find(t => t.displayName === ySourceFieldName)

          const fastCompute = field.fastCompute

          if (!field.fastCompute) {
            return formatFieldDisplay(value, field, datasetFields, formatters)
          } else if (fastCompute.includes('ratio')) {
            return (value * 100).toFixed(2) + '%'
          } else {
            return value
          }
        }
      }
    }
  }

  // 获取 name
  const getCommonSeriesName = (name, field) => {
    return group.length
      ? name.split(GROUP_SPLIT).slice(0, -1).join(GROUP_SPLIT) +
          GROUP_SPLIT +
          field.displayName
      : field.displayName
  }

  // 获取百分比堆积柱图的总和
  const getTotal = (list = []) => {
    const res = []
    for (let i = 0; i < list[0].length; i++) {
      let sum = 0
      for (let j = 0; j < list.length; j++) {
        sum += list[j][i] || 0
      }
      res.push(sum)
    }
    return res
  }

  const getCommonSeries = () => {
    const getSeriesStyle = getStyle()
    // 用作百分比堆积柱图的总和
    const total = isPercentStacked
      ? getTotal(Object.values(dataMap).map(t => t.data))
      : []

    return Object.keys(dataMap).reduce((acc, mapKey) => {
      let { field, data } = dataMap[mapKey]

      const fieldIndex = yFields.findIndex(
        t => t.renderName === field.renderName
      )
      const { color, type } = getSeriesStyle(fieldIndex)
      const axisItem = axis.find(
        t => getNewNameByMeasure(t) === field.renderName
      )

      const chartType = axisItem?.chartType ?? _chartType

      // 折线图特殊值置0处理
      const isLineEmptyWith0 = chartType
        ? config.lineEmptyWith === '0'
        : undefined
      data = data.map(t => {
        return t ? t : isLineEmptyWith0 ? 0 : t
      })

      // 百分比堆叠
      if (isPercentStacked) {
        data = data.map((t, i) => {
          return total[i] <= 0 ? 0 : t / total[i]
        })
      }

      // 堆积名称 - 按照坐标轴去堆叠
      let stack
      // 折线图不参与堆积
      if (isBarStacked && chartType === 'bar') {
        stack = axisItem?.yAxisPosition ?? ''

        if (!splited) {
          if (!barFlatted) {
            // 非指标并列
            stack += '-' + 'stacked' // 'left-stacked'
          } else {
            stack += '-' + field.name + '-' + 'stacked' // 'left-uid-stacked'
          }
        } else {
          if (!barFlatted) {
            stack = field.name + '-' + 'stacked'
          } else {
            stack = undefined
          }
        }
      }

      const seriesItem = {
        _y: field,
        type: chartType,
        name: getCommonSeriesName(mapKey, field),
        barMaxWidth: 40,
        color,
        data,
        stack,
        lineStyle: { type },
        connectNulls: config.lineEmptyWith === 'connect',
        label: getCommonSeriesLabel(),
        // 对label标签的布局设置
        labelLayout: ({ rect }) => {
          if (!rect.height) {
            return { dy: -20, hideOverlap: true }
          } else {
            return {
              hideOverlap: true
            }
          }
        },
        position: axisItem?.yAxisPosition,
        xAxisIndex: splited ? fieldIndex : 0,
        yAxisIndex: splited
          ? fieldIndex
          : axisItem?.yAxisPosition === 'right'
            ? 1
            : 0
      }

      return acc.concat(seriesItem)
    }, [])
  }

  return getCommonSeries()
}

/**
 * 自定义渲染 tooltip
 * @param {{ series: Array<Series>, yFields: array, datasetFields: array, group: array, compareMode: string,  }}
 * @param {Array<Series>} series 系列值
 * @param {array} yFields 渲染指标
 * @param {array} datasetFields 数据集字段
 * @param {array} group 分组索引
 * @param {String} compareMode 对比类型 0 原值，1 差值，2 差值百分比
 * @returns
 */
export function tooltipFormat({
  series = [],
  yFields = [],
  datasetFields = [],
  compareMode = 0,
  group = [],
  formatters
}) {
  const list = Array.isArray(series)
    ? series
    : [series]
        .filter(t => t.axisDim === 'x') // 过滤掉 axisPointer 的tooltip数据
        .sort((a, b) => a.seriesIndex - b.seriesIndex)
  const hasGroup = group.length > 0
  // 按照指标分组显示
  const listMap = list.reduce((acc, pre, i) => {
    const { seriesName, axisIndex } = pre
    const yName = seriesName
      .split(GROUP_SPLIT)
      .slice(group.length)
      .join(YNAME_SPLIT)
    // 原始字段
    const originField = yFields.find(t => t.displayName === yName)
    // 原始字段渲染名
    const originRenderName = originField.renderName

    if (typeof acc[originRenderName] === 'undefined') {
      acc[originRenderName] = {
        originField,
        data: []
      }
    }

    // 对比字段
    const vsField = yFields.find(t => t.renderName === originRenderName)

    // 将当前指标或对比指标分为一组
    if (yName === originField.displayName || yName === vsField?.displayName) {
      acc[originRenderName]['data'].push(pre)
    }

    return acc
  }, {})

  let tooltipStr = ''

  Object.keys(listMap).forEach((key, idx) => {
    const { originField, data } = listMap[key]
    const { name } = list[0]
    const marginTop = idx === 0 ? 0 : '10px'
    // 维度值的显示(多维度只显示一个多维度值)
    const xValueEle =
      !hasGroup && idx > 0
        ? ''
        : `<div style="margin-top: ${marginTop};font-size:14px;color:#666;font-weight:600;">
      ${name || '-'}
      <span style="float:right;padding-left:15px;">
        ${hasGroup ? originField.displayName : ''}
      </span>
    </div>`

    // dom字符串
    let listStr = xValueEle

    data.forEach((item, i, arr) => {
      const { marker, seriesName, value } = item
      const marginTop = '5px'
      const displayYName = hasGroup
        ? seriesName.split(GROUP_SPLIT).slice(0, -1).join(YNAME_SPLIT)
        : // .replace(YNAME_SPLIT + originField.displayName, '')
          seriesName

      let displayValue =
        typeof value === 'undefined'
          ? '-'
          : formatFieldDisplay(value, originField, datasetFields, formatters)

      const fastCompute = originField.fastCompute
      if (fastCompute?.includes('ratio')) {
        displayValue = (displayValue * 100).toFixed(2) + '%'
      }

      let targetValue = value

      // 是否对比字段
      const isVs = originField._isVs

      if (isVs) {
        // 字段的原始name
        const [fieldOriginRenderName] =
          originField.renderName.split(VS_FIELD_SUFFIX)

        if (listMap[fieldOriginRenderName]) {
          targetValue = listMap[fieldOriginRenderName]['data'][0].value
        }

        if (compareMode === 2) {
          displayValue += `
          <span style="color: ${getDiffColor(value, targetValue)}">
            (${getDiffValue(value, targetValue, compareMode)})
          </span>`
        } else if (compareMode === 1) {
          displayValue += `
          <span style="color: ${getDiffColor(value, targetValue)}">
            (${getDiffValue(value, targetValue, compareMode)})
          </span>`
        }
      }

      listStr += `<div style="display: flex; align-items: center; margin-top: ${marginTop}">
        ${marker}
        <div style="flex: 1; overflow: hidden; text-overflow: ellipsis; word-break: break-al">
          ${displayYName}
        </div>
        <span style="font-weight:600;float:right;padding-left:15px;">
          ${displayValue}
        </span>
      </div>`
    })

    tooltipStr += listStr
  })

  return tooltipStr
}
