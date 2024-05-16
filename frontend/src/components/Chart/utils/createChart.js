import { message } from 'ant-design-vue'
import { CATEGORY } from '@/CONST.dict.js'
import { deepClone } from 'common/utils/help'
import { merge } from 'common/utils/object'
import {
  gridCommon,
  legendCommon,
  legendPositionMap,
  colors,
  xAxisCommon,
  yAxisCommon,
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
  generateSeries,
} from './utils'
import { transformFieldsByVs, formatDtWithOption } from './index.js'
import { isDateField } from '@/views/dataset/utils'
import { chartTypeMap } from '@/views/analysis/LayoutAside/config'

export const MAX_GROUP_COUNT = 1000

export const MAX_COUNT_MESSAGE = t =>
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
}) {
  const {
    axis = [],
    legend = {},
    splited = false,
    compare: compareOption = {},
  } = config

  // 处理字段、数据
  const { fields: fieldsSorted, data } = transformOriginBySort({
    originFields: deepClone(originFields),
    originData: deepClone(originData),
  })

  // 处理对比字段
  const fields = transformFieldsByVs({ fields: fieldsSorted, compare }).map(
    (t, i) => {
      return { ...t, yAxisIndex: i }
    }
  )

  // 维度字段
  const xFields = fields.filter(t => t.category === CATEGORY.PROPERTY)
  // 指标字段
  const yFields = fields.filter(t => t.category === CATEGORY.INDEX)

  // 以时间字段(或者第一个字段)为x轴，
  let x = xFields.find(isDateField)
  if (!x) x = xFields[0]
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
    group: groupFields,
  })

  if (groupFields.length && Object.keys(dataMap).length > MAX_GROUP_COUNT) {
    message.warn(MAX_COUNT_MESSAGE(chartType))

    throw Error('当前数据分组超出限制')
  }

  // 将dt维度按照分组格式化显示
  if (fieldsSorted[0].dataType.includes('TIME')) {
    xData = xData.map(t => formatDtWithOption(t, fieldsSorted[0]))
  }

  // 布局配置
  const gridOptions = generateGrid({
    splited,
    chart: instance,
    fields: yFields,
    grid: merge(gridCommon, extraChartOptions.grid),
  })

  const xAxis = {
    ...xAxisCommon,
    name: x.displayName,
    nameGap: 35,
    type: 'category',
    nameLocation: 'middle',
    data: xData,
  }
  // x轴配置
  const xAxisOptions = !splited ? { ...xAxis } : generateXAxis({ yFields, xAxis })

  // 图表尺寸
  const chartSize = getChartSize(instance)
  const yAxis = {
    ...yAxisCommon,
    type: 'value',
    nameTextStyle: {
      width: chartSize?.height - 80,
      overflow: 'truncate',
    },
    nameTruncate: {
      // 标题文字样式
      maxWidth: chartSize?.height - 80,
      ellipsis: '...',
    },
    axisLabel: {
      color: '#999',
      fontSize: 12,
    },
  }

  // y轴
  const yAxisOptions = generateYAxis({
    yFields,
    yAxis,
    datasetFields,
    axis,
    splited,
    dataMap,
  })

  // echarts 所需数据结构
  const seriesOptions = generateSeries({
    chart: instance,
    chartType: chartType,
    dataMap: dataMap,
    xData,
    group: groupFields,
    yFields,
    yAxis,
    axis,
    config,
    datasetFields,
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
        type: 'shadow',
      },
      formatter: series => {
        return tooltipFormat({
          series,
          yFields,
          datasetFields,
          compareMode: compareOption.mode,
          group: groupFields,
        })
      },
    },
    axisPointer: {
      link: [
        {
          xAxisIndex: 'all',
        },
      ],
    },
    backgroundColor: 'transparent',
    color: colors,
    legend: {
      ...legendCommon,
      ...legendPositionMap[legend.position],
      formatter(name) {
        return name.split(GROUP_SPLIT).join(YNAME_SPLIT)
      },
    },

    grid: gridOptions,
    xAxis: xAxisOptions,
    series: seriesOptions,
    yAxis: yAxisOptions,
  }
}
