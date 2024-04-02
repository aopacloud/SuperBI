import { CATEGORY } from '@/CONST.dict.js'
import { createIsEqualFromKey } from 'common/utils/help'
import { lightenColor } from 'common/utils/color'
import { toDigit, toThousand, toPercent } from 'common/utils/number'
import { colors, CHART_GRID_HEIGHT } from 'common/components/Charts/utils/default.js'
import {
  VS_FIELD_SUFFIX,
  formatDtWithOption,
  formatFieldDisplay,
  createSortByOrder,
} from './index.js'
import { versionJs } from '@/versions'

// 连接分组的字符(唯一，需要做分割)
export const GROUP_SPLIT = '§º§'
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
export function getDiffvalue(origin, target, mode) {
  if (mode === 2) {
    const v = toPercent((target - origin) / origin, 2)

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
  const widht = chart.getWidth()
  const height = chart.getHeight()

  return { widht, height }
}

/**
 * 处理源数据字段和数据的排序（根据维度时间）
 * @param  {{ originFields: array<T>, originData: array<K> }}
 * @param {array<T>} originFields 源字段
 * @param {array<K>} originData 源数据
 * @returns {{fields: array<T>, data: array<K>}}
 */
export const transformOriginBySort = ({ originFields = [], originData = [] }) => {
  const xIndex = originFields.findIndex(
    t => t.category === CATEGORY.PROPERTY && versionJs.ViewsAnalysis.isDateField(t)
  )
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
    .sort(createSortByOrder(true, 0))
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
  group = [],
}) => {
  const xData = [
    ...new Set(
      data.map(row => {
        return row[0]
      })
    ),
  ]

  // 获取非分组数据
  const getData = () => {
    const resMap = yFields.reduce((acc, cur) => {
      if (typeof acc[cur.renderName] === 'undefined') {
        acc[cur.renderName] = {
          field: cur,
          data: Array(data.length).fill(),
          max: 0,
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

      if (field.category === CATEGORY.PROPERTY && field.dataType.includes('TIME')) {
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
            max: 0,
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
    xData,
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
  if (!splited) {
    chart.resize({ width: 'auto', height: 'auto' })

    return { ...grid }
  }

  const domHeight = chart.getDom().clientHeight
  const gridsHeight = CHART_GRID_HEIGHT * fields.length + 50

  chart.resize({
    width: 'auto',
    height: gridsHeight > domHeight ? gridsHeight : 'auto',
  })

  return fields.map((v, i) => {
    return {
      ...grid,
      top: i * CHART_GRID_HEIGHT + 55,
      height: CHART_GRID_HEIGHT - 55,
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
      gridIndex: i,
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
      const max = Math.max(...dataGrouped[field.renderName].map(t => t.max || 0))
      const maxFormat = formatFieldDisplay(max, field, datasetFields)

      return {
        ...yAxis,
        gridIndex: i,
        position: axisItem?.yAxisPosition || 'left',
        name: field.displayName,
        nameGap: getStrLength(maxFormat),
        axisLabel: {
          ...axis.axisLabel,
          formatter: v => {
            return formatFieldDisplay(v, field, datasetFields)
          },
        },
      }
    })
  }

  // 左右轴字段
  const [leftAxis, rightAxis] = axis.reduce(
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
  // 拼接一个左轴一个右轴
  return yFields.reduce(
    (acc, cur) => {
      const axisItem = axis.find(a => a.name === cur.name)

      const genYAxis = () => {
        const axisFields = axisItem?.yAxisPosition === 'right' ? rightAxis : leftAxis

        const max = Math.max(
          ...(dataGrouped[cur.renderName] || []).map(t => t.max || 0)
        )

        // const max = Math.max(
        //   ...(axisFields.map(field => dataMap[field.renderName]?.['max']) || 0)
        // )
        const isFormatSamed = isSameFieldFormat(
          axisFields.map(t => datasetFields.find(d => d.renderName === t.renderName))
        )
        const maxFormat = isFormatSamed
          ? formatFieldDisplay(max, axisFields[0], datasetFields)
          : toDigit(max, 2)

        return {
          ...yAxis,
          alignTicks: true,
          position: axisItem?.yAxisPosition,
          name: axisFields.map(t => t.displayName).join('/'),
          nameGap: getStrLength(maxFormat),
          axisLabel: {
            ...axis.axisLabel,
            formatter: v => {
              if (v === 0) return 0

              return isFormatSamed
                ? formatFieldDisplay(v, axisFields[0], datasetFields)
                : toDigit(v, 2)
            },
          },
        }
      }

      if (axisItem?.yAxisPosition === 'left') {
        if (!Object.keys(acc[0]).length) {
          acc[0] = genYAxis()
        }
      }

      if (axisItem?.yAxisPosition === 'right') {
        if (!Object.keys(acc[1]).length) {
          acc[1] = genYAxis()
        }
      }

      return acc
    },
    [{}, {}]
  )
}

/**
 * 生成系列数据
 * @param {{ chartType: string, xData: string[], yFields: array, dataMap: any, group: array, config: any, datasetFields: array }}
 * @param {string} chartType 图表类型
 * @param {string[]} xData 维度数据
 * @param {string[]} yFields y轴字段
 * @param {any} dataMap 数据表
 * @param {string} group 分组
 * @param {any} config 配置
 * @param {string[]} datasetFields 数据集字段
 */
export const generateSeries = ({
  instance,
  chartType = 'line',
  xData = [],
  yFields = [],
  dataMap = {},
  group = [],
  config = {},
  axis = [],
  datasetFields = [],
}) => {
  const labelVisible = config.labelShow || false
  const splited = config.splited

  const getColor = () => {
    let colorIndex = 0
    return yIndex => {
      const field = yFields[yIndex]
      let color = colors[colorIndex]

      if (field._isVs) {
        color = lightenColor(colors[colorIndex - 1], 0.7)
      } else {
        colorIndex++
      }

      return color
    }
  }
  // 获取 label
  const getCommonSeriesLabel = () => {
    return {
      show: labelVisible,
      position: [0, -15],
      formatter(series) {
        const { seriesName, value } = series

        const ySourceFieldName = group.length
          ? seriesName.split(GROUP_SPLIT).slice(group.length).join('')
          : seriesName
        const field = yFields.find(t => t.displayName === ySourceFieldName)

        return formatFieldDisplay(value, field, datasetFields)
      },
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

  const getCommonSeries = () => {
    const getSeriesColor = getColor()

    return Object.keys(dataMap).reduce((acc, mapKey) => {
      const { field, data } = dataMap[mapKey]
      const fieldIndex = yFields.findIndex(t => t.renderName === field.renderName)
      const color = getSeriesColor(fieldIndex)
      const axisItem = axis.find(t => t.name === field.name)

      const seriesItem = {
        _y: field,
        type: axisItem?.chartType ?? chartType,
        name: getCommonSeriesName(mapKey, field),
        barMaxWidth: 40,
        color,
        data,
        label: getCommonSeriesLabel(),
        labelLayout: { hideOverlap: true },
        position: axisItem?.yAxisPosition,
        xAxisIndex: splited ? fieldIndex : 0,
        yAxisIndex: splited
          ? fieldIndex
          : axisItem?.yAxisPosition === 'right'
          ? 1
          : 0,
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
    const yName = seriesName.split(GROUP_SPLIT).slice(group.length).join(YNAME_SPLIT)
    // 原始字段
    const originField = yFields.find(t => t.displayName === yName)
    // 原始字段渲染名
    const originRenderName = originField.renderName

    if (typeof acc[originRenderName] === 'undefined') {
      acc[originRenderName] = {
        originField,
        data: [],
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
          : formatFieldDisplay(value, originField, datasetFields)
      let preValue = value

      // 是否对比字段
      const isVs = originField._isVs

      if (isVs) {
        // 字段的原始name
        const [fieldOriginRenderName] = originField.renderName.split(VS_FIELD_SUFFIX)

        preValue = listMap[fieldOriginRenderName]['data'][0].value
      }

      const getStyle = (origin, target) => {
        return target === origin ? '' : target > origin ? '#67C23A' : '#F56C6C'
      }

      if (isVs) {
        if (compareMode === 2) {
          displayValue += `
          <span style="color: ${getStyle(preValue, value)}">
            (${getDiffvalue(preValue, value, compareMode)})
          </span>`
        } else if (compareMode === 1) {
          displayValue += `
          <span style="color: ${getStyle(preValue, value)}">
            (${getDiffvalue(preValue, value, compareMode)})
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
