import { message } from 'ant-design-vue'
import { CATEGORY } from '@/CONST.dict.js'
import { deepClone, getWordWidth } from 'common/utils/help'
import { merge } from 'common/utils/object'
import {
  gridCommon,
  legendCommon,
  legendPositionMap,
  colors,
  xAxisCommon,
  yAxisCommon
} from 'common/components/Charts/utils/default.js'
import {
  getChartSize,
  GROUP_SPLIT,
  YNAME_SPLIT,
  transformOriginBySort,
  generateDataMap,
  tooltipFormat,
  generateGrid,
  generateXAxis,
  generateYAxis,
  generateSeries
} from './utils'
import {
  transformFieldsByVs,
  formatDtWithOption,
  transformQuickCompute
} from './index.js'
import { isDateField } from '@/views/dataset/utils'
import { chartTypeMap } from '@/views/analysis/LayoutAside/components/SettingSections/config'
import { sum } from '@/common/utils/number'
import { createSummaryMap } from '../Table/utils'
import { transformWithQuickIndex } from '@/views/analysis/utils'

export const MAX_GROUP_COUNT = 1000

export const MAX_GROUP_COUNT_MESSAGE = t =>
  `当前条件分组过多，不适合使用${chartTypeMap[t]['label']}, 请切换至其他图表`

/**
 * 创建图表配置
 * @param  {{ instance: echarts, originFields: array<any>, originData: array<any>, config: object, datasetFields: array<any> }}
 * @param  {Chart} params.Chart               图表实例
 * @param  {array<any>} params.originFields   原始字段
 * @param  {array<any>} params.originData     原始数据
 * @param  {object} params.config             图表配置
 * @param  {array<any>} params.datasetFields  图表配置
 * @return {[type]}                           渲染图表配置项
 */
