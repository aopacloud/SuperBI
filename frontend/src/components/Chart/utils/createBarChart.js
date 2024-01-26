import { CATEGORY } from '@/CONST.dict.js'
import {
  gridCommon,
  legendCommon,
  legendPositionMap,
  colors,
  xAxisCommon,
} from 'common/components/Charts/utils/default.js'

import {
  getChartSize,
  GROUP_SPLIT_CHARACTER,
  YNAME_JOIN_CHARACTER,
  transformData,
  transformWithOrigin,
  formatFieldDisplay,
  tooltipFormat,
  isSameFieldFormat,
  generateGridOption,
  generateXAxisOption,
  generateYAxisOption,
  generateYAxisOptionByAxisMap,
  generateSeriesData,
  getStrLength,
} from './utils'

const CHART_TYPE = 'bar'

/**
 * 创建图表配置
 * @param  {{ instance: Chart, originFields: array<any>, originData: array<any>, config: object, datasetFields: array<any> }}
 * @param  {Chart} params.Chart               图表实例
 * @param  {array<any>} params.originFields   原始字段
 * @param  {array<any>} params.originData     原始数据
 * @param  {object} params.config             图表配置
 * @param  {array<any>} params.datasetFields  图表配置
 * @return {[type]}                           渲染图表配置项
 */
export default function createChart({
  instance,
  originFields = [],
  originData = [],
  datasetFields = [],
  config = {},
  compare,
}) {
  const { axis = [], compare: compareOption = {} } = config

  // 处理字段、数据
  const { fields, data } = transformWithOrigin({
    originFields,
    originData,
  })

  // 维度字段
  const xFields = fields.filter(t => t.category === CATEGORY.PROPERTY)
  // 指标字段
  const yFields = fields.filter(t => t.category === CATEGORY.INDEX) //.map((t, i) => ({ ...t, yAxisIndex: i })) // 获取掉维度后重新设置指标Y轴

  // 以时间字段(或者第一个字段)为x轴，
  let x = xFields.find(t => t.dataType.includes('TIME'))
  if (!x) x = xFields[0]

  // 初始化边界错误
  if (!x) return

  // x轴字段的索引
  const xIndex = fields.findIndex(t => t.name === x.name)

  // xFields转Y轴的索引
  const groupXFields = xFields.filter(t => t._index !== xIndex) //.map(t => t._index)

  // 转化的数据
  const { dimensions, dataMap } = transformData({
    originFields: originFields,
    yFields,
    data,
    xIndex,
    groups: groupXFields,
  })

  const IS_SPLIT = yFields.length > 1 && !config.splited

  const xAxis = {
    ...xAxisCommon,
    name: x.displayName,
    nameGap: 35,
    type: 'category',
    nameLocation: 'middle',
    data: dimensions,
  }

  // 图表尺寸
  const chartSize = getChartSize(instance)

  // y轴最后一个刻度值(最大值)
  const maxValue = Math.max(...Object.keys(dataMap).map(k => dataMap[k]['max']))
  // 最大值格式化
  const maxFormat = !isSameFieldFormat(yFields)
    ? maxValue
    : formatFieldDisplay(maxValue, yFields[0], datasetFields)

  const yAxis = {
    type: 'value',
    position: 'left',
    scale: true,
    name: yFields.map(t => t.displayName).join('/'),
    nameGap: getStrLength(maxFormat),
    barMaxWidth: 40,
    nameLocation: 'middle',
    nameTextStyle: {
      width: chartSize.height - 80,
      overflow: 'truncate',
    },
    nameTruncate: {
      // 标题文字样式
      maxWidth: chartSize.height - 80,
      ellipsis: '...',
    },
    alignTicks: true, // 刻度线对齐
    axisLabel: {
      color: '#999',
      fontSize: 12,
      formatter(v) {
        const isLeftYAxisSameFormat = isSameFieldFormat(yFields)
        if (!isLeftYAxisSameFormat) {
          return v
        }

        return formatFieldDisplay(v, yFields[0], datasetFields)
      },
    },
    // 网格分割线
    splitLine: {
      lineStyle: {
        color: '#F3F4F4',
      },
    },
    // 轴刻度
    axisTick: {
      show: false,
    },
    // 轴线
    axisLine: {
      show: false,
    },
  }

  // echarts 所需数据结构
  const seriesData = generateSeriesData({
    dataMap: dataMap,
    dimensions,
    groups: groupXFields,
    yFields,
    yAxis,
    config,
    IS_SPLIT,
    chartType: CHART_TYPE,
    groupXFields,
    originFields: datasetFields,
    chart: instance,
  })

  // 布局数据
  const gridData = !IS_SPLIT
    ? { ...gridCommon }
    : generateGridOption({ chart: instance, fields: yFields, grid: gridCommon })

  // x轴
  const xAxisData = !IS_SPLIT ? xAxis : generateXAxisOption({ fields: yFields, xAxis })

  // y轴
  let yAxisData = []
  // 拆分显示
  if (IS_SPLIT) {
    // 拆分根据指标显示多个y轴
    yAxisData = generateYAxisOption({ fields: yFields, yAxis, datasetFields, dataMap })
  } else {
    yAxisData = { ...yAxis }
  }

  // 避免多次触发
  // instance.chart.off('legendselectchanged')
  // instance.chart.on('legendselectchanged', payload => {
  //   onLegendselectchanged(payload, {
  //     chart: instance.chart,
  //     IS_SPLIT,
  //     axis,
  //     yAxis,
  //     originFields: datasetFields,
  //     group: groupXFields,
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
          originFields: datasetFields,
          compareMode: compareOption.mode,
          group: groupXFields,
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
      ...legendPositionMap[config.legend.position],
      formatter(name) {
        return name.split(GROUP_SPLIT_CHARACTER).join(YNAME_JOIN_CHARACTER)
      },
    },

    grid: gridData,
    xAxis: xAxisData,
    yAxis: yAxisData,
    series: seriesData,
  }
}