export default function createChart({
  instance,
  chartType = 'bar',
  originFields = [],
  originData = [],
  datasetFields = [],
  compare,
  config = {},
  extraChartOptions = {},
  sorters = { row: [], column: [] },
  formatters = [],
  summaryRows = []
}) {
  const {
    legend: legendConfig,
    splited = false,
    compare: compareConfig = {},
    xAxis: xAxisConfig = {},
    yAxis: yAxisConfig = {}
  } = config

  // 处理字段、数据
  let { fields, data } = transformOriginBySort({
    sorters,
    originFields: deepClone(originFields.map(transformWithQuickIndex)),
    originData: deepClone(originData)
  })

  // 修复历史数据的交叉表格切换到图表时，分组类型导致的计算汇总数据错误
  fields = fields.map(t => ((t._group = undefined), t))

  // 处理对比字段
  fields = transformFieldsByVs({ fields, compare })

  // 汇总数据
  const summaryMap = createSummaryMap(summaryRows, fields)

  // 处理快速计算的数据
  data = transformQuickCompute({
    list: data,
    columns: fields,
    summary: summaryMap
  })

  // 维度字段
  const xFields = fields.filter(t => t.category === CATEGORY.PROPERTY)
  // 指标字段
  const yFields = fields.filter(t => t.category === CATEGORY.INDEX)

  // 取排序后的第一个x字段
  let x = xFields[0]

  // 初始化边界错误
  if (!x) return

  // x轴字段的索引
  const xIndex = fields.findIndex(t => t.renderName === x.renderName)
  // xFields转Y轴的索引
  const groupFields = xFields.filter((t, i) => i !== xIndex)

  // 转化的数据
  let { xData, dataMap } = generateDataMap({
    originFields: fields,
    yFields,
    data,
    group: groupFields
  })

  if (groupFields.length && Object.keys(dataMap).length > MAX_GROUP_COUNT) {
    message.warn(MAX_GROUP_COUNT_MESSAGE(chartType))

    throw Error('当前数据分组超出限制')
  }

  // 将dt维度按照分组格式化显示
  if (fields[0].dataType.includes('TIME')) {
    xData = xData.map(t => formatDtWithOption(t, fields[0]))
  }

  // 布局配置
  const gridOptions = generateGrid({
    splited,
    chart: instance,
    fields: yFields,
    grid: merge(gridCommon, extraChartOptions.grid)
  })

  // 图表尺寸
  const chartSize = getChartSize(instance)

  // x轴显示
  const xAxisVisible = xAxisConfig.show ?? true
  const xNameVisible = xAxisConfig.nameShow ?? true

  // 判断x轴标签是否需要旋转
  let shouldAxisLabelRotate = false
  if (xAxisConfig.labelCount === 'max') {
    const allLabelWidth = sum(xData.map(t => getWordWidth(t, 8, 18)))
    shouldAxisLabelRotate =
      allLabelWidth > chartSize.width - gridCommon.left - gridCommon.right
  }

  const xAxis = {
    ...xAxisCommon,
    show: xAxisVisible ?? true,
    name: xNameVisible ? x.displayName : undefined,
    nameGap: shouldAxisLabelRotate
      ? Math.max(...xData.map(t => getWordWidth(t))) + 10
      : 35,
    type: 'category',
    nameLocation: 'middle',
    data: xData,
    axisLabel: {
      ...xAxisCommon.axisLabel,
      interval:
        xAxisConfig.labelCount === 'max'
          ? 0
          : xAxisConfig.labelCount === 'thin'
            ? 1
            : undefined,
      rotate: shouldAxisLabelRotate ? 60 : 0
    }
  }

  // x轴配置
  const xAxisOptions = !splited
    ? { ...xAxis }
    : generateXAxis({ yFields, xAxis })

  // y轴显示
  const yAxisVisible = yAxisConfig.show ?? true
  const yNameVisible = yAxisConfig.nameShow ?? true
  // 不显示y轴时，调整图表布局
  // if (!yAxisShow) {
  //   gridOptions.left = 40
  //   gridOptions.containLabel = false
  // }

  // 处理轴值范围
  const delRangeVal = v => {
    if (typeof v === 'undefined' || v === '') return
    return Number(v)
  }

  const yAxis = {
    ...yAxisCommon,
    show: yAxisVisible,
    name: yNameVisible ? true : undefined,
    nameGap: yAxisVisible ? undefined : 20,
    type: 'value',
    nameTextStyle: {
      width: chartSize?.height - 80,
      overflow: 'truncate'
    },
    nameTruncate: {
      // 标题文字样式
      maxWidth: chartSize?.height - 80,
      ellipsis: '...'
    },
    axisLabel: {
      color: '#999',
      fontSize: 12
    },
    // alignTicks: yAxisConfig.interval === 'count' ? false : true,
    min: delRangeVal(yAxisConfig.range?.min),
    max: delRangeVal(yAxisConfig.range?.max),
    splitNumber:
      yAxisConfig.interval === 'count' ? yAxisConfig.intervalCount : undefined,
    interval:
      yAxisConfig.interval === 'step' ? yAxisConfig.intervalStep : undefined
  }

  // y轴
  const yAxisOptions = generateYAxis({
    yFields,
    yAxis,
    datasetFields,
    axis: config.yAxis?.axis ?? config.axis,
    splited,
    dataMap,
    formatters,
    xData
  })

  // echarts 所需数据结构
  const seriesOptions = generateSeries({
    chart: instance,
    chartType: chartType,
    dataMap: dataMap,
    group: groupFields,
    yFields: yFields,
    config,
    datasetFields,
    formatters
  })

  // 避免多次触发
  // instance.chart.off('legendselectchanged')
  // instance.chart.on('legendselectchanged', payload => {
  //   onLegendselectchanged(payload, {
  //     chart: instance.chart,
  //     IS_SPLIT,
  //     axis,
  //     yAxis,
  //     originFields: datasetFields,
  //     group: groupFields,
  //     dataMap,
  //   })
  // })

  return {
    tooltip: {
      trigger: 'axis',
      confine: true,
      enterable: true,
      extraCssText: 'max-width:70%;max-height:96%;overflow:auto',
      axisPointer: {
        type: 'shadow'
      },
      formatter: series => {
        return tooltipFormat({
          series,
          yFields,
          datasetFields,
          compareMode: compareConfig.mode,
          group: groupFields,
          formatters
        })
      }
    },
    axisPointer: {
      link: [
        {
          xAxisIndex: 'all'
        }
      ]
    },
    backgroundColor: 'transparent',
    color: colors,
    legend: Object.assign(
      {
        ...legendCommon,
        // ...legendPositionMap[legend.position],
        formatter(name) {
          return name.split(GROUP_SPLIT).join(YNAME_SPLIT)
        }
      },
      legendConfig
        ? { show: true, ...legendPositionMap[legendConfig?.position] }
        : { show: false }
    ),

    grid: gridOptions,
    xAxis: xAxisOptions,
    series: seriesOptions, //.map(t => ({ ...t, data: undefined })),
    yAxis: yAxisOptions
    // dataset: {
    //   dimensions: originFields.map(t => t.displayName),
    //   source: data
    // }
  }
}
